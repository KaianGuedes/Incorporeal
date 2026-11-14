package dev.war.incorporeal;

import dev.war.incorporeal.deathchest.DeathChestListener;
import org.bukkit.plugin.java.JavaPlugin;

public class Incorporeal extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new DeathChestListener(this), this);

        getLogger().info("Incorporeal enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Incorporeal disabled.");
    }
}