package dev.war.incorporeal.deathchest;

import dev.war.incorporeal.Incorporeal;
import dev.war.incorporeal.util.SerializationUtil;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

public class DeathChestEntity {

    public static void create(Incorporeal plugin, Location location, Player owner, List<ItemStack> items) {
        Location baseLoc = location.getBlock().getLocation().add(0.5, 1.0, 0.5);
        Location visualLoc = baseLoc.clone().add(0, 0.5, 0);

        baseLoc.getWorld().playSound(baseLoc, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.5f);
        baseLoc.getWorld().playSound(baseLoc, Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 1.0f, 0.8f);

        BlockDisplay display = visualLoc.getWorld().spawn(visualLoc, BlockDisplay.class, d -> {
            d.setBlock(Material.ENDER_CHEST.createBlockData());
            d.setBrightness(new Display.Brightness(15, 15));
        });

        Interaction interaction = baseLoc.getWorld().spawn(baseLoc.clone().subtract(0, 0.2, 0), Interaction.class, i -> {
            i.setInteractionWidth(1.2f);
            i.setInteractionHeight(1.5f);

            i.getPersistentDataContainer().set(new NamespacedKey(plugin, "owner_uuid"), PersistentDataType.STRING, owner.getUniqueId().toString());
            i.getPersistentDataContainer().set(new NamespacedKey(plugin, "stored_items"), PersistentDataType.BYTE_ARRAY, SerializationUtil.serializeItems(items));
            i.getPersistentDataContainer().set(new NamespacedKey(plugin, "linked_display"), PersistentDataType.STRING, display.getUniqueId().toString());
        });

        startAnimation(plugin, display, interaction);
    }

    public static void startAnimation(Incorporeal plugin, BlockDisplay display, Interaction interaction) {
        new BukkitRunnable() {
            int tick = 0;
            final Particle.DustOptions cyanDust = new Particle.DustOptions(Color.fromRGB(0, 255, 255), 0.8f);

            @Override
            public void run() {
                if (display.isDead() || interaction.isDead()) {
                    if (!display.isDead()) display.remove();
                    if (!interaction.isDead()) interaction.remove();
                    this.cancel();
                    return;
                }

                if (!display.isValid() || !interaction.isValid()) {
                    this.cancel();
                    return;
                }

                boolean nearPlayer = false;
                for (Entity e : display.getNearbyEntities(8, 8, 8)) {
                    if (e instanceof Player && !e.isDead()) {
                        nearPlayer = true;
                        break;
                    }
                }

                display.setGlowing(nearPlayer);
                if (nearPlayer) {
                    display.setGlowColorOverride(Color.fromRGB(0, 255, 255));
                }

                display.setInterpolationDelay(0);
                display.setInterpolationDuration(5);

                // --- ANIMATION AND SCALING MATH ---
                // Math.min guarantees it grows from tiny (0.0001f) and stops exactly at 1.0f over 40 ticks
                float size = Math.min(1.0f, 0.0001f + (tick * 0.025f));

                float angle = tick * 0.05f;
                float bobbing = (float) Math.sin(tick * 0.1f) * 0.25f;

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

                if (tick % 10 == 0) {
                    centerLoc.getWorld().spawnParticle(Particle.SOUL, centerLoc, 1, 0.4, 0.4, 0.4, 0.02);
                }
                centerLoc.getWorld().spawnParticle(Particle.DUST, centerLoc, 2, 0.5, 0.5, 0.5, 0, cyanDust);

                // --- NEW AMBIENT SOUND EFFECTS ---
                if (tick % 5 == 0) {
                    centerLoc.getWorld().playSound(centerLoc, Sound.BLOCK_CREAKING_HEART_IDLE, 0.1f, 3.2f);
                }
                if (tick % 120 == 0) {
                    centerLoc.getWorld().playSound(centerLoc, Sound.PARTICLE_SOUL_ESCAPE, 0.6f, 0.5f);
                }

                tick += 5;
            }
        }.runTaskTimer(plugin, 0, 5);
    }
}