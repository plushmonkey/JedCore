package com.jedk1.jedcore.scoreboard;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.jedk1.jedcore.JCMethods;
import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.configuration.Config;
import com.jedk1.jedcore.configuration.JedCoreConfig;
import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;

public class BendingBoard {

	public static ConcurrentHashMap<Player, BendingBoard> boards = new ConcurrentHashMap<Player, BendingBoard>();
	public static List<UUID> disabled = new ArrayList<UUID>();
	public static boolean enabled;
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
		setFields();
	}

	private Player player;
	private SimpleScoreboard scoreboard;

	public BendingBoard(Player player) {
		this.player = player;
		this.scoreboard = new SimpleScoreboard(title);
		boards.put(player, this);
	}

	public static void setFields() {
		enabled = JedCoreConfig.board.getConfig().getBoolean("Settings.Enabled");
		title = ChatColor.translateAlternateColorCodes('&', JedCoreConfig.board.getConfig().getString("Settings.Title"));
		empty = ChatColor.translateAlternateColorCodes('&', JedCoreConfig.board.getConfig().getString("Settings.EmptySlot"));
		combo = ChatColor.translateAlternateColorCodes('&', JedCoreConfig.board.getConfig().getString("Settings.Combos"));
		toggleOn = ChatColor.translateAlternateColorCodes('&', JedCoreConfig.board.getConfig().getString("Settings.Toggle.On"));
		toggleOff = ChatColor.translateAlternateColorCodes('&', JedCoreConfig.board.getConfig().getString("Settings.Toggle.Off"));
		disabledworlds = JedCoreConfig.board.getConfig().getBoolean("Settings.Display.DisabledWorlds");
	}

	public static void updateOnline() {
		if (!enabled) return;
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (disabled.contains(player.getUniqueId())) continue;
			if (!disabledworlds && JCMethods.getDisabledWorlds().contains(player.getWorld().getName())) continue;
			BendingBoard.get(player).update();
		}
	}

	public static void toggle(Player player) {
		if (!enabled) return;
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
		if (!enabled) { 
			if (disabled.contains(player.getUniqueId())) return;
			if (!boards.containsKey(player)) return;
			get(player).remove();
			return;
		}
		if (disabled.contains(player.getUniqueId())) return;
		if (!disabledworlds && JCMethods.getDisabledWorlds().contains(player.getWorld().getName())) {
			if (boards.containsKey(player)) {
				get(player).remove();
			}
			return;
		}
		get(player).update(slot);
	}

	public static BendingBoard get(Player player) {
		if (!enabled) return null;
		if (boards.containsKey(player)) {
			return boards.get(player);
		} else {
			return new BendingBoard(player);
		}
	}
	
	public static boolean isDisabled(Player player) {
		return disabled.contains(player.getUniqueId());
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
		if (!enabled) return;
		new BukkitRunnable() {
			public void run() {
				BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
				if (bPlayer == null) return;

				HashMap<Integer, String> abilities = bPlayer.getAbilities();

				int currentSlot = slot;
				if (currentSlot < 0) {
					currentSlot = player.getInventory().getHeldItemSlot();
				}

				List<String> formatted = new ArrayList<>();

				for (int slotIndex = 1; slotIndex < 10; slotIndex++) {
					CoreAbility currentAbility = CoreAbility.getAbility(abilities.get(slotIndex));
					String currentAbilityName = abilities.get(slotIndex);
					StringBuilder sb = new StringBuilder();

					if (currentSlot == (slotIndex - 1)) {
						sb.append(">");
					}

					if (abilities.containsKey(slotIndex) && currentAbility != null) {
						for (String str : formatted) {
							String stripped = ChatColor.stripColor(str).replace(">", "");

							if (stripped.equalsIgnoreCase(currentAbilityName)) {
								// Add a unique chat color to the beginning, so the ability doesn't override other slots in the map.
								sb.append(ChatColor.values()[slotIndex]);
								break;
							}
						}

						sb.append(currentAbility.getElement().getColor());

						if (bPlayer.isOnCooldown(currentAbilityName)) {
							sb.append(ChatColor.STRIKETHROUGH);
						}

						sb.append(currentAbilityName);
					} else {
						if (abilities.containsKey(slotIndex) && MultiAbilityManager.hasMultiAbilityBound(player)) {
							sb.append(abilities.get(slotIndex));
						} else {
							sb.append(empty.replace("%", String.valueOf(slotIndex)));
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
						formatted.add("" + CoreAbility.getAbility(ability).getElement().getColor() + ChatColor.STRIKETHROUGH + ability);
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
