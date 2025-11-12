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

import java.util.Set;

public class GUIListener implements Listener {
    private final ChunkLoader plugin;
    private final ChunkLoaderManager manager;
    private final ChunkLoaderGUI gui;
    private final MessageHelper messageHelper;

    public GUIListener(ChunkLoader plugin, ChunkLoaderManager manager, ChunkLoaderGUI gui, MessageHelper messageHelper) {
        this.plugin = plugin;
        this.manager = manager;
        this.gui = gui;
        this.messageHelper = messageHelper;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();

        String title = messageHelper.getInventoryTitle(event.getView());
        if (title == null) {
            return;
        }

        String titleString = getPlainText(title);

        if (titleString.equals("Chunk Loaders")) {
            event.setCancelled(true);
            handleMainMenuClick(event, player);
        } else if (titleString.startsWith("Confirm to remove: ")) {
            event.setCancelled(true);
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
            messageHelper.sendMessage(player, Component.text("Chunk loader not found!", NamedTextColor.RED));
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
                    messageHelper.sendMessage(player, Component.text("You don't have permission to disable chunk loaders", NamedTextColor.RED));
                    return;
                }
                if (manager.disableRegion(regionName)) {
                    plugin.saveData();
                    messageHelper.sendMessage(player, Component.text("Disabled chunk loader '" + regionName + "'", NamedTextColor.GREEN));
                    gui.openMainMenu(player);
                }
            } else {
                if (!player.hasPermission("chunkloader.enable")) {
                    messageHelper.sendMessage(player, Component.text("You don't have permission to enable chunk loaders", NamedTextColor.RED));
                    return;
                }
                if (manager.enableRegion(regionName)) {
                    plugin.saveData();
                    messageHelper.sendMessage(player, Component.text("Enabled chunk loader '" + regionName + "'", NamedTextColor.GREEN));
                    gui.openMainMenu(player);
                }
            }
        } else if (clickType == ClickType.SHIFT_LEFT) {
            if (!player.hasPermission("chunkloader.remove")) {
                messageHelper.sendMessage(player, Component.text("You don't have permission to remove chunk loaders", NamedTextColor.RED));
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
                messageHelper.sendMessage(player, Component.text("Removed chunk loader '" + regionName + "'", NamedTextColor.GREEN));
                gui.openMainMenu(player);
            } else {
                messageHelper.sendMessage(player, Component.text("Failed to remove chunk loader", NamedTextColor.RED));
                player.closeInventory();
            }
        } else if (clicked.getType() == Material.RED_WOOL) {
            gui.openMainMenu(player);
        }
    }

    private void showRegionInfo(Player player, ChunkLoaderRegion region) {
        messageHelper.sendMessage(player, Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GREEN));
        messageHelper.sendMessage(player, Component.text("Chunk Loader: ", NamedTextColor.GRAY)
                .append(Component.text(region.getName(), NamedTextColor.GOLD)));
        messageHelper.sendMessage(player, Component.text("World: ", NamedTextColor.GRAY)
                .append(Component.text(region.getWorldName(), NamedTextColor.AQUA)));
        messageHelper.sendMessage(player, Component.text("Chunks: ", NamedTextColor.GRAY)
                .append(Component.text(String.valueOf(region.getChunkCount()), NamedTextColor.YELLOW)));

        String status = region.isEnabled() ? "Enabled" : "Disabled";
        NamedTextColor statusColor = region.isEnabled() ? NamedTextColor.GREEN : NamedTextColor.RED;
        messageHelper.sendMessage(player, Component.text("Status: ", NamedTextColor.GRAY)
                .append(Component.text(status, statusColor)));

        if (region.getChunkCount() > 0) {
            ChunkLoaderRegion.ChunkCoordinate[] corners = getCornerChunks(region);
            messageHelper.sendMessage(player, Component.text("Corner Chunks:", NamedTextColor.GRAY));
            messageHelper.sendMessage(player, Component.text("  ", NamedTextColor.GRAY)
                    .append(Component.text(corners[0].toString(), NamedTextColor.WHITE)));
            messageHelper.sendMessage(player, Component.text("  ", NamedTextColor.GRAY)
                    .append(Component.text(corners[1].toString(), NamedTextColor.WHITE)));
        }
        messageHelper.sendMessage(player, Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GREEN));
    }

    private ChunkLoaderRegion.ChunkCoordinate[] getCornerChunks(ChunkLoaderRegion region) {
        Set<ChunkLoaderRegion.ChunkCoordinate> chunks = region.getChunks();

        int minX = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;

        for (ChunkLoaderRegion.ChunkCoordinate coord : chunks) {
            minX = Math.min(minX, coord.getX());
            minZ = Math.min(minZ, coord.getZ());
            maxX = Math.max(maxX, coord.getX());
            maxZ = Math.max(maxZ, coord.getZ());
        }

        return new ChunkLoaderRegion.ChunkCoordinate[] {
            new ChunkLoaderRegion.ChunkCoordinate(minX, minZ),
            new ChunkLoaderRegion.ChunkCoordinate(maxX, maxZ)
        };
    }

    private String getPlainText(String legacyText) {
        if (legacyText == null) {
            return "";
        }
        Component component = messageHelper.fromLegacy(legacyText);
        if (component instanceof net.kyori.adventure.text.TextComponent) {
            return ((net.kyori.adventure.text.TextComponent) component).content();
        }
        return "";
    }
}
