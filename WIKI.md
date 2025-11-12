# Ultimate Chunk Loader Wiki

## Quick Start

1. Install the plugin in your `plugins/` folder
2. Restart your server
3. Run `/chunkloader gui` to open the management menu

## Creating Chunk Loaders

### Using Coordinates
```
/chunkloader create spawn 0 0 100 100
```
Creates a chunk loader named "spawn" from block coordinates (0,0) to (100,100).

### Using WorldEdit (Requires WorldEdit Plugin)
1. Make a WorldEdit selection (`//wand` or `//pos1` and `//pos2`)
2. Run:
```
/chunkloader create myregion worldedit
```

## GUI Management

Open the GUI with `/chunkloader gui` or `/cl gui`

**Controls:**
- **Left Click** - View detailed information
- **Right Click** - Toggle enable/disable
- **Shift + Left Click** - Remove (with confirmation)

**Color Coding:**
- 🟢 **Green Wool** = Enabled
- 🔴 **Red Wool** = Disabled

## Commands Reference

| Command | Description | Permission |
|---------|-------------|------------|
| `/chunkloader gui` | Open GUI menu | `chunkloader.gui` |
| `/chunkloader create <name> <x1> <z1> <x2> <z2>` | Create from coordinates | `chunkloader.create` |
| `/chunkloader create <name> worldedit` | Create from WorldEdit selection | `chunkloader.create` |
| `/chunkloader enable <name>` | Enable chunk loader | `chunkloader.enable` |
| `/chunkloader disable <name>` | Disable chunk loader | `chunkloader.disable` |
| `/chunkloader remove <name>` | Remove chunk loader | `chunkloader.remove` |
| `/chunkloader list` | List all chunk loaders | `chunkloader.list` |
| `/chunkloader info <name>` | View details | `chunkloader.info` |
| `/chunkloader reload` | Reload plugin | `chunkloader.reload` |

**Aliases:** `/cl` or `/cloader`

## Permissions

Grant all permissions:
```yaml
chunkloader.*
```

Individual permissions:
```yaml
chunkloader.create
chunkloader.remove
chunkloader.enable
chunkloader.disable
chunkloader.list
chunkloader.info
chunkloader.gui
chunkloader.reload
```

**Default:** All permissions default to OP level.

## Configuration

Located at `plugins/ChunkLoader/config.yml`

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

Chunk loaders are saved in `plugins/ChunkLoader/data.yml` and automatically persist across server restarts.

## Use Cases

- **Spawn Areas** - Keep spawn chunks always loaded
- **Farms** - Ensure automatic farms continue running
- **Redstone** - Keep complex redstone circuits active
- **Shops** - Maintain shop regions for item sorters
- **Event Areas** - Prepare areas before players arrive

## FAQ

**Q: Do chunks stay loaded after server restart?**
A: Yes, all chunk loaders are automatically restored.

**Q: Can I temporarily disable a chunk loader?**
A: Yes, use `/chunkloader disable <name>` or right-click in the GUI.

**Q: What's the difference between disable and remove?**
A: Disable keeps the configuration but unloads chunks. Remove deletes it completely.

**Q: How many chunks can I load?**
A: No hard limit, but be mindful of server performance.

**Q: Does this work with other plugins?**
A: Yes, uses Minecraft's native chunk ticket system.

## Troubleshooting

**Chunks not staying loaded:**
- Check the chunk loader is enabled (green in GUI)
- Verify world name matches (case-sensitive)
- Check server logs for errors

**GUI shows no items:**
- Ensure you have chunk loaders created first
- Run `/chunkloader list` to verify they exist
- Update to latest version

**WorldEdit not working:**
- Install WorldEdit as a separate plugin
- Ensure WorldEdit is loaded before ChunkLoader
- Make a valid selection before using the command

## Support

- **GitHub Issues:** Report bugs and request features
- **Documentation:** Check README.md for installation details
- **License:** MIT License - Free to use and modify
