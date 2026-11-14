package dev.war.incorporeal;

import dev.war.incorporeal.deathchest.ChestAnimator;
import dev.war.incorporeal.deathchest.ChestKeys;
import dev.war.incorporeal.deathchest.DeathChestListener;
import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Incorporeal extends JavaPlugin {
    private ChestKeys keys;
    private ChestAnimator animator;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        keys = new ChestKeys(this);
        animator = new ChestAnimator(this);

        getServer().getPluginManager().registerEvents(new DeathChestListener(this), this);
        Bukkit.getScheduler().runTask(this, animator::restartAll);

        getLogger().info("Incorporeal enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Incorporeal disabled.");
    }

    public ChestKeys keys() {
        return keys;
    }

    public ChestAnimator animator() {
        return animator;
    }

    public String message(String path, String... replacements) {
        String text = getConfig().getString("messages." + path, "");
        if (text.isEmpty()) return null;
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            text = text.replace("{" + replacements[i] + "}", replacements[i + 1]);
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
