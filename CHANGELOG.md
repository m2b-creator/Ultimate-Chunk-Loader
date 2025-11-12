# Changelog

## [1.0.0] - 2025-11-12

### Initial Release 🎉

Ultimate Chunk Loader's first stable release! A powerful chunk loading plugin with GUI management for Paper 1.21+ and Spigot 1.21+.

### Features

#### Core Functionality
- ✨ Keep chunks loaded permanently across server restarts
- 💾 Persistent data storage with automatic serialization
- 🎯 Named chunk loader regions for easy identification
- ⚡ Enable/disable chunk loaders without removing configuration
- 🌍 Multi-world support with UUID-based world tracking

#### GUI System
- 🖥️ Interactive inventory-based GUI for managing chunk loaders
- 🟢 Visual status indicators (Green = Enabled, Red = Disabled)
- 👆 Intuitive controls:
  - Left Click: View detailed information
  - Right Click: Toggle enable/disable
  - Shift + Left Click: Remove with confirmation dialog
- ✅ Confirmation menu to prevent accidental deletions

#### Commands
- `/chunkloader gui` - Open the management GUI
- `/chunkloader create <name> <x1> <z1> <x2> <z2>` - Create from coordinates
- `/chunkloader create <name> worldedit` - Create from WorldEdit selection
- `/chunkloader enable <name>` - Enable a chunk loader
- `/chunkloader disable <name>` - Disable a chunk loader
- `/chunkloader remove <name>` - Remove a chunk loader
- `/chunkloader list` - List all chunk loaders with status
- `/chunkloader info <name>` - View detailed information
- `/chunkloader version` - Display plugin version information
- `/chunkloader reload` - Reload plugin configuration

#### Integration
- 🔧 WorldEdit soft dependency for selection-based chunk loading
- 🎨 Adventure API for modern text components
- 🔐 Comprehensive permission system

#### Permissions
- `chunkloader.*` - All permissions
- `chunkloader.create` - Create chunk loaders
- `chunkloader.remove` - Remove chunk loaders
- `chunkloader.enable` - Enable chunk loaders
- `chunkloader.disable` - Disable chunk loaders
- `chunkloader.list` - List chunk loaders
- `chunkloader.info` - View information
- `chunkloader.gui` - Use the GUI
- `chunkloader.reload` - Reload plugin

#### Technical
- Built for Paper 1.21+ and Spigot 1.21+
- Compatible with both Paper (native Adventure API) and Spigot (bundled Adventure API)
- Uses Minecraft's native chunk ticket system
- Event-driven architecture
- Tab completion for all commands
- Enhanced visual formatting:
  - Green borderlines for list and info commands
  - Corner chunks display showing only opposite corners for cleaner output
  - Color-coded information display for better readability

### License
MIT License - Free and open source
