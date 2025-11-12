package solutions.m2b.chunkLoader;

import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.*;

public class ChunkLoaderRegion {
    private final String name;
    private final UUID worldUUID;
    private final String worldName;
    private final Set<ChunkCoordinate> chunks;
    private boolean enabled;

    public ChunkLoaderRegion(String name, World world, Set<ChunkCoordinate> chunks) {
        this.name = name;
        this.worldUUID = world.getUID();
        this.worldName = world.getName();
        this.chunks = new HashSet<>(chunks);
        this.enabled = true;
    }

    public ChunkLoaderRegion(String name, UUID worldUUID, String worldName, Set<ChunkCoordinate> chunks, boolean enabled) {
        this.name = name;
        this.worldUUID = worldUUID;
        this.worldName = worldName;
        this.chunks = new HashSet<>(chunks);
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public UUID getWorldUUID() {
        return worldUUID;
    }

    public String getWorldName() {
        return worldName;
    }

    public Set<ChunkCoordinate> getChunks() {
        return Collections.unmodifiableSet(chunks);
    }

    public int getChunkCount() {
        return chunks.size();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("world-uuid", worldUUID.toString());
        data.put("world-name", worldName);
        data.put("enabled", enabled);

        List<Map<String, Integer>> chunkList = new ArrayList<>();
        for (ChunkCoordinate chunk : chunks) {
            Map<String, Integer> chunkData = new HashMap<>();
            chunkData.put("x", chunk.getX());
            chunkData.put("z", chunk.getZ());
            chunkList.add(chunkData);
        }
        data.put("chunks", chunkList);

        return data;
    }

    @SuppressWarnings("unchecked")
    public static ChunkLoaderRegion deserialize(Map<String, Object> data) {
        String name = (String) data.get("name");
        UUID worldUUID = UUID.fromString((String) data.get("world-uuid"));
        String worldName = (String) data.get("world-name");
        boolean enabled = data.containsKey("enabled") ? (Boolean) data.get("enabled") : true;

        Set<ChunkCoordinate> chunks = new HashSet<>();
        List<Map<String, Integer>> chunkList = (List<Map<String, Integer>>) data.get("chunks");
        for (Map<String, Integer> chunkData : chunkList) {
            int x = chunkData.get("x");
            int z = chunkData.get("z");
            chunks.add(new ChunkCoordinate(x, z));
        }

        return new ChunkLoaderRegion(name, worldUUID, worldName, chunks, enabled);
    }

    public static class ChunkCoordinate {
        private final int x;
        private final int z;

        public ChunkCoordinate(int x, int z) {
            this.x = x;
            this.z = z;
        }

        public ChunkCoordinate(Chunk chunk) {
            this.x = chunk.getX();
            this.z = chunk.getZ();
        }

        public int getX() {
            return x;
        }

        public int getZ() {
            return z;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ChunkCoordinate that = (ChunkCoordinate) o;
            return x == that.x && z == that.z;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, z);
        }

        @Override
        public String toString() {
            return "(" + x + ", " + z + ")";
        }
    }
}
