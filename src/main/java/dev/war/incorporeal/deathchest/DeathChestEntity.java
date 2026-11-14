package dev.war.incorporeal.deathchest;

import dev.war.incorporeal.Incorporeal;
import dev.war.incorporeal.util.Compat;
import dev.war.incorporeal.util.SerializationUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class DeathChestEntity {

    public static boolean create(Incorporeal plugin, Location location, Player owner, List<ItemStack> items) {
        byte[] data;
        try {
            data = SerializationUtil.serializeItems(items);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not store the items of " + owner.getName() + ", leaving them as drops", e);
            return false;
        }

        Location safe = safeLocation(location);
        Location baseLoc = safe.getBlock().getLocation().add(0.5, 1.0, 0.5);
        Location visualLoc = baseLoc.clone().add(0, 0.5, 0);

        Compat.playSound(baseLoc, ChestStyle.SOUND_SPAWN_ACTIVATE, 1.0f, 1.5f);
        Compat.playSound(baseLoc, ChestStyle.SOUND_SPAWN_MIRROR, 1.0f, 0.8f);

        BlockDisplay display = spawnDisplay(visualLoc);

        Interaction interaction = baseLoc.getWorld().spawn(baseLoc.clone().subtract(0, ChestStyle.INTERACTION_DROP, 0), Interaction.class);
        interaction.setInteractionWidth(ChestStyle.INTERACTION_WIDTH);
        interaction.setInteractionHeight(ChestStyle.INTERACTION_HEIGHT);

        PersistentDataContainer pdc = interaction.getPersistentDataContainer();
        pdc.set(plugin.keys().owner, PersistentDataType.STRING, owner.getUniqueId().toString());
        pdc.set(plugin.keys().format, PersistentDataType.INTEGER, SerializationUtil.FORMAT_BUKKIT);
        pdc.set(plugin.keys().created, PersistentDataType.LONG, System.currentTimeMillis());
        pdc.set(plugin.keys().display, PersistentDataType.STRING, display.getUniqueId().toString());
        pdc.set(plugin.keys().items, PersistentDataType.BYTE_ARRAY, data);

        plugin.animator().start(interaction);

        String message = plugin.message("death-chest",
                "x", String.valueOf(baseLoc.getBlockX()),
                "y", String.valueOf(safe.getBlockY()),
                "z", String.valueOf(baseLoc.getBlockZ()),
                "world", safe.getWorld().getName());
        if (message != null) owner.sendMessage(message);
        return true;
    }

    private static BlockDisplay spawnDisplay(Location visualLoc) {
        BlockDisplay d = visualLoc.getWorld().spawn(visualLoc, BlockDisplay.class);
        d.setBlock(Material.ENDER_CHEST.createBlockData());
        d.setBrightness(new Display.Brightness(ChestStyle.BRIGHTNESS, ChestStyle.BRIGHTNESS));
        return d;
    }

    public static BlockDisplay findDisplay(Incorporeal plugin, Interaction interaction) {
        String uuid = interaction.getPersistentDataContainer().get(plugin.keys().display, PersistentDataType.STRING);
        if (uuid == null) return null;
        try {
            Entity entity = Bukkit.getEntity(UUID.fromString(uuid));
            return entity instanceof BlockDisplay display ? display : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static BlockDisplay respawnDisplay(Incorporeal plugin, Interaction interaction) {
        BlockDisplay display = spawnDisplay(interaction.getLocation().add(0, ChestStyle.DISPLAY_RISE, 0));
        interaction.getPersistentDataContainer().set(plugin.keys().display, PersistentDataType.STRING, display.getUniqueId().toString());
        return display;
    }

    private static Location safeLocation(Location location) {
        World world = location.getWorld();
        int minY = world.getMinHeight();
        int maxY = world.getMaxHeight() - 3;
        int x = location.getBlockX();
        int z = location.getBlockZ();
        int y = location.getBlockY();
        if (y >= minY && y <= maxY) return location;

        if (y > maxY) {
            y = maxY;
        } else {
            int top = world.getHighestBlockYAt(x, z);
            y = top >= minY ? top + 1 : world.getSpawnLocation().getBlockY();
        }
        y = Math.max(minY + 1, Math.min(y, maxY));
        while (y < maxY && !(world.getBlockAt(x, y, z).isPassable()
                && world.getBlockAt(x, y + 1, z).isPassable()
                && world.getBlockAt(x, y + 2, z).isPassable())) {
            y++;
        }
        return new Location(world, location.getX(), y, location.getZ(), location.getYaw(), location.getPitch());
    }
}
