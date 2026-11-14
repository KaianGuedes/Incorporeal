package dev.war.incorporeal.deathchest;

import dev.war.incorporeal.Incorporeal;
import dev.war.incorporeal.util.Compat;
import dev.war.incorporeal.util.SerializationUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;

public class DeathChestListener implements Listener {
    private final Incorporeal plugin;
    private final ChestKeys keys;

    public DeathChestListener(Incorporeal plugin) {
        this.plugin = plugin;
        this.keys = plugin.keys();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getDrops().isEmpty()) return;

        boolean stored = DeathChestEntity.create(
                plugin,
                event.getEntity().getLocation(),
                event.getEntity(),
                new ArrayList<>(event.getDrops())
        );

        if (stored) event.getDrops().clear();
    }

    @EventHandler
    public void onEntitiesLoad(EntitiesLoadEvent event) {
        for (Entity entity : event.getEntities()) {
            if (entity instanceof Interaction interaction) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.animator().start(interaction), 1L);
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!(event.getRightClicked() instanceof Interaction interaction)) return;
        PersistentDataContainer pdc = interaction.getPersistentDataContainer();
        if (!pdc.has(keys.items, PersistentDataType.BYTE_ARRAY)) return;
        if (pdc.has(keys.opened, PersistentDataType.BYTE)) return;

        Player player = event.getPlayer();
        if (!canOpen(player, pdc)) {
            send(player, plugin.message("not-owner"));
            return;
        }

        ItemStack[] items;
        try {
            items = SerializationUtil.deserializeItems(
                    pdc.get(keys.items, PersistentDataType.BYTE_ARRAY),
                    pdc.get(keys.format, PersistentDataType.INTEGER));
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not read a death chest, its items were kept", e);
            send(player, plugin.message("unreadable-items"));
            return;
        }

        pdc.set(keys.opened, PersistentDataType.BYTE, (byte) 1);

        Location dropLoc = interaction.getLocation().add(0, 0.5, 0); // Drop items slightly elevated

        for (ItemStack item : items) {
            if (item != null && !item.getType().isAir()) {
                dropLoc.getWorld().dropItemNaturally(dropLoc, item);
            }
        }

        // --- VFX and SFX upon opening ---
        // Cool incorporeal sound combination
        Compat.playSound(dropLoc, ChestStyle.SOUND_OPEN_CAST, 1.0f, 1.2f);
        Compat.playSound(dropLoc, ChestStyle.SOUND_OPEN_CHIME, 1.0f, 0.8f);

        // Cyan skull explosion
        Compat.spawnParticle(dropLoc, Compat.SOUL, 15, 0.3, 0.1, null);

        // Cyan dust shockwave
        Particle.DustOptions cyanDust = new Particle.DustOptions(ChestStyle.CYAN, ChestStyle.OPEN_DUST_SIZE);
        Compat.spawnParticle(dropLoc, Compat.DUST, 30, 0.4, 1.0, cyanDust);
        // --------------------------------

        String displayUuidStr = pdc.get(keys.display, PersistentDataType.STRING);
        if (displayUuidStr != null) {
            try {
                Entity display = Bukkit.getEntity(UUID.fromString(displayUuidStr));
                if (display != null) display.remove();
            } catch (IllegalArgumentException ignored) {}
        }

        interaction.remove();
    }

    private boolean canOpen(Player player, PersistentDataContainer pdc) {
        String owner = pdc.get(keys.owner, PersistentDataType.STRING);
        if (owner == null || owner.equals(player.getUniqueId().toString())) return true;
        if (plugin.getConfig().getBoolean("open-access.others-can-open", false)) return true;

        long minutes = plugin.getConfig().getLong("open-access.open-to-all-after-minutes", 0);
        Long created = pdc.get(keys.created, PersistentDataType.LONG);
        return minutes > 0 && created != null && System.currentTimeMillis() - created >= minutes * 60_000L;
    }

    private void send(Player player, String message) {
        if (message != null) player.sendMessage(message);
    }
}
