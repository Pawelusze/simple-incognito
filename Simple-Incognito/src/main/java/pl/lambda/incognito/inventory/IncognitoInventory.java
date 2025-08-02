package pl.lambda.incognito.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import pl.lambda.incognito.IncognitoPlugin;
import pl.lambda.incognito.util.ItemBuilder;
import pl.lambda.incognito.util.ChatHelper;

public final class IncognitoInventory implements InventoryHolder {

    private final IncognitoPlugin plugin;
    private final Player player;
    private final Inventory inventory;
    private final ItemStack nicknameItem;
    private final ItemStack skinItem;

    public IncognitoInventory(IncognitoPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 9, ChatHelper.translateColors("&8Tryb Incognito"));
        this.nicknameItem = createNicknameItem();
        this.skinItem = createSkinItem();
        setupItems();
    }

    private ItemStack createNicknameItem() {
        var data = plugin.getDataManager().getPlayerData(player);
        var builder = new ItemBuilder(Material.PLAYER_HEAD)
                .setDisplayName("&6Ukryj swój nick");

        // Jeśli skin jest ukryty, nie ustawiaj skull owner żeby nie pokazywać prawdziwego skina
        if (!data.isSkinHidden()) {
            builder.setSkullOwner(player);
        }

        return builder.build();
    }

    private ItemStack createSkinItem() {
        return new ItemBuilder(Material.LEATHER_CHESTPLATE)
                .setDisplayName("&6Ukryj swój skin")
                .build();
    }

    private void setupItems() {
        var data = plugin.getDataManager().getPlayerData(player);

        // Odświeżamy nickname item z aktualnym stanem skina
        var nicknameItemBase = new ItemBuilder(Material.PLAYER_HEAD)
                .setDisplayName("&6Ukryj swój nick");

        if (!data.isSkinHidden()) {
            nicknameItemBase.setSkullOwner(player);
        }

        var displayNicknameItem = new ItemBuilder(nicknameItemBase.build())
                .setLore(
                        "&8>> &7Status: " + (data.isNicknameHidden() ? "&aTAK" : "&cNIE"),
                        "&8>> &7Twój nick: &f" + data.getDisplayNickname(),
                        "&8>> &aKliknij &2LEWYM&a, aby przełączyć"
                )
                .build();

        var displaySkinItem = new ItemBuilder(skinItem.clone())
                .setLore(
                        "&8>> &7Status: " + (data.isSkinHidden() ? "&aTAK" : "&cNIE"),
                        "&8>> &aKliknij &2LEWYM&a, aby przełączyć"
                )
                .build();

        inventory.setItem(3, displayNicknameItem);
        inventory.setItem(5, displaySkinItem);
    }

    public void open() {
        player.openInventory(inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public boolean isNicknameItem(ItemStack item) {
        if (item == null || item.getType() != Material.PLAYER_HEAD) {
            return false;
        }
        var meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }
        var displayName = meta.displayName();
        var expectedName = nicknameItem.getItemMeta().displayName();
        return displayName != null && displayName.equals(expectedName);
    }

    public boolean isSkinItem(ItemStack item) {
        if (item == null || item.getType() != Material.LEATHER_CHESTPLATE) {
            return false;
        }
        var meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }
        var displayName = meta.displayName();
        var expectedName = skinItem.getItemMeta().displayName();
        return displayName != null && displayName.equals(expectedName);
    }

    public IncognitoPlugin getPlugin() {
        return plugin;
    }

    public Player getPlayer() {
        return player;
    }
}
