package dev.war.incorporeal.util;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.SoundCategory;

public final class Compat {
    public static final Particle DUST = particle("DUST", "REDSTONE");
    public static final Particle SOUL = particle("SOUL");

    private Compat() {
    }

    private static Particle particle(String... names) {
        for (String name : names) {
            try {
                Object value = Particle.class.getField(name).get(null);
                if (value instanceof Particle found) return found;
            } catch (ReflectiveOperationException | LinkageError ignored) {
            }
        }
        return null;
    }

    public static void playSound(Location loc, String key, float volume, float pitch) {
        loc.getWorld().playSound(loc, key, SoundCategory.MASTER, volume, pitch);
    }

    public static void spawnParticle(Location loc, Particle particle, int count, double offset, double speed, Object data) {
        if (particle == null) return;
        loc.getWorld().spawnParticle(particle, loc, count, offset, offset, offset, speed, data);
    }
}
