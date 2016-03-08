package com.jedk1.jedcore.util;

import com.google.common.collect.Lists;
import com.jedk1.jedcore.JedCore;
import com.projectkorra.projectkorra.ProjectKorra;

import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

public class UpdateChecker {
	
	public static boolean hasUpdate = false;
	
	public enum Result {
		SUITABLE,
		NOT_SUITABLE,
		SUITABE_UPDATE,
		NOT_SUITABLE_UPDATE,
		UNKNOWN,
		NEWER
	}
	
	public static boolean hasUpdate() {
		return hasUpdate;
	}
	
	public static void fetch() {
		if (!JedCore.plugin.getConfig().getBoolean("Settings.Updater.Check")) {
			JedCore.log.info("JedCore update checker is disabled, skipping compatibility checks.");
			JedCore.log.info("Please enable the update checker to ensure your JedCore version is compatible with the ProjectKorra version you are running.");
			return;
		}
		fetch(new Callback<Result>() {
			@Override
			public void call(Result response) {
				if (response.equals(Result.SUITABLE)) {
					JedCore.log.info("No updates found.");
				}
				if (response.equals(Result.NOT_SUITABLE)) {
					JedCore.log.info("Warning: JedCore version is not compatible with this version of ProjectKorra. "
							+ "Disabling JedCore...");
					JedCore.plugin.getServer().getPluginManager().disablePlugin(JedCore.plugin);
					return;
				}
				if (response.equals(Result.NOT_SUITABLE_UPDATE)) {
					JedCore.log.info("Warning: JedCore version is not compatible with this version of ProjectKorra. "
							+ "Disabling JedCore...");
					JedCore.log.info("Update: An update is available for JedCore!");
					JedCore.log.info("http://projectkorra.com/resources/jedcore.125/");
					JedCore.plugin.getServer().getPluginManager().disablePlugin(JedCore.plugin);
					return;
				}
				if (response.equals(Result.SUITABE_UPDATE)) {
					JedCore.log.info("Update: An update is available for JedCore!");
					JedCore.log.info("http://projectkorra.com/resources/jedcore.125/");
					hasUpdate = true;
					return;
				}
				if (response.equals(Result.UNKNOWN)) {
					JedCore.log.info("Warning: Unknown version of ProjectKorra detected.");
					return;
				}
				if (response.equals(Result.NEWER)) {
					JedCore.log.info("Unknown JedCore version detected. JedCore version is newer than latest version fetched. "
							+ "Assuming build is safe to run.");
					return;
				}
			}
		});
	}
	
	public interface Callback<T> {
		public void call(T response);
	}

	public static void fetch(final Callback<Result> callback) {
		final String pkVersion = ProjectKorra.plugin.getDescription().getVersion();
		final String jcVersion = JedCore.plugin.getDescription().getVersion();
		new BukkitRunnable() {
			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				try {
					URL userUrl = new URL("http://pastebin.com/raw/2Qfut1Tj");
					InputStream is = userUrl.openStream();
					JSONParser parser = new JSONParser();
					JSONObject json = (JSONObject) parser.parse(new InputStreamReader(is, "UTF-8"));
					Result result = Result.UNKNOWN;
					if (json.containsKey(pkVersion)) {
						List<String> compat = (List<String>) json.get(pkVersion);
						if (compat.contains(jcVersion)) {
							result = Result.SUITABLE;
						} else {
							String latestVersion = compat.get(compat.size() - 1).replaceAll("[^\\d.]", "").replaceAll("\\.", "");
							String numericJC = jcVersion.replaceAll("[^\\d.]", "").replaceAll("\\.", "");
							if (Integer.valueOf(numericJC) > Integer.valueOf(latestVersion)) {
								result = Result.NEWER;
							} else {
								result = Result.NOT_SUITABLE;
							}
						}
						List<String> versions = Lists.newArrayList(json.keySet());
						if (versions.indexOf(pkVersion) < (versions.size() - 1)) {
							result = (result == Result.SUITABLE) ? Result.SUITABE_UPDATE : Result.NOT_SUITABLE_UPDATE;
						}
						if (result.equals(Result.SUITABLE) && compat.contains(jcVersion) && compat.indexOf(jcVersion) < compat.size() - 1) {
							result = Result.SUITABE_UPDATE;
						}
					}
					callback.call(result);
				}
				catch (Exception e) {
					JedCore.log.warning("Unable to check JedCore compatibility and updates!");
					e.printStackTrace();
				}
			}
		}.runTaskAsynchronously(JedCore.plugin);
	}
}
