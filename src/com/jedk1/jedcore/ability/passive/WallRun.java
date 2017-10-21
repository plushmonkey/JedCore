package com.jedk1.jedcore.ability.passive;

import com.jedk1.jedcore.JCMethods;
import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.configuration.JedCoreConfig;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.ParticleEffect.BlockData;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Random;

public class WallRun extends ChiAbility implements AddonAbility {

	private long cooldown;
	private long duration;

	private boolean enabled;
	
	private boolean particles;
	private boolean air;
	private boolean earth;
	private boolean water;
	private boolean fire;
	private boolean chi;

	private List<String> invalid;

	private long time;

	Random rand = new Random();

	public WallRun(Player player) {
		super(player);

		setFields();
		if (!enabled) return;
		
		if (bPlayer.isOnCooldown("WallRun")) return;
		
		if (hasAbility(player, WallRun.class)) {
			((WallRun) getAbility(player, WallRun.class)).remove();
			return;
		}

		if (player.getGameMode().equals(GameMode.SPECTATOR)) {
			return;
		}

		time = System.currentTimeMillis();

		if (isEligible() && !JCMethods.isDisabledWorld(player.getWorld())) {
			start();
		}
	}
	
	public void setFields() {
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);
		
		enabled = config.getBoolean("Abilities.Passives.WallRun.Enabled");
		cooldown = config.getLong("Abilities.Passives.WallRun.Cooldown");
		duration = config.getLong("Abilities.Passives.WallRun.Duration");
		particles = config.getBoolean("Abilities.Passives.WallRun.Particles");
		air = config.getBoolean("Abilities.Passives.WallRun.Air");
		earth = config.getBoolean("Abilities.Passives.WallRun.Earth");
		water = config.getBoolean("Abilities.Passives.WallRun.Water");
		fire = config.getBoolean("Abilities.Passives.WallRun.Fire");
		chi = config.getBoolean("Abilities.Passives.WallRun.Chi");
		invalid = config.getStringList("Abilities.Passives.WallRun.InvalidBlocks");
	}

	private boolean isEligible() {
		if (!player.isSprinting())
			return false;

		if (!bPlayer.isToggled()) {
			return false;
		}

		if (bPlayer.getElements().contains(Element.AIR) && air) {
			return true;
		} else if (bPlayer.getElements().contains(Element.EARTH) && earth) {
			return true;
		} else if (bPlayer.getElements().contains(Element.WATER) && water) {
			return true;
		} else if (bPlayer.getElements().contains(Element.FIRE) && fire) {
			return true;
		} else if (bPlayer.getElements().contains(Element.CHI) && chi) {
			return true;
		}

		return false;
	}

	private boolean isAgainstWall() {
		Location location = player.getLocation();
		if (location.getBlock().getRelative(BlockFace.NORTH).getType().isSolid() && !invalid.contains(location.getBlock().getRelative(BlockFace.NORTH).getType().name())) {
			return true;
		} else if (location.getBlock().getRelative(BlockFace.SOUTH).getType().isSolid() && !invalid.contains(location.getBlock().getRelative(BlockFace.SOUTH).getType().name())) {
			return true;
		} else if (location.getBlock().getRelative(BlockFace.WEST).getType().isSolid() && !invalid.contains(location.getBlock().getRelative(BlockFace.WEST).getType().name())) {
			return true;
		} else if (location.getBlock().getRelative(BlockFace.EAST).getType().isSolid() && !invalid.contains(location.getBlock().getRelative(BlockFace.EAST).getType().name())) {
			return true;
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void progress() {
		if (player.isDead() || !player.isOnline() || player.isOnGround()) {
			remove();
			return;
		}
		if (!isAgainstWall()) {
			remove();
			return;
		}
		if (System.currentTimeMillis() > time + duration) {
			remove();
			return;
		}

		if (System.currentTimeMillis() - time > 50L) {
			bPlayer.addCooldown("WallRun", getCooldown());
		}

		if (particles) {
			ParticleEffect.CRIT.display(player.getLocation(), (float) Math.random(), (float) Math.random(), (float) Math.random(), 0F, 4);
			ParticleEffect.BLOCK_CRACK.display(new BlockData(Material.STONE, (byte) 0), (float) Math.random(), (float) Math.random(), (float) Math.random(), 0.1F, 3, player.getLocation(), 32);
			AirAbility.playAirbendingParticles(player.getLocation(), 5);
		}

		Vector dir = player.getLocation().getDirection();
		dir.multiply(1.15);
		player.setVelocity(dir);
	}
	
	public long getCooldown() {
		return cooldown;
	}
	
	public Location getLocation() {
		return null;
	}
	
	@Override
	public String getName() {
		return "WallRun";
	}
	
	@Override
	public boolean isHiddenAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public boolean isSneakAbility() {
		return false;
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
	   return null;
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
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);
		return config.getBoolean("Abilities.Passives.WallRun.Enabled");
	}
}
