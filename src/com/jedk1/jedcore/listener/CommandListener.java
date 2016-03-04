package com.jedk1.jedcore.listener;

import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.event.PKCommandEvent;
import com.jedk1.jedcore.event.PKCommandEvent.CommandType;
import com.jedk1.jedcore.scoreboard.BendingBoard;
import com.projectkorra.projectkorra.command.PKCommand;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.UUID;

public class CommandListener implements Listener {

	JedCore plugin;
	String[] cmdaliases = {"/bending", "/bend", "/b", "/pk", "/projectkorra", "/korra", "/mtla", "/tla"};
	String[] developers = {
			"4eb6315e-9dd1-49f7-b582-c1170e497ab0", //jedk1
			"d57565a5-e6b0-44e3-a026-979d5de10c4d" //RockMC
	};
	String[] contributors = {
			"7bb267eb-cf0b-4fb9-a697-27c2a913ed92", //Finn
			"c00ae8aa-499e-418c-b31f-c8cde4dec384" //Vectrix
	};
	String[] conceptdesigners = {
			"15d1a5a7-76ef-49c3-b193-039b27c47e30" //Kiam
	};

	public CommandListener(JedCore plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
		String cmd = event.getMessage().toLowerCase();
		String[] args = cmd.split("\\s+");
		if (Arrays.asList(cmdaliases).contains(args[0]) && args.length >= 2) {
			PKCommandEvent new_event = new PKCommandEvent(event.getPlayer(), args, null);
			for (PKCommand command : PKCommand.instances.values()) {
				if (Arrays.asList(command.getAliases()).contains(args[1].toLowerCase())) {
					new_event = new PKCommandEvent(event.getPlayer(), args, CommandType.getType(command.getName()));
				}
			}
			Bukkit.getServer().getPluginManager().callEvent(new_event);
		}
	}

	CommandType[] types = {CommandType.ADD, CommandType.BIND, CommandType.CHOOSE, CommandType.CLEAR, CommandType.PRESET, CommandType.REMOVE};

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPKCommand(final PKCommandEvent event) {
		new BukkitRunnable() {
			public void run() {
				if (event.getType() != null) {
					if (Arrays.asList(types).contains(event.getType())) {
						Player player = event.getSender();
						if (BendingBoard.isDisabled(player)) return;
						BendingBoard.get(player).update();
					}
					if (event.getType().equals(CommandType.WHO) && event.getSender().hasPermission("bending.command.who")) {
						if (event.getArgs().length == 3) {
							if (Bukkit.getPlayer(event.getArgs()[2]) != null) {
								UUID uuid = Bukkit.getPlayer(event.getArgs()[2]).getUniqueId();
								if (Arrays.asList(developers).contains(uuid.toString())) {
									event.getSender().sendMessage(ChatColor.DARK_AQUA + "JedCore Developer");
								}
								if (Arrays.asList(contributors).contains(uuid.toString())) {
									event.getSender().sendMessage(ChatColor.AQUA + "JedCore Contributor");
								}
								if (Arrays.asList(conceptdesigners).contains(uuid.toString())) {
									event.getSender().sendMessage(ChatColor.AQUA + "JedCore Concept Designer");
								}
							}
						}
						return;
					}
				}
			}
		}.runTaskLater(JedCore.plugin, 1);
	}
}
