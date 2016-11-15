package com.jedk1.jedcore;

import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

import com.jedk1.jedcore.command.Commands;
import com.jedk1.jedcore.configuration.JedCoreConfig;
import com.jedk1.jedcore.listener.AbilityListener;
import com.jedk1.jedcore.listener.CommandListener;
import com.jedk1.jedcore.listener.JCListener;
import com.jedk1.jedcore.scoreboard.BendingBoard;
import com.jedk1.jedcore.util.MetricsLite;
import com.jedk1.jedcore.util.RegenTempBlock;
import com.jedk1.jedcore.util.TempFallingBlock;
import com.jedk1.jedcore.util.UpdateChecker;
import com.projectkorra.projectkorra.ability.CoreAbility;

public class JedCore extends JavaPlugin {

	public static JedCore plugin;
	public static Logger log;
	public static String dev;
	public static String version;
	
	@Override
	public void onEnable() {
		if (!isJava8()) {
			log.info("JedCore requires Java 8! Disabling JedCore...");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		
		plugin = this;
		JedCore.log = this.getLogger();
		new JedCoreConfig(this);
		
		UpdateChecker.fetch();
		
		if (!isSpigot()) {
			log.info("Bukkit detected, JedCore will not function properly.");
		}
		
		dev = this.getDescription().getAuthors().toString().replace("[", "").replace("]", "");
		version = this.getDescription().getVersion();
		
		JCMethods.registerCombos();
		JCMethods.registerDisabledWorlds();
		CoreAbility.registerPluginAbilities(plugin, "com.jedk1.jedcore.ability");
		getServer().getPluginManager().registerEvents(new AbilityListener(this), this);
		getServer().getPluginManager().registerEvents(new CommandListener(this), this);
		getServer().getPluginManager().registerEvents(new JCListener(this), this);
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new JCManager(this), 0, 1);
		
		BendingBoard.updateOnline();
		new Commands();
		
		try {
	        MetricsLite metrics = new MetricsLite(this);
	        metrics.start();
	        log.info("Initialized Metrics.");
	    } catch (IOException e) {
	        log.info("Failed to submit statistics for MetricsLite.");
	    }
	}
	
	private boolean isSpigot() {
		return plugin.getServer().getVersion().toLowerCase().contains("spigot");
	}
	
	private boolean isJava8() {
		return System.getProperty("java.version").equals("1.8");
	}
	
	public void onDisable() {
		RegenTempBlock.revertAll();
		TempFallingBlock.removeAllFallingBlocks();
	}
}
