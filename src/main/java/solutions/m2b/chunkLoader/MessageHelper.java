package solutions.m2b.chunkLoader;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

public class MessageHelper {
    private final BukkitAudiences audiences;
    private final LegacyComponentSerializer legacySerializer;

    public MessageHelper(ChunkLoader plugin) {
        this.audiences = BukkitAudiences.create(plugin);
        this.legacySerializer = LegacyComponentSerializer.legacySection();
    }

    public void sendMessage(CommandSender sender, Component message) {
        audiences.sender(sender).sendMessage(message);
    }

    public void sendMessage(Player player, Component message) {
        audiences.player(player).sendMessage(message);
    }

    public String toLegacy(Component component) {
        return legacySerializer.serialize(component);
    }

    public Component fromLegacy(String legacy) {
        return legacySerializer.deserialize(legacy);
    }

    public String getInventoryTitle(InventoryView view) {
        return view.getTitle();
    }

    public void close() {
        if (audiences != null) {
            audiences.close();
        }
    }
}
