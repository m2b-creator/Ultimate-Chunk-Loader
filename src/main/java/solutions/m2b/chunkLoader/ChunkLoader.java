package solutions.m2b.chunkLoader;

import org.bukkit.plugin.java.JavaPlugin;

public final class ChunkLoader extends JavaPlugin {
    private ChunkLoaderManager manager;
    private DataStorage dataStorage;
    private MessageHelper messageHelper;

    @Override
    public void onEnable() {
        getLogger().info("ChunkLoader is starting up...");

        saveDefaultConfig();

        messageHelper = new MessageHelper(this);
        manager = new ChunkLoaderManager(this);
        dataStorage = new DataStorage(this);

        loadData();

        ChunkLoaderGUI gui = new ChunkLoaderGUI(this, manager, messageHelper);
        GUIListener guiListener = new GUIListener(this, manager, gui, messageHelper);
        getServer().getPluginManager().registerEvents(guiListener, this);

        ChunkLoaderCommand commandHandler = new ChunkLoaderCommand(this, manager, gui, messageHelper);
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

        if (messageHelper != null) {
            messageHelper.close();
        }

        getLogger().info("ChunkLoader disabled successfully.");
    }

    public void loadData() {
        try {
            manager.deserializeRegions(dataStorage.loadRegions());
        } catch (Exception e) {
            getLogger().warning("Failed to load chunk loader data: " + e.getMessage());
        }
    }

    public void saveData() {
        try {
            dataStorage.saveRegions(manager.serializeRegions());
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
