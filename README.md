# ChunkLoader

A Minecraft Paper plugin that keeps chunks loaded using coordinate-based regions or WorldEdit selections.

## Features

- **Interactive GUI** - Easy-to-use graphical interface for managing chunk loaders
- Keep chunks loaded permanently across server restarts
- Create chunk loaders using coordinates (x1, z1, x2, z2)
- Create chunk loaders from WorldEdit selections (optional)
- Name your chunk loader regions for easy management
- Enable/disable chunk loaders without removing them
- Persistent data storage (survives server restarts)
- Permission-based access control
- List and view information about all chunk loaders with their status

## Building

First, set up the Gradle wrapper:

```bash
gradle wrapper
```

Then build the plugin:

```bash
./gradlew build
```

The compiled plugin JAR will be in `build/libs/ChunkLoader-1.0-SNAPSHOT.jar`

## Installation

1. Download or build the plugin JAR
2. Place it in your server's `plugins/` folder
3. (Optional) Install WorldEdit for selection-based chunk loading
4. Restart your server

## Commands

All commands can be used with `/chunkloader`, `/cl`, or `/cloader`

- `/chunkloader gui` or `/chunkloader menu` - **Open the GUI management menu**
- `/chunkloader create <name> <x1> <z1> <x2> <z2>` - Create a chunk loader using block coordinates
- `/chunkloader create <name> worldedit` - Create a chunk loader from your WorldEdit selection
- `/chunkloader remove <name>` - Remove a chunk loader
- `/chunkloader enable <name>` - Enable a disabled chunk loader
- `/chunkloader disable <name>` - Disable a chunk loader without removing it
- `/chunkloader list` - List all chunk loaders with their status
- `/chunkloader info <name>` - View detailed information about a chunk loader
- `/chunkloader reload` - Reload the plugin configuration and data

## Permissions

- `chunkloader.*` - Access to all commands (default: op)
- `chunkloader.create` - Create chunk loaders (default: op)
- `chunkloader.remove` - Remove chunk loaders (default: op)
- `chunkloader.enable` - Enable chunk loaders (default: op)
- `chunkloader.disable` - Disable chunk loaders (default: op)
- `chunkloader.list` - List chunk loaders (default: op)
- `chunkloader.info` - View chunk loader information (default: op)
- `chunkloader.gui` - Use the GUI menu (default: op)
- `chunkloader.reload` - Reload the plugin (default: op)

## Usage Examples

### Using the GUI (Recommended)

The easiest way to manage your chunk loaders is through the GUI:

```
/chunkloader gui
```

**In the GUI:**
- **Green wool** = Enabled chunk loader
- **Red wool** = Disabled chunk loader
- **Left Click** - View detailed information
- **Right Click** - Toggle enable/disable
- **Shift + Left Click** - Remove (with confirmation)

### Create a chunk loader using coordinates

```
/chunkloader create spawn 0 0 64 64
```

This creates a chunk loader named "spawn" that keeps all chunks loaded between block coordinates (0, 0) and (64, 64).

### Create a chunk loader using WorldEdit selection

1. Make a WorldEdit selection (`//wand`, then left-click and right-click)
2. Run: `/chunkloader create myregion worldedit`

### List all chunk loaders

```
/chunkloader list
```

### View information about a specific chunk loader

```
/chunkloader info spawn
```

### Remove a chunk loader

```
/chunkloader remove spawn
```

### Disable a chunk loader temporarily

```
/chunkloader disable spawn
```

This unloads the chunks but keeps the region configuration saved, so you can re-enable it later.

### Enable a previously disabled chunk loader

```
/chunkloader enable spawn
```

## Configuration

The plugin creates a `config.yml` file in `plugins/ChunkLoader/` with the following options:

```yaml
settings:
  debug: false
  auto-save-interval: 5

messages:
  prefix: "&6[ChunkLoader]&r "
  no-permission: "&cYou don't have permission to do that."
  reload-success: "&aPlugin reloaded successfully!"
```

## Data Storage

Chunk loader data is stored in `plugins/ChunkLoader/data.yml` and persists across server restarts. The plugin automatically saves when:
- A chunk loader is created
- A chunk loader is removed
- The server shuts down

## Requirements

- Paper 1.21+ or compatible fork
- Java 21+
- WorldEdit (optional, for selection-based loading)

## How It Works

The plugin uses Minecraft's chunk ticket system to keep chunks loaded:
- `addPluginChunkTicket()` - Registers the plugin's interest in a chunk
- `setForceLoaded(true)` - Ensures the chunk stays loaded

Chunks remain loaded even when no players are nearby, making this perfect for farms, redstone contraptions, or any automated systems.

## License

This plugin is provided as-is for use on Minecraft servers.
