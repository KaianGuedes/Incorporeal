package dev.war.incorporeal.deathchest;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import dev.war.incorporeal.Incorporeal;
import dev.war.incorporeal.util.SerializationUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.UUID;

public class DeathChestListener implements Listener {
    private final Incorporeal plugin;
    private final NamespacedKey itemsKey;
    private final NamespacedKey displayKey;

    public DeathChestListener(Incorporeal plugin) {
        this.plugin = plugin;
        this.itemsKey = new NamespacedKey(plugin, "stored_items");
        this.displayKey = new NamespacedKey(plugin, "linked_display");
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getDrops().isEmpty()) return;

        DeathChestEntity.create(
                plugin,
                event.getEntity().getLocation(),
                event.getEntity(),
                new ArrayList<>(event.getDrops())
        );

        event.getDrops().clear();
    }

    @EventHandler
    public void onEntityAdd(EntityAddToWorldEvent event) {
        if (!(event.getEntity() instanceof Interaction interaction)) return;
        if (!interaction.getPersistentDataContainer().has(itemsKey, PersistentDataType.BYTE_ARRAY)) return;

        String displayUuidStr = interaction.getPersistentDataContainer().get(displayKey, PersistentDataType.STRING);
        if (displayUuidStr != null) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                try {
                    Entity displayEntity = Bukkit.getEntity(UUID.fromString(displayUuidStr));
                    if (displayEntity instanceof BlockDisplay display) {
                        DeathChestEntity.startAnimation(plugin, display, interaction);
                    }
                } catch (IllegalArgumentException ignored) {}
            }, 1L);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Interaction interaction)) return;
        if (!interaction.getPersistentDataContainer().has(itemsKey, PersistentDataType.BYTE_ARRAY)) return;

        byte[] serialized = interaction.getPersistentDataContainer().get(itemsKey, PersistentDataType.BYTE_ARRAY);
        ItemStack[] items = SerializationUtil.deserializeItems(serialized);

        Location dropLoc = interaction.getLocation().add(0, 0.5, 0); // Drop items slightly elevated

        for (ItemStack item : items) {
            if (item != null && !item.getType().isAir()) {
                dropLoc.getWorld().dropItemNaturally(dropLoc, item);
            }
        }

        // --- VFX and SFX upon opening ---
        // Cool incorporeal sound combination
        dropLoc.getWorld().playSound(dropLoc, Sound.ENTITY_EVOKER_CAST_SPELL, 1.0f, 1.2f);
        dropLoc.getWorld().playSound(dropLoc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 0.8f);

        // Cyan skull explosion
        dropLoc.getWorld().spawnParticle(Particle.SOUL, dropLoc, 15, 0.3, 0.3, 0.3, 0.1);

        // Cyan dust shockwave
        Particle.DustOptions cyanDust = new Particle.DustOptions(Color.fromRGB(0, 255, 255), 1.5f);
        dropLoc.getWorld().spawnParticle(Particle.DUST, dropLoc, 30, 0.4, 0.4, 0.4, 1.0, cyanDust);
        // --------------------------------

        String displayUuidStr = interaction.getPersistentDataContainer().get(displayKey, PersistentDataType.STRING);
        if (displayUuidStr != null) {
            try {
                Entity display = Bukkit.getEntity(UUID.fromString(displayUuidStr));
                if (display != null) display.remove();
            } catch (IllegalArgumentException ignored) {}
        }

        interaction.remove();
    }
}