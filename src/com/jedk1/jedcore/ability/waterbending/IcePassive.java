package com.jedk1.jedcore.ability.waterbending;

import com.jedk1.jedcore.JedCore;
import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.IceAbility;
import com.projectkorra.projectkorra.util.ParticleEffect;

import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class IcePassive {

	private static int speedFactor = JedCore.plugin.getConfig().getInt("Abilities.Water.Ice.Passive.SkateSpeedFactor");

	@SuppressWarnings("deprecation")
	public static void handleSkating() {
		for (Player player: Bukkit.getServer().getOnlinePlayers()) {
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
			if (bPlayer != null && bPlayer.canIcebend() && bPlayer.isElementToggled(Element.WATER) == true && bPlayer.hasElement(Element.WATER)) {
				if (player.isSprinting() && IceAbility.isIce(player.getLocation().getBlock().getRelative(BlockFace.DOWN)) && player.isOnGround()) {
					ParticleEffect.SNOW_SHOVEL.display((float) Math.random()/2, (float) Math.random()/2, (float) Math.random()/2, 0F, 15, player.getLocation().clone().add(0, 0.2, 0), 257D);
					player.removePotionEffect(PotionEffectType.SPEED);
					player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, speedFactor));	
				}
			}
		}
	}
}
