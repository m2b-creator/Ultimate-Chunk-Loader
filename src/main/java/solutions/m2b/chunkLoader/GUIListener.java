package solutions.m2b.chunkLoader;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {
    private final ChunkLoader plugin;
    private final ChunkLoaderManager manager;
    private final ChunkLoaderGUI gui;

    public GUIListener(ChunkLoader plugin, ChunkLoaderManager manager, ChunkLoaderGUI gui) {
        this.plugin = plugin;
        this.manager = manager;
        this.gui = gui;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();

        Component title = event.getView().title();
        if (title == null) {
            return;
        }

        String titleString = getPlainText(title);
        plugin.getLogger().info("GUI Click detected - Title: '" + titleString + "'");

        if (titleString.equals("Chunk Loaders")) {
            event.setCancelled(true);
            plugin.getLogger().info("Handling main menu click");
            handleMainMenuClick(event, player);
        } else if (titleString.startsWith("Confirm to remove: ")) {
            event.setCancelled(true);
            plugin.getLogger().info("Handling confirm to remove click");
            handleConfirmMenuClick(event, player, titleString.substring(19));
        }
    }

    private void handleMainMenuClick(InventoryClickEvent event, Player player) {
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        String regionName = gui.getRegionNameFromItem(clicked);
        if (regionName == null) {
            return;
        }

        ChunkLoaderRegion region = manager.getRegion(regionName);
        if (region == null) {
            player.sendMessage(Component.text("Chunk loader not found!", NamedTextColor.RED));
            player.closeInventory();
            return;
        }

        ClickType clickType = event.getClick();

        if (clickType == ClickType.LEFT) {
            player.closeInventory();
            showRegionInfo(player, region);
        } else if (clickType == ClickType.RIGHT) {
            if (region.isEnabled()) {
                if (!player.hasPermission("chunkloader.disable")) {
                    player.sendMessage(Component.text("You don't have permission to disable chunk loaders", NamedTextColor.RED));
                    return;
                }
                if (manager.disableRegion(regionName)) {
                    plugin.saveData();
                    player.sendMessage(Component.text("Disabled chunk loader '" + regionName + "'", NamedTextColor.GREEN));
                    gui.openMainMenu(player);
                }
            } else {
                if (!player.hasPermission("chunkloader.enable")) {
                    player.sendMessage(Component.text("You don't have permission to enable chunk loaders", NamedTextColor.RED));
                    return;
                }
                if (manager.enableRegion(regionName)) {
                    plugin.saveData();
                    player.sendMessage(Component.text("Enabled chunk loader '" + regionName + "'", NamedTextColor.GREEN));
                    gui.openMainMenu(player);
                }
            }
        } else if (clickType == ClickType.SHIFT_LEFT) {
            if (!player.hasPermission("chunkloader.remove")) {
                player.sendMessage(Component.text("You don't have permission to remove chunk loaders", NamedTextColor.RED));
                return;
            }
            gui.openConfirmRemoveMenu(player, regionName);
        }
    }

    private void handleConfirmMenuClick(InventoryClickEvent event, Player player, String regionName) {
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        if (clicked.getType() == Material.GREEN_WOOL) {
            if (manager.removeRegion(regionName)) {
                plugin.saveData();
                player.sendMessage(Component.text("Removed chunk loader '" + regionName + "'", NamedTextColor.GREEN));
                gui.openMainMenu(player);
            } else {
                player.sendMessage(Component.text("Failed to remove chunk loader", NamedTextColor.RED));
                player.closeInventory();
            }
        } else if (clicked.getType() == Material.RED_WOOL) {
            gui.openMainMenu(player);
        }
    }

    private void showRegionInfo(Player player, ChunkLoaderRegion region) {
        player.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.DARK_GREEN));
        player.sendMessage(Component.text("Chunk Loader: ", NamedTextColor.GRAY)
                .append(Component.text(region.getName(), NamedTextColor.GOLD)));
        player.sendMessage(Component.text("World: ", NamedTextColor.GRAY)
                .append(Component.text(region.getWorldName(), NamedTextColor.AQUA)));
        player.sendMessage(Component.text("Chunks: ", NamedTextColor.GRAY)
                .append(Component.text(String.valueOf(region.getChunkCount()), NamedTextColor.YELLOW)));

        String status = region.isEnabled() ? "Enabled" : "Disabled";
        NamedTextColor statusColor = region.isEnabled() ? NamedTextColor.GREEN : NamedTextColor.RED;
        player.sendMessage(Component.text("Status: ", NamedTextColor.GRAY)
                .append(Component.text(status, statusColor)));

        if (region.getChunkCount() <= 10) {
            player.sendMessage(Component.text("Coordinates:", NamedTextColor.GRAY));
            for (ChunkLoaderRegion.ChunkCoordinate coord : region.getChunks()) {
                player.sendMessage(Component.text("  • Chunk ", NamedTextColor.DARK_GRAY)
                        .append(Component.text(coord.toString(), NamedTextColor.WHITE)));
            }
        }
        player.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.DARK_GREEN));
    }

    private String getPlainText(Component component) {
        if (component instanceof net.kyori.adventure.text.TextComponent) {
            String content = ((net.kyori.adventure.text.TextComponent) component).content();
            plugin.getLogger().info("Extracted plain text from TextComponent: '" + content + "'");
            return content;
        }
        plugin.getLogger().info("Component is not a TextComponent, type: " + component.getClass().getName());
        return "";
    }
}
