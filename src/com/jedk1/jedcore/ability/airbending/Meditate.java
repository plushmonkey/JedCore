package com.jedk1.jedcore.ability.airbending;

import com.jedk1.jedcore.JedCore;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.util.ParticleEffect;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Meditate extends AirAbility implements AddonAbility {

	private long time;
	private double startHealth;

	private String unfocusMsg;
	private long warmup;
	private long cooldown;
	private int boostDuration;
	private int particleDensity;
	private boolean lossFocusMessage;
	private int absorptionBoost;
	private int speedBoost;
	private int jumpBoost;

	public Meditate(Player player) {
		super(player);
		if (!bPlayer.canBend(this)) {
			return;
		}

		setFields();
		start();
	}
	
	public void setFields() {
		unfocusMsg = JedCore.plugin.getConfig().getString("Abilities.Air.Meditate.UnfocusMessage");
		lossFocusMessage = JedCore.plugin.getConfig().getBoolean("Abilities.Air.Meditate.LossFocusMessage");
		warmup = JedCore.plugin.getConfig().getLong("Abilities.Air.Meditate.ChargeTime");
		cooldown = JedCore.plugin.getConfig().getLong("Abilities.Air.Meditate.Cooldown");
		boostDuration = JedCore.plugin.getConfig().getInt("Abilities.Air.Meditate.BoostDuration");
		particleDensity = JedCore.plugin.getConfig().getInt("Abilities.Air.Meditate.ParticleDensity");
		absorptionBoost = JedCore.plugin.getConfig().getInt("Abilities.Air.Meditate.AbsorptionBoost");
		speedBoost = JedCore.plugin.getConfig().getInt("Abilities.Air.Meditate.SpeedBoost");
		jumpBoost = JedCore.plugin.getConfig().getInt("Abilities.Air.Meditate.JumpBoost");
		
		time = System.currentTimeMillis();
		startHealth = player.getHealth();
	}

	@Override
	public void progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}
		if (!bPlayer.canBendIgnoreCooldowns(this)) {
			remove();
			return;
		}
		if (player.getHealth() < startHealth) {
			if (lossFocusMessage) {
				player.sendMessage(new StringBuilder().append(Element.AIR.getColor()).append(unfocusMsg).toString());
			}
			remove();
			return;
		}
		if (System.currentTimeMillis() > time + warmup) {
			ParticleEffect.INSTANT_SPELL.display((float) Math.random(), (float) Math.random(), (float) Math.random(), 0.0F, particleDensity, player.getLocation(), 256D);
			if (!player.isSneaking()) {
				bPlayer.addCooldown(this);
				givePlayerBuffs();
				remove();
				return;
			}
			return;
		} else if (player.isSneaking()) {
			ParticleEffect.MOB_SPELL_AMBIENT.display((float) Math.random(), (float) Math.random(), (float) Math.random(), 0.0F, particleDensity, player.getLocation(), 256D);
		} else {
			remove();
			return;
		}
		return;
	}

	private void givePlayerBuffs() {
		if (player.hasPotionEffect(PotionEffectType.SPEED)) {
			player.removePotionEffect(PotionEffectType.SPEED);
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, boostDuration/50, speedBoost - 1));
		} else {
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, boostDuration/50, speedBoost - 1));
		}

		if (player.hasPotionEffect(PotionEffectType.JUMP)) {
			player.removePotionEffect(PotionEffectType.JUMP);
			player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, boostDuration/50, jumpBoost - 1));
		} else {
			player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, boostDuration/50, jumpBoost - 1));
		}

		if (player.hasPotionEffect(PotionEffectType.ABSORPTION)) {
			player.removePotionEffect(PotionEffectType.ABSORPTION);
			player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, boostDuration/50, absorptionBoost - 1));
		} else {
			player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, boostDuration/50, absorptionBoost - 1));
		}
	}
	
	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public String getName() {
		return "Meditate";
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public String getAuthor() {
		return JedCore.dev;
	}

	@Override
	public String getVersion() {
		return JedCore.version;
	}

	@Override
	public String getDescription() {
		return "* JedCore Addon *\n" + JedCore.plugin.getConfig().getString("Abilities.Air.Meditate.Description");
	}

	@Override
	public void load() {
		return;
	}

	@Override
	public void stop() {
		return;
	}
	
	@Override
	public boolean isEnabled() {
		return JedCore.plugin.getConfig().getBoolean("Abilities.Air.Meditate.Enabled");
	}
}
