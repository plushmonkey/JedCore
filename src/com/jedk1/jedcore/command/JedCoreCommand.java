package com.jedk1.jedcore.command;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.jedk1.jedcore.JedCore;
import com.projectkorra.projectkorra.command.PKCommand;

public class JedCoreCommand extends PKCommand {
	private static final String DOWNLOAD_URL = "https://github.com/plushmonkey/JedCore";

	public JedCoreCommand() {
		super("jedcore", "/bending jedcore", "This command will show the statistics and version of JedCore.", new String[] { "jedcore", "jc" });
	}

	@Override
	public void execute(CommandSender sender, List<String> args) {
		if (!correctLength(sender, args.size(), 0, 1) || (!hasPermission(sender) && !isSenderJedCoreDev(sender))) {
			return;
		}
		if (args.size() == 0) {
			sendBuildInfo(sender);
		} else if (args.size() == 1 && (hasPermission(sender, "debug") || isSenderJedCoreDev(sender))) {
			//Dev commands for debugging etc.
			if (args.get(0).equalsIgnoreCase("refresh")) {
				sender.sendMessage(ChatColor.AQUA + "Jedcore refreshed.");
			}
		} else {
			help(sender, false);
		}
	}

	public static void sendBuildInfo(CommandSender sender) {
		sender.sendMessage(ChatColor.GRAY + "Running JedCore Build: " + ChatColor.RED + JedCore.plugin.getDescription().getVersion());
		sender.sendMessage(ChatColor.GRAY + "Developed by: " + ChatColor.RED + JedCore.plugin.getDescription().getAuthors().toString().replace("[", "").replace("]", ""));
		sender.sendMessage(ChatColor.GRAY + "Modified by: " + ChatColor.RED + "plushmonkey");
		sender.sendMessage(ChatColor.GRAY + "URL: " + ChatColor.RED + ChatColor.ITALIC + DOWNLOAD_URL);
	}
	
	private boolean isSenderJedCoreDev(CommandSender sender) {
		UUID[] devs = {
				UUID.fromString("4eb6315e-9dd1-49f7-b582-c1170e497ab0"),
				UUID.fromString("d57565a5-e6b0-44e3-a026-979d5de10c4d")
		};
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (Arrays.asList(devs).contains(player.getUniqueId())) {
				return true;
			}
		}
		return false;
	}
}
