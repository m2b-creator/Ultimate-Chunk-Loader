package solutions.m2b.chunkLoader;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkLoaderManager {
    private final Plugin plugin;
    private final Map<String, ChunkLoaderRegion> regions;

    public ChunkLoaderManager(Plugin plugin) {
        this.plugin = plugin;
        this.regions = new ConcurrentHashMap<>();
    }

    public boolean addRegion(ChunkLoaderRegion region) {
        if (regions.containsKey(region.getName())) {
            return false;
        }

        regions.put(region.getName(), region);
        if (region.isEnabled()) {
            loadRegionChunks(region);
        }
        return true;
    }

    public boolean removeRegion(String name) {
        ChunkLoaderRegion region = regions.remove(name);
        if (region == null) {
            return false;
        }

        unloadRegionChunks(region);
        return true;
    }

    public ChunkLoaderRegion getRegion(String name) {
        return regions.get(name);
    }

    public Collection<ChunkLoaderRegion> getAllRegions() {
        return Collections.unmodifiableCollection(regions.values());
    }

    public boolean regionExists(String name) {
        return regions.containsKey(name);
    }

    public boolean enableRegion(String name) {
        ChunkLoaderRegion region = regions.get(name);
        if (region == null) {
            return false;
        }

        if (region.isEnabled()) {
            return false;
        }

        region.setEnabled(true);
        loadRegionChunks(region);
        return true;
    }

    public boolean disableRegion(String name) {
        ChunkLoaderRegion region = regions.get(name);
        if (region == null) {
            return false;
        }

        if (!region.isEnabled()) {
            return false;
        }

        region.setEnabled(false);
        unloadRegionChunks(region);
        return true;
    }

    public void loadAllRegions() {
        for (ChunkLoaderRegion region : regions.values()) {
            if (region.isEnabled()) {
                loadRegionChunks(region);
            }
        }
    }

    public void unloadAllRegions() {
        for (ChunkLoaderRegion region : regions.values()) {
            unloadRegionChunks(region);
        }
    }

    private void loadRegionChunks(ChunkLoaderRegion region) {
        World world = Bukkit.getWorld(region.getWorldUUID());
        if (world == null) {
            plugin.getLogger().warning("Cannot load chunks for region '" + region.getName() + "': World not found");
            return;
        }

        for (ChunkLoaderRegion.ChunkCoordinate coord : region.getChunks()) {
            Chunk chunk = world.getChunkAt(coord.getX(), coord.getZ());
            chunk.addPluginChunkTicket(plugin);
            chunk.setForceLoaded(true);
        }

        plugin.getLogger().info("Loaded " + region.getChunkCount() + " chunks for region '" + region.getName() + "'");
    }

    private void unloadRegionChunks(ChunkLoaderRegion region) {
        World world = Bukkit.getWorld(region.getWorldUUID());
        if (world == null) {
            return;
        }

        for (ChunkLoaderRegion.ChunkCoordinate coord : region.getChunks()) {
            Chunk chunk = world.getChunkAt(coord.getX(), coord.getZ());
            chunk.removePluginChunkTicket(plugin);
            chunk.setForceLoaded(false);
        }

        plugin.getLogger().info("Unloaded " + region.getChunkCount() + " chunks for region '" + region.getName() + "'");
    }

    public List<Map<String, Object>> serializeRegions() {
        List<Map<String, Object>> data = new ArrayList<>();
        for (ChunkLoaderRegion region : regions.values()) {
            data.add(region.serialize());
        }
        return data;
    }

    public void deserializeRegions(List<Map<String, Object>> data) {
        regions.clear();
        for (Map<String, Object> regionData : data) {
            ChunkLoaderRegion region = ChunkLoaderRegion.deserialize(regionData);
            regions.put(region.getName(), region);
        }
    }
}
