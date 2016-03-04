package com.jedk1.jedcore.scoreboard;

import com.jedk1.jedcore.JCMethods;
import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.configuration.Config;
import com.jedk1.jedcore.configuration.JedCoreConfig;
import com.jedk1.jedcore.util.Blacklist;
import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BendingBoard {

	public static ConcurrentHashMap<Player, BendingBoard> boards = new ConcurrentHashMap<Player, BendingBoard>();
	public static List<UUID> disabled = new ArrayList<UUID>();
	public static List<String> worlds = new ArrayList<String>();
	public static String title;
	public static String empty;
	public static String toggleOn;
	public static String toggleOff;
	public static String combo;
	public static Config toggled;
	public static boolean disabledworlds;

	static {
		toggled = new Config(new File("/board/players.yml"));

		List<String> uuids = toggled.getConfig().getStringList("Players");
		if (uuids != null && !uuids.isEmpty()) {
			if (!uuids.get(0).matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89ab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}")) {
				toggled.getConfig().set("Players", new ArrayList<String>());
				toggled.saveConfig();
			} else {
				for (String s : uuids) {
					disabled.add(UUID.fromString(s));
				}
			}
		}
	}

	private Player player;
	private SimpleScoreboard scoreboard;

	public BendingBoard(Player player) {
		if (Blacklist.isUser(player.getUniqueId())) return;
		this.player = player;
		this.scoreboard = new SimpleScoreboard(title);
		boards.put(player, this);
	}

	public static void setFields() {
		title = ChatColor.translateAlternateColorCodes('&', JedCoreConfig.board.getConfig().getString("Settings.Title"));
		empty = ChatColor.translateAlternateColorCodes('&', JedCoreConfig.board.getConfig().getString("Settings.EmptySlot"));
		combo = ChatColor.translateAlternateColorCodes('&', JedCoreConfig.board.getConfig().getString("Settings.Combos"));
		toggleOn = ChatColor.translateAlternateColorCodes('&', JedCoreConfig.board.getConfig().getString("Settings.Toggle.On"));
		toggleOff = ChatColor.translateAlternateColorCodes('&', JedCoreConfig.board.getConfig().getString("Settings.Toggle.Off"));
		disabledworlds = JedCoreConfig.board.getConfig().getBoolean("Settings.Display.DisabledWorlds");

		worlds.clear();
		List<String> worlds = new ArrayList<String>();
		worlds.addAll(ProjectKorra.plugin.getConfig().getStringList("Properties.DisabledWorlds"));
		if (worlds != null && !worlds.isEmpty()) {
			for (String s : worlds) {
				BendingBoard.worlds.add(s);
			}
		}
	}

	public static void updateOnline() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (disabled.contains(player.getUniqueId())) continue;
			if (!disabledworlds && worlds.contains(player.getWorld().getName())) continue;
			BendingBoard.get(player).update();
		}
	}

	public static void toggle(Player player) {
		List<String> uuids = new ArrayList<String>();
		uuids.addAll(toggled.getConfig().getStringList("Players"));
		if (uuids.contains(player.getUniqueId().toString())) {
			uuids.remove(player.getUniqueId().toString());
			disabled.remove(player.getUniqueId());
			get(player).update();
			player.sendMessage(toggleOn);
		} else {
			uuids.add(player.getUniqueId().toString());
			disabled.add(player.getUniqueId());
			get(player).remove();
			player.sendMessage(toggleOff);
		}
		toggled.getConfig().set("Players", uuids);
		toggled.saveConfig();
	}

	public static void update(Player player) {
		update(player, -1);
	}

	public static void update(Player player, int slot) {
		if (disabled.contains(player.getUniqueId())) return;
		if (!disabledworlds && worlds.contains(player.getWorld().getName())) {
			if (boards.containsKey(player)) {
				get(player).remove();
			}
			return;
		}
		get(player).update(slot);
	}

	public static BendingBoard get(Player player) {
		if (boards.containsKey(player)) {
			return boards.get(player);
		} else {
			return new BendingBoard(player);
		}
	}
	
	public static boolean isDisabled(Player player) {
		return disabled.contains(player.getUniqueId());
	}
	
	public static boolean isDisabledWorld(World world) {
		return worlds.contains(world.getName());
	}

	public void remove() {
		scoreboard.reset();
		boards.remove(player);
		player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
	}

	public void update() {
		update(-1);
	}

	public void update(final int slot) {
		new BukkitRunnable() {
			public void run() {
				int x = slot;
				BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
				if (bPlayer == null) return;
				HashMap<Integer, String> abilities = bPlayer.getAbilities();
				if (x < 0) {
					x = player.getInventory().getHeldItemSlot();
				}
				List<String> formatted = new ArrayList<String>();
				for (int i = 1; i < 10; i++) {
					StringBuilder sb = new StringBuilder();
					if (x == (i - 1))
						sb.append(">");
					if (abilities.containsKey(i) && CoreAbility.getAbility(abilities.get(i)) != null) {
						for (String s : formatted) {
							if (ChatColor.stripColor(s).replace(">", "").equalsIgnoreCase(abilities.get(i))) {
								sb.append(ChatColor.RESET);
							}
						}
						sb.append(CoreAbility.getAbility(abilities.get(i)).getElement().getColor());
						if (bPlayer.isOnCooldown(abilities.get(i))) {
							sb.append(ChatColor.STRIKETHROUGH);
						}
						sb.append(abilities.get(i));
						//if (bPlayer.isOnCooldown(abilities.get(i))) {
						//	sb.append(ChatColor.RESET + " " + -(((System.currentTimeMillis() - bPlayer.getCooldown(abilities.get(i)))/1000) - 1));
						//}
					} else {
						if (abilities.containsKey(i) && MultiAbilityManager.hasMultiAbilityBound(player)) {
							sb.append(abilities.get(i));
						} else {
							sb.append(empty.replace("%", String.valueOf(i)));
						}
					}
					formatted.add(sb.toString());
				}
				boolean combo = false;
				for (String ability : bPlayer.getCooldowns().keySet()) {
					if (JCMethods.getCombos().contains(ability)) {
						if (!combo) {
							formatted.add(BendingBoard.combo);
						}
						combo = true;
						formatted.add( CoreAbility.getAbility(ability).getElement().getColor() + "" + ChatColor.STRIKETHROUGH + ability);
					}
				}
				if (scoreboard.get(-10, "") != null) {
					for (int i = -9; i > -15; i--) {
						scoreboard.remove(i, "");
					}
				}
				for (String s : formatted) {
					scoreboard.add(s, -(formatted.indexOf(s) + 1));
				}
				scoreboard.update();
				scoreboard.send(player);
			}
		}.runTaskLater(JedCore.plugin, 5);
	}
}
