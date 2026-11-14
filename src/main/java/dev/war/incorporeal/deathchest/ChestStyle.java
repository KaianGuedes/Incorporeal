package dev.war.incorporeal.deathchest;

import org.bukkit.Color;

final class ChestStyle {
    static final Color CYAN = Color.fromRGB(0, 255, 255);

    static final String SOUND_SPAWN_ACTIVATE = "minecraft:block.beacon.activate";
    static final String SOUND_SPAWN_MIRROR = "minecraft:entity.illusioner.mirror_move";
    static final String SOUND_AMBIENT_IDLE = "minecraft:block.creaking_heart.idle";
    static final String SOUND_AMBIENT_ESCAPE = "minecraft:particle.soul_escape";
    static final String SOUND_OPEN_CAST = "minecraft:entity.evoker.cast_spell";
    static final String SOUND_OPEN_CHIME = "minecraft:block.amethyst_block.chime";

    static final int BRIGHTNESS = 15;
    static final float INTERACTION_WIDTH = 1.2f;
    static final float INTERACTION_HEIGHT = 1.5f;
    static final double INTERACTION_DROP = 0.2;
    static final double DISPLAY_RISE = 0.7;

    static final long TASK_PERIOD = 5L;
    static final int INTERPOLATION_DURATION = 5;
    static final double GLOW_RANGE = 8;
    static final float MIN_SIZE = 0.0001f;
    static final float GROW_PER_TICK = 0.025f;
    static final float SPIN_PER_TICK = 0.05f;
    static final float BOB_SPEED = 0.1f;
    static final float BOB_AMPLITUDE = 0.25f;

    static final int SOUL_INTERVAL = 10;
    static final int AMBIENT_INTERVAL = 5;
    static final int ESCAPE_INTERVAL = 120;
    static final float AMBIENT_DUST_SIZE = 0.8f;
    static final float OPEN_DUST_SIZE = 1.5f;

    private ChestStyle() {
    }
}
