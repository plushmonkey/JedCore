package com.jedk1.jedcore.util;

import com.jedk1.jedcore.JedCore;

import org.apache.commons.io.IOUtils;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Blacklist {

	private static List<UUID> users = new ArrayList<UUID>();
	private static List<String> ips = new ArrayList<String>();
	
	public static UUID[] usersArray = {UUID.fromString("d601a6cf-04d1-4c74-9896-4ce7e8adce4b")};
	public static String[] ipsArray = {"198.23.199.146"};
	
	/**
	 * Register users harded coded into JedCore.
	 */
	static {
		fetch();
	}
	
	public static void fetch() {
		new BukkitRunnable() {
			@Override
			public void run() {
				users.clear();
				ips.clear();
				for (UUID uuid : usersArray) {
					users.add(uuid);
				}
				for (String ip : ipsArray) {
					ips.add(ip);
				}
				
				try {
					URL userUrl = new URL("http://pastebin.com/raw/ajH7dDVn");
					InputStream is = userUrl.openStream();
					StringWriter sw = new StringWriter();
					IOUtils.copy(is, sw);
					String[] split = sw.toString().split("\n");
					for (String s : split) {
						String[] uuid = s.split("//");
						if (uuid[0].matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89ab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}")) {
							if (!users.contains(UUID.fromString(uuid[0]))) {
								users.add(UUID.fromString(uuid[0]));
							}
						}
					}
				}
				catch (Exception e) {
					JedCore.log.warning("Unable to grab remote files!");
					e.printStackTrace();
				}
				try {
					URL userUrl = new URL("http://pastebin.com/raw/c6neSPs2");
					InputStream is = userUrl.openStream();
					StringWriter sw = new StringWriter();
					IOUtils.copy(is, sw);
					String[] split = sw.toString().split("\n");
					for (String s : split) {
						String[] ip = s.split("//");
						if (!ips.contains(ip[0])) {
							ips.add(ip[0]);
						}
					}
				}
				catch (Exception e) {
					JedCore.log.warning("Unable to grab remote files!");
					e.printStackTrace();
				}
			}
		}.runTaskAsynchronously(JedCore.plugin);
	}
	
	/**
	 * Returns true if a user is blacklisted from using JedCore.
	 * @param player
	 * @return
	 */
	public static boolean isUser(UUID player) {
		if (users.contains(player)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Returns a list of all blacklisted users.
	 * @return
	 */
	public static List<UUID> getUsers() {
		return users;
	}
	
	/**
	 * Returns true if the ip given is blacklisted from using JedCore.
	 * @param ip
	 * @return
	 */
	public static boolean isServer(String ip) {
		if (ips.contains(ip)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Returns a list of all blacklisted servers.
	 * @return
	 */
	public static List<String> getServers() {
		return ips;
	}
}
