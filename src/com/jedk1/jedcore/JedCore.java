package com.jedk1.jedcore;

import java.io.IOException;
import java.util.logging.*;

import com.google.common.reflect.ClassPath;
import com.jedk1.jedcore.util.*;
import org.bukkit.plugin.java.JavaPlugin;

import com.jedk1.jedcore.command.Commands;
import com.jedk1.jedcore.configuration.JedCoreConfig;
import com.jedk1.jedcore.listener.AbilityListener;
import com.jedk1.jedcore.listener.CommandListener;
import com.jedk1.jedcore.listener.JCListener;
import com.jedk1.jedcore.scoreboard.BendingBoard;
import com.projectkorra.projectkorra.ability.CoreAbility;
import org.bukkit.scheduler.BukkitRunnable;

public class JedCore extends JavaPlugin {

	public static JedCore plugin;
	public static Logger log;
	public static String dev;
	public static String version;
	
	@Override
	public void onEnable() {
		if (!isJava8orHigher()) {
			getLogger().info("JedCore requires Java 8+! Disabling JedCore...");
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

		JCMethods.registerDisabledWorlds();
		CoreAbility.registerPluginAbilities(plugin, "com.jedk1.jedcore.ability");
		getServer().getPluginManager().registerEvents(new AbilityListener(this), this);
		getServer().getPluginManager().registerEvents(new CommandListener(this), this);
		getServer().getPluginManager().registerEvents(new JCListener(this), this);
		getServer().getPluginManager().registerEvents(new ChiRestrictor(), this);
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new JCManager(this), 0, 1);
		
		BendingBoard.updateOnline();
		new Commands();

		FireTick.loadMethod();

		new BukkitRunnable() {
			@Override
			public void run() {
				JCMethods.registerCombos();
				BendingBoard.loadOtherCooldowns();
				initializeCollisions();
			}
		}.runTaskLater(this, 1);
		
		try {
	        MetricsLite metrics = new MetricsLite(this);
	        metrics.start();
	        log.info("Initialized Metrics.");
	    } catch (IOException e) {
	        log.info("Failed to submit statistics for MetricsLite.");
	    }
	}

	public void initializeCollisions() {
		boolean enabled = this.getConfig().getBoolean("Properties.AbilityCollisions.Enabled");

		if (!enabled) {
			getLogger().info("Collisions disabled.");
			return;
		}

		try {
			ClassPath cp = ClassPath.from(this.getClassLoader());

			for (ClassPath.ClassInfo info : cp.getTopLevelClassesRecursive("com.jedk1.jedcore.ability")) {
				try {
					@SuppressWarnings("unchecked")
					Class<? extends CoreAbility> abilityClass = (Class<? extends CoreAbility>)Class.forName(info.getName());

					if (abilityClass == null) continue;

					CollisionInitializer initializer = new CollisionInitializer<>(abilityClass);
					initializer.initialize();
				} catch (Exception e) {

				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private boolean isSpigot() {
		return plugin.getServer().getVersion().toLowerCase().contains("spigot");
	}
	
	private boolean isJava8orHigher() {
		return Integer.valueOf(System.getProperty("java.version").substring(2, 3)) >= 8;
	}
	
	public void onDisable() {
		RegenTempBlock.revertAll();
		TempFallingBlock.removeAllFallingBlocks();
	}
}
