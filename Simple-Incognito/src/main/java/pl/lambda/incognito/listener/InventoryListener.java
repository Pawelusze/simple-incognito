package pl.lambda.incognito.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import pl.lambda.incognito.IncognitoPlugin;
import pl.lambda.incognito.inventory.IncognitoInventory;
import pl.lambda.incognito.manager.IncognitoManager;

public class InventoryListener implements Listener {

    private final IncognitoPlugin plugin;

    public InventoryListener(IncognitoPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (!(event.getInventory().getHolder() instanceof IncognitoInventory gui)) {
            return;
        }

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) {
            return;
        }

        if (gui.isNicknameItem(clickedItem)) {
            IncognitoManager.toggleNickname(plugin, player);
            player.closeInventory();
            new IncognitoInventory(plugin, player).open();
        } else if (gui.isSkinItem(clickedItem)) {
            IncognitoManager.toggleSkin(plugin, player);
            player.closeInventory();
            new IncognitoInventory(plugin, player).open();
        }
    }
}
