package pl.lambda.incognito.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta meta;
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder(ItemStack item) {
        this.item = item.clone();
        this.meta = this.item.getItemMeta();
    }

    public ItemBuilder setDisplayName(String name) {
        if (meta != null) {
            Component component = LEGACY_SERIALIZER.deserialize(name)
                .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false);
            meta.displayName(component);
        }
        return this;
    }

    public ItemBuilder setLore(String... lore) {
        if (meta != null) {
            List<Component> componentLore = new ArrayList<>();
            for (String line : lore) {
                Component component = LEGACY_SERIALIZER.deserialize(line)
                    .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false);
                componentLore.add(component);
            }
            meta.lore(componentLore);
        }
        return this;
    }

    public ItemBuilder setLore(List<String> lore) {
        if (meta != null) {
            List<Component> componentLore = new ArrayList<>();
            for (String line : lore) {
                Component component = LEGACY_SERIALIZER.deserialize(line)
                    .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false);
                componentLore.add(component);
            }
            meta.lore(componentLore);
        }
        return this;
    }

    public ItemBuilder addLore(String... lines) {
        if (meta != null) {
            List<Component> currentLore = meta.lore();
            if (currentLore == null) {
                currentLore = new ArrayList<>();
            }

            for (String line : lines) {
                Component component = LEGACY_SERIALIZER.deserialize(line)
                    .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false);
                currentLore.add(component);
            }
            meta.lore(currentLore);
        }
        return this;
    }

    public ItemBuilder setAmount(int amount) {
        item.setAmount(amount);
        return this;
    }

    public ItemBuilder setSkullOwner(Player player) {
        if (meta instanceof SkullMeta) {
            ((SkullMeta) meta).setOwningPlayer(player);
        }
        return this;
    }

    public ItemBuilder setUnbreakable(boolean unbreakable) {
        if (meta != null) {
            meta.setUnbreakable(unbreakable);
        }
        return this;
    }

    public ItemStack build() {
        if (meta != null) {
            item.setItemMeta(meta);
        }
        return item;
    }
}
