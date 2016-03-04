package com.jedk1.jedcore.command;

import com.jedk1.jedcore.scoreboard.BendingBoard;
import com.projectkorra.projectkorra.command.PKCommand;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class BoardCommand extends PKCommand {
	
	public BoardCommand() {
		super("board", "/bending board", "Toggles the visibility of the BendingBoard.", new String[] { "board", "bendingboard", "bb" });
	}

	@Override
	public void execute(CommandSender sender, List<String> args) {
		if (!isPlayer(sender) || !correctLength(sender, args.size(), 0, 0) || !hasPermission(sender)) {
			return;
		}
		if (args.size() == 0) {
			BendingBoard.toggle((Player) sender);
		} else {
			help(sender, false);
		}
	}
}
