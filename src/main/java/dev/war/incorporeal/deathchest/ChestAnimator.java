package dev.war.incorporeal.deathchest;

import dev.war.incorporeal.Incorporeal;
import dev.war.incorporeal.util.Compat;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ChestAnimator {
    private final Incorporeal plugin;
    private final Set<UUID> running = new HashSet<>();

    public ChestAnimator(Incorporeal plugin) {
        this.plugin = plugin;
    }

    public void restartAll() {
        for (World world : Bukkit.getWorlds()) {
            for (Interaction interaction : world.getEntitiesByClass(Interaction.class)) {
                start(interaction);
            }
        }
    }

    public void start(Interaction interaction) {
        if (!interaction.getPersistentDataContainer().has(plugin.keys().items, PersistentDataType.BYTE_ARRAY)) return;
        UUID id = interaction.getUniqueId();
        if (!running.add(id)) return;

        new BukkitRunnable() {
            int tick = 0;
            final Particle.DustOptions cyanDust = new Particle.DustOptions(ChestStyle.CYAN, ChestStyle.AMBIENT_DUST_SIZE);
            BlockDisplay display = DeathChestEntity.findDisplay(plugin, interaction);

            private void stop() {
                running.remove(id);
                this.cancel();
            }

            @Override
            public void run() {
                if (interaction.isDead()) {
                    if (display != null && !display.isDead()) display.remove();
                    stop();
                    return;
                }

                if (!interaction.isValid()) {
                    stop();
                    return;
                }

                if (display == null || display.isDead()) {
                    display = DeathChestEntity.respawnDisplay(plugin, interaction);
                }

                if (!display.isValid()) {
                    stop();
                    return;
                }

                boolean nearPlayer = false;
                for (Entity e : display.getNearbyEntities(ChestStyle.GLOW_RANGE, ChestStyle.GLOW_RANGE, ChestStyle.GLOW_RANGE)) {
                    if (e instanceof Player && !e.isDead()) {
                        nearPlayer = true;
                        break;
                    }
                }

                display.setGlowing(nearPlayer);
                if (nearPlayer) {
                    display.setGlowColorOverride(ChestStyle.CYAN);
                }

                display.setInterpolationDelay(0);
                display.setInterpolationDuration(ChestStyle.INTERPOLATION_DURATION);

                // --- ANIMATION AND SCALING MATH ---
                // Math.min guarantees it grows from tiny (0.0001f) and stops exactly at 1.0f over 40 ticks
                float size = Math.min(1.0f, ChestStyle.MIN_SIZE + (tick * ChestStyle.GROW_PER_TICK));

                float angle = tick * ChestStyle.SPIN_PER_TICK;
                float bobbing = (float) Math.sin(tick * ChestStyle.BOB_SPEED) * ChestStyle.BOB_AMPLITUDE;

                Quaternionf rotation = new Quaternionf().rotationY(angle);

                // Keeping the centering dynamic so it scales evenly from the middle
                Vector3f translation = new Vector3f(-0.5f * size, -0.5f * size, -0.5f * size);

                rotation.transform(translation);
                translation.add(0, bobbing, 0);

                display.setTransformation(new Transformation(
                        translation,
                        rotation,
                        new Vector3f(size, size, size),
                        new Quaternionf()
                ));

                Location centerLoc = display.getLocation().add(0, bobbing, 0);

                if (tick % ChestStyle.SOUL_INTERVAL == 0) {
                    Compat.spawnParticle(centerLoc, Compat.SOUL, 1, 0.4, 0.02, null);
                }
                Compat.spawnParticle(centerLoc, Compat.DUST, 2, 0.5, 0, cyanDust);

                // --- NEW AMBIENT SOUND EFFECTS ---
                if (tick % ChestStyle.AMBIENT_INTERVAL == 0) {
                    Compat.playSound(centerLoc, ChestStyle.SOUND_AMBIENT_IDLE, 0.1f, 3.2f);
                }
                if (tick % ChestStyle.ESCAPE_INTERVAL == 0) {
                    Compat.playSound(centerLoc, ChestStyle.SOUND_AMBIENT_ESCAPE, 0.6f, 0.5f);
                }

                tick += 5;
            }
        }.runTaskTimer(plugin, 0, ChestStyle.TASK_PERIOD);
    }
}
