package solutions.m2b.chunkLoader;

import org.bukkit.plugin.java.JavaPlugin;

public final class ChunkLoader extends JavaPlugin {
    private ChunkLoaderManager manager;
    private DataStorage dataStorage;

    @Override
    public void onEnable() {
        getLogger().info("ChunkLoader is starting up...");

        saveDefaultConfig();

        manager = new ChunkLoaderManager(this);
        dataStorage = new DataStorage(this);

        loadData();

        ChunkLoaderGUI gui = new ChunkLoaderGUI(this, manager);
        GUIListener guiListener = new GUIListener(this, manager, gui);
        getServer().getPluginManager().registerEvents(guiListener, this);

        ChunkLoaderCommand commandHandler = new ChunkLoaderCommand(this, manager, gui);
        getCommand("chunkloader").setExecutor(commandHandler);
        getCommand("chunkloader").setTabCompleter(commandHandler);

        manager.loadAllRegions();

        if (getServer().getPluginManager().getPlugin("WorldEdit") != null) {
            getLogger().info("WorldEdit detected! Selection-based chunk loading is enabled.");
        } else {
            getLogger().info("WorldEdit not found. Only coordinate-based chunk loading is available.");
        }

        getLogger().info("ChunkLoader enabled successfully! Loaded " + manager.getAllRegions().size() + " chunk loaders.");
    }

    @Override
    public void onDisable() {
        if (manager != null) {
            manager.unloadAllRegions();
            saveData();
        }

        getLogger().info("ChunkLoader disabled successfully.");
    }

    public void loadData() {
        try {
            manager.deserializeRegions(dataStorage.loadRegions());
            getLogger().info("Loaded chunk loader data from file.");
        } catch (Exception e) {
            getLogger().warning("Failed to load chunk loader data: " + e.getMessage());
        }
    }

    public void saveData() {
        try {
            dataStorage.saveRegions(manager.serializeRegions());
            getLogger().info("Saved chunk loader data to file.");
        } catch (Exception e) {
            getLogger().severe("Failed to save chunk loader data: " + e.getMessage());
        }
    }

    public void reloadPlugin() {
        manager.unloadAllRegions();
        reloadConfig();
        loadData();
        manager.loadAllRegions();
    }
}
