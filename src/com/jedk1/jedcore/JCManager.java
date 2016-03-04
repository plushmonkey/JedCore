 package com.jedk1.jedcore;

import com.jedk1.jedcore.ability.firebending.FirePunch;
import com.jedk1.jedcore.ability.firebending.FireShots;
import com.jedk1.jedcore.ability.firebending.LightningBurst;
import com.jedk1.jedcore.ability.waterbending.HealingWaters;
import com.jedk1.jedcore.ability.waterbending.IcePassive;
import com.jedk1.jedcore.util.RegenTempBlock;
import com.jedk1.jedcore.util.TempFallingBlock;

import org.bukkit.Bukkit;

public class JCManager implements Runnable {

	public JedCore plugin;
	
	public JCManager(JedCore plugin) {
		this.plugin = plugin;
	}
	
	public void run() {
		FirePunch.display(Bukkit.getServer());
		FireShots.progressFireShots();
		LightningBurst.progressAll();
		
		HealingWaters.heal(Bukkit.getServer());
		IcePassive.handleSkating();
		//IceWall.progressAll();
		
		RegenTempBlock.manage();
		TempFallingBlock.manage();
	}
}