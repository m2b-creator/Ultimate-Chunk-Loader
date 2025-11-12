package solutions.m2b.chunkLoader;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ChunkLoaderGUI {
    private final ChunkLoader plugin;
    private final ChunkLoaderManager manager;

    public ChunkLoaderGUI(ChunkLoader plugin, ChunkLoaderManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    public void openMainMenu(Player player) {
        Collection<ChunkLoaderRegion> regions = manager.getAllRegions();

        plugin.getLogger().info("Opening GUI for " + player.getName() + " with " + regions.size() + " regions");

        int size = Math.min(54, ((regions.size() + 8) / 9) * 9);
        if (size < 9) size = 9;

        Inventory gui = Bukkit.createInventory(null, size, Component.text("Chunk Loaders", NamedTextColor.DARK_GREEN));

        int slot = 0;
        for (ChunkLoaderRegion region : regions) {
            if (slot >= size) break;

            plugin.getLogger().info("Adding region '" + region.getName() + "' to slot " + slot);
            ItemStack item = createRegionItem(region);
            gui.setItem(slot, item);
            slot++;
        }

        plugin.getLogger().info("GUI created with " + slot + " items, opening for player");
        player.openInventory(gui);
    }

    private ItemStack createRegionItem(ChunkLoaderRegion region) {
        Material material = region.isEnabled() ? Material.LIME_WOOL : Material.RED_WOOL;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text(region.getName(), region.isEnabled() ? NamedTextColor.GREEN : NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("World: " + region.getWorldName(), NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Chunks: " + region.getChunkCount(), NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Status: " + (region.isEnabled() ? "Enabled" : "Disabled"),
                region.isEnabled() ? NamedTextColor.GREEN : NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Left Click: View Info", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Right Click: " + (region.isEnabled() ? "Disable" : "Enable"), NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Shift + Left Click: Remove", NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false));

        meta.lore(lore);
        item.setItemMeta(meta);

        return item;
    }

    public void openConfirmRemoveMenu(Player player, String regionName) {
        Inventory gui = Bukkit.createInventory(null, 27, Component.text("Confirm to remove: " + regionName, NamedTextColor.RED));

        ItemStack confirm = new ItemStack(Material.GREEN_WOOL);
        ItemMeta confirmMeta = confirm.getItemMeta();
        confirmMeta.displayName(Component.text("Confirm to remove", NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false));
        List<Component> confirmLore = new ArrayList<>();
        confirmLore.add(Component.text("Click to permanently remove", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        confirmLore.add(Component.text(regionName, NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        confirmMeta.lore(confirmLore);
        confirm.setItemMeta(confirmMeta);

        ItemStack cancel = new ItemStack(Material.RED_WOOL);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.displayName(Component.text("Cancel", NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false));
        List<Component> cancelLore = new ArrayList<>();
        cancelLore.add(Component.text("Return to main menu", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        cancelMeta.lore(cancelLore);
        cancel.setItemMeta(cancelMeta);

        gui.setItem(11, confirm);
        gui.setItem(15, cancel);

        player.openInventory(gui);
    }

    public String getRegionNameFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta.displayName() == null) {
            return null;
        }

        Component displayName = meta.displayName();
        if (displayName instanceof net.kyori.adventure.text.TextComponent) {
            return ((net.kyori.adventure.text.TextComponent) displayName).content();
        }

        return null;
    }
}
