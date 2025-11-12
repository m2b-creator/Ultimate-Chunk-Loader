package solutions.m2b.chunkLoader;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.SessionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class ChunkLoaderCommand implements CommandExecutor, TabCompleter {
    private final ChunkLoader plugin;
    private final ChunkLoaderManager manager;
    private final ChunkLoaderGUI gui;
    private final boolean worldEditAvailable;

    public ChunkLoaderCommand(ChunkLoader plugin, ChunkLoaderManager manager, ChunkLoaderGUI gui) {
        this.plugin = plugin;
        this.manager = manager;
        this.gui = gui;
        this.worldEditAvailable = plugin.getServer().getPluginManager().getPlugin("WorldEdit") != null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                return handleCreate(sender, args);
            case "remove":
                return handleRemove(sender, args);
            case "enable":
                return handleEnable(sender, args);
            case "disable":
                return handleDisable(sender, args);
            case "list":
                return handleList(sender);
            case "info":
                return handleInfo(sender, args);
            case "gui":
            case "menu":
                return handleGUI(sender);
            case "version":
            case "ver":
                return handleVersion(sender);
            case "reload":
                return handleReload(sender);
            default:
                sendHelp(sender);
                return true;
        }
    }

    private boolean handleCreate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("This command can only be used by players", NamedTextColor.RED));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("chunkloader.create")) {
            player.sendMessage(Component.text("You don't have permission to create chunk loaders", NamedTextColor.RED));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(Component.text("Usage: /chunkloader create <name> <x1> <z1> <x2> <z2>", NamedTextColor.RED));
            player.sendMessage(Component.text("Or: /chunkloader create <name> worldedit", NamedTextColor.RED));
            return true;
        }

        String name = args[1];

        if (manager.regionExists(name)) {
            player.sendMessage(Component.text("A chunk loader with that name already exists", NamedTextColor.RED));
            return true;
        }

        Set<ChunkLoaderRegion.ChunkCoordinate> chunks;

        if (args.length >= 3 && args[2].equalsIgnoreCase("worldedit")) {
            if (!worldEditAvailable) {
                player.sendMessage(Component.text("WorldEdit is not installed on this server", NamedTextColor.RED));
                return true;
            }

            try {
                chunks = getChunksFromWorldEdit(player);
            } catch (IncompleteRegionException e) {
                player.sendMessage(Component.text("No WorldEdit selection found. Please make a selection first", NamedTextColor.RED));
                return true;
            } catch (Exception e) {
                player.sendMessage(Component.text("Error reading WorldEdit selection: " + e.getMessage(), NamedTextColor.RED));
                return true;
            }
        } else if (args.length >= 6) {
            try {
                int x1 = Integer.parseInt(args[2]);
                int z1 = Integer.parseInt(args[3]);
                int x2 = Integer.parseInt(args[4]);
                int z2 = Integer.parseInt(args[5]);
                chunks = getChunksFromCoordinates(player.getWorld(), x1, z1, x2, z2);
            } catch (NumberFormatException e) {
                player.sendMessage(Component.text("Invalid coordinates provided", NamedTextColor.RED));
                return true;
            }
        } else {
            player.sendMessage(Component.text("Usage: /chunkloader create <name> <x1> <z1> <x2> <z2>", NamedTextColor.RED));
            player.sendMessage(Component.text("Or: /chunkloader create <name> worldedit", NamedTextColor.RED));
            return true;
        }

        ChunkLoaderRegion region = new ChunkLoaderRegion(name, player.getWorld(), chunks);
        manager.addRegion(region);
        plugin.saveData();

        player.sendMessage(Component.text("Created chunk loader '" + name + "' with " + chunks.size() + " chunks", NamedTextColor.GREEN));
        return true;
    }

    private boolean handleRemove(CommandSender sender, String[] args) {
        if (!sender.hasPermission("chunkloader.remove")) {
            sender.sendMessage(Component.text("You don't have permission to remove chunk loaders", NamedTextColor.RED));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /chunkloader remove <name>", NamedTextColor.RED));
            return true;
        }

        String name = args[1];

        if (manager.removeRegion(name)) {
            plugin.saveData();
            sender.sendMessage(Component.text("Removed chunk loader '" + name + "'", NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text("No chunk loader found with that name", NamedTextColor.RED));
        }

        return true;
    }

    private boolean handleEnable(CommandSender sender, String[] args) {
        if (!sender.hasPermission("chunkloader.enable")) {
            sender.sendMessage(Component.text("You don't have permission to enable chunk loaders", NamedTextColor.RED));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /chunkloader enable <name>", NamedTextColor.RED));
            return true;
        }

        String name = args[1];

        if (manager.enableRegion(name)) {
            plugin.saveData();
            sender.sendMessage(Component.text("Enabled chunk loader '" + name + "'", NamedTextColor.GREEN));
        } else {
            ChunkLoaderRegion region = manager.getRegion(name);
            if (region == null) {
                sender.sendMessage(Component.text("No chunk loader found with that name", NamedTextColor.RED));
            } else {
                sender.sendMessage(Component.text("Chunk loader '" + name + "' is already enabled", NamedTextColor.YELLOW));
            }
        }

        return true;
    }

    private boolean handleDisable(CommandSender sender, String[] args) {
        if (!sender.hasPermission("chunkloader.disable")) {
            sender.sendMessage(Component.text("You don't have permission to disable chunk loaders", NamedTextColor.RED));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /chunkloader disable <name>", NamedTextColor.RED));
            return true;
        }

        String name = args[1];

        if (manager.disableRegion(name)) {
            plugin.saveData();
            sender.sendMessage(Component.text("Disabled chunk loader '" + name + "'", NamedTextColor.GREEN));
        } else {
            ChunkLoaderRegion region = manager.getRegion(name);
            if (region == null) {
                sender.sendMessage(Component.text("No chunk loader found with that name", NamedTextColor.RED));
            } else {
                sender.sendMessage(Component.text("Chunk loader '" + name + "' is already disabled", NamedTextColor.YELLOW));
            }
        }

        return true;
    }

    private boolean handleList(CommandSender sender) {
        if (!sender.hasPermission("chunkloader.list")) {
            sender.sendMessage(Component.text("You don't have permission to list chunk loaders", NamedTextColor.RED));
            return true;
        }

        Collection<ChunkLoaderRegion> regions = manager.getAllRegions();

        if (regions.isEmpty()) {
            sender.sendMessage(Component.text("No chunk loaders are currently active", NamedTextColor.YELLOW));
            return true;
        }

        sender.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GREEN));
        sender.sendMessage(Component.text("Chunk Loaders:", NamedTextColor.GOLD));
        for (ChunkLoaderRegion region : regions) {
            String status = region.isEnabled() ? "Enabled" : "Disabled";
            NamedTextColor statusColor = region.isEnabled() ? NamedTextColor.GREEN : NamedTextColor.RED;

            sender.sendMessage(Component.text("  • ", NamedTextColor.GRAY)
                    .append(Component.text(region.getName(), NamedTextColor.GOLD))
                    .append(Component.text(" - ", NamedTextColor.GRAY))
                    .append(Component.text(region.getChunkCount() + " chunks", NamedTextColor.YELLOW))
                    .append(Component.text(" in ", NamedTextColor.GRAY))
                    .append(Component.text(region.getWorldName(), NamedTextColor.AQUA))
                    .append(Component.text(" (", NamedTextColor.GRAY))
                    .append(Component.text(status, statusColor))
                    .append(Component.text(")", NamedTextColor.GRAY)));
        }
        sender.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GREEN));

        return true;
    }

    private boolean handleInfo(CommandSender sender, String[] args) {
        if (!sender.hasPermission("chunkloader.info")) {
            sender.sendMessage(Component.text("You don't have permission to view chunk loader info", NamedTextColor.RED));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /chunkloader info <name>", NamedTextColor.RED));
            return true;
        }

        String name = args[1];
        ChunkLoaderRegion region = manager.getRegion(name);

        if (region == null) {
            sender.sendMessage(Component.text("No chunk loader found with that name", NamedTextColor.RED));
            return true;
        }

        sender.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GREEN));
        sender.sendMessage(Component.text("Chunk Loader: ", NamedTextColor.GRAY)
                .append(Component.text(region.getName(), NamedTextColor.GOLD)));
        sender.sendMessage(Component.text("  World: ", NamedTextColor.GRAY)
                .append(Component.text(region.getWorldName(), NamedTextColor.AQUA)));
        sender.sendMessage(Component.text("  Chunks: ", NamedTextColor.GRAY)
                .append(Component.text(String.valueOf(region.getChunkCount()), NamedTextColor.YELLOW)));

        String status = region.isEnabled() ? "Enabled" : "Disabled";
        NamedTextColor statusColor = region.isEnabled() ? NamedTextColor.GREEN : NamedTextColor.RED;
        sender.sendMessage(Component.text("  Status: ", NamedTextColor.GRAY)
                .append(Component.text(status, statusColor)));

        if (region.getChunkCount() > 0) {
            ChunkLoaderRegion.ChunkCoordinate[] corners = getCornerChunks(region);
            sender.sendMessage(Component.text("  Corner Chunks:", NamedTextColor.GRAY));
            sender.sendMessage(Component.text("    ", NamedTextColor.GRAY)
                    .append(Component.text(corners[0].toString(), NamedTextColor.WHITE)));
            sender.sendMessage(Component.text("    ", NamedTextColor.GRAY)
                    .append(Component.text(corners[1].toString(), NamedTextColor.WHITE)));
        }
        sender.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GREEN));

        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("chunkloader.reload")) {
            sender.sendMessage(Component.text("You don't have permission to reload the plugin", NamedTextColor.RED));
            return true;
        }

        plugin.reloadPlugin();
        sender.sendMessage(Component.text("Plugin reloaded successfully", NamedTextColor.GREEN));
        return true;
    }

    private boolean handleGUI(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("This command can only be used by players", NamedTextColor.RED));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("chunkloader.gui")) {
            player.sendMessage(Component.text("You don't have permission to use the GUI", NamedTextColor.RED));
            return true;
        }

        gui.openMainMenu(player);
        return true;
    }

    private boolean handleVersion(CommandSender sender) {
        String version = plugin.getDescription().getVersion();
        String name = plugin.getDescription().getName();

        sender.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GREEN));
        sender.sendMessage(Component.text(name, NamedTextColor.GOLD)
                .append(Component.text(" v" + version, NamedTextColor.YELLOW)));
        sender.sendMessage(Component.text("A powerful chunk loader plugin", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("GitHub: ", NamedTextColor.GRAY)
                .append(Component.text("github.com/m2b-creator/Ultimate-Chunk-Loader", NamedTextColor.AQUA)));
        sender.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GREEN));
        return true;
    }

    private Set<ChunkLoaderRegion.ChunkCoordinate> getChunksFromCoordinates(World world, int x1, int z1, int x2, int z2) {
        Set<ChunkLoaderRegion.ChunkCoordinate> chunks = new HashSet<>();

        int minX = Math.min(x1, x2);
        int maxX = Math.max(x1, x2);
        int minZ = Math.min(z1, z2);
        int maxZ = Math.max(z1, z2);

        int chunkX1 = minX >> 4;
        int chunkZ1 = minZ >> 4;
        int chunkX2 = maxX >> 4;
        int chunkZ2 = maxZ >> 4;

        for (int x = chunkX1; x <= chunkX2; x++) {
            for (int z = chunkZ1; z <= chunkZ2; z++) {
                chunks.add(new ChunkLoaderRegion.ChunkCoordinate(x, z));
            }
        }

        return chunks;
    }

    private Set<ChunkLoaderRegion.ChunkCoordinate> getChunksFromWorldEdit(Player player) throws IncompleteRegionException {
        Set<ChunkLoaderRegion.ChunkCoordinate> chunks = new HashSet<>();

        SessionManager sessionManager = WorldEdit.getInstance().getSessionManager();
        com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(player.getWorld());
        Region selection = sessionManager.get(BukkitAdapter.adapt(player)).getSelection(world);

        if (selection == null) {
            throw new IncompleteRegionException();
        }

        BlockVector3 min = selection.getMinimumPoint();
        BlockVector3 max = selection.getMaximumPoint();

        int chunkX1 = min.x() >> 4;
        int chunkZ1 = min.z() >> 4;
        int chunkX2 = max.x() >> 4;
        int chunkZ2 = max.z() >> 4;

        for (int x = chunkX1; x <= chunkX2; x++) {
            for (int z = chunkZ1; z <= chunkZ2; z++) {
                chunks.add(new ChunkLoaderRegion.ChunkCoordinate(x, z));
            }
        }

        return chunks;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("ChunkLoader Commands:", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/chunkloader gui - Open management GUI", NamedTextColor.AQUA));
        sender.sendMessage(Component.text("/chunkloader create <name> <x1> <z1> <x2> <z2> - Create from coordinates", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/chunkloader create <name> worldedit - Create from WorldEdit selection", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/chunkloader remove <name> - Remove a chunk loader", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/chunkloader enable <name> - Enable a chunk loader", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/chunkloader disable <name> - Disable a chunk loader", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/chunkloader list - List all chunk loaders", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/chunkloader info <name> - View info about a chunk loader", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/chunkloader version - Show plugin version", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/chunkloader reload - Reload the plugin", NamedTextColor.YELLOW));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filterMatching(args[0], Arrays.asList("gui", "menu", "create", "remove", "enable", "disable", "list", "info", "version", "ver", "reload"));
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("info")
                || args[0].equalsIgnoreCase("enable") || args[0].equalsIgnoreCase("disable"))) {
            List<String> regionNames = new ArrayList<>();
            for (ChunkLoaderRegion region : manager.getAllRegions()) {
                regionNames.add(region.getName());
            }
            return filterMatching(args[1], regionNames);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("create")) {
            if (worldEditAvailable) {
                return filterMatching(args[2], Collections.singletonList("worldedit"));
            }
        }

        return Collections.emptyList();
    }

    private List<String> filterMatching(String prefix, List<String> options) {
        String lowerPrefix = prefix.toLowerCase();
        return options.stream()
                .filter(s -> s.toLowerCase().startsWith(lowerPrefix))
                .sorted()
                .toList();
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
}
