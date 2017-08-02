package com.jedk1.jedcore.ability.airbending;

import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.collision.CollisionDetector;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.airbending.AirSpout;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class AirGlide extends AirAbility implements AddonAbility {

	// The player must touch the ground for the cooldown to start if this is true.
	private boolean requireGround;
	private double speed;
	private double fallSpeed;
	private int particles;
	private boolean airspout;
	private long cooldown;
	private long duration;
	private long lastCooldown;
	private boolean progressing;

	public AirGlide(Player player) {
		super(player);

		if (hasAbility(player, AirGlide.class)) {
			AirGlide ag = getAbility(player, AirGlide.class);
			ag.remove();
			return;
		}

		if (bPlayer.isOnCooldown(this) || CollisionDetector.isOnGround(player)) {
			return;
		}

		setFields();

		this.progressing = true;

		start();
	}

	public void setFields() {
		speed = JedCore.plugin.getConfig().getDouble("Abilities.Air.AirGlide.Speed");
		fallSpeed = JedCore.plugin.getConfig().getDouble("Abilities.Air.AirGlide.FallSpeed");
		particles = JedCore.plugin.getConfig().getInt("Abilities.Air.AirGlide.Particles");
		airspout = JedCore.plugin.getConfig().getBoolean("Abilities.Air.AirGlide.AllowAirSpout");
		cooldown  = JedCore.plugin.getConfig().getLong("Abilities.Air.AirGlide.Cooldown");
		duration  = JedCore.plugin.getConfig().getLong("Abilities.Air.AirGlide.Duration");
		requireGround = JedCore.plugin.getConfig().getBoolean("Abilities.Air.AirGlide.RequireGround") && cooldown > 0;
	}
	
	@SuppressWarnings("deprecation")
	public void progress() {
		long time = System.currentTimeMillis();

		if (this.progressing) {
			update(time);
		} else {
			if (player.isDead() || !player.isOnline()) {
				this.requireGround = false;
				remove();
				return;
			}

			if (CollisionDetector.isOnGround(this.player)) {
				// Flip this so remove() actually removes the instance.
				this.requireGround = false;
				remove();
			} else {
				// Limit how frequently addCooldown is called so bending board isn't spammed with updates.
				if (time > lastCooldown + cooldown / 2) {
					// Keep resetting the cooldown until the player touches the ground.
					bPlayer.addCooldown(this);
					lastCooldown = time;
				}
			}
		}
	}

	private void update(long time) {
		if (this.duration > 0 && time >= this.getStartTime() + this.duration) {
			remove();
			return;
		}

		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}

		if (!hasAbility(player, AirGlide.class)) {
			remove();
			return;
		}

		if ((airspout && hasAbility(player, AirSpout.class)) || !hasAirGlide()) {
			remove();
			return;
		}

		if (!player.isOnGround()) {
			Location firstLocation = player.getEyeLocation();
			Vector directionVector = firstLocation.getDirection().normalize();
			double distanceFromPlayer = speed;
			Vector shootFromPlayer = new Vector(directionVector.getX() * distanceFromPlayer, -fallSpeed, directionVector.getZ() * distanceFromPlayer);
			firstLocation.add(shootFromPlayer.getX(), shootFromPlayer.getY(), shootFromPlayer.getZ());

			player.setVelocity(shootFromPlayer);
			playAirbendingParticles(player.getLocation(), particles);
		} else if (!isTransparent(player.getLocation().getBlock().getRelative(BlockFace.DOWN))) {
			remove();
		}
	}

	@Override
	public void remove() {
		this.progressing = false;
		bPlayer.addCooldown(this);

		if (!this.requireGround) {
			super.remove();
		}
	}

	private boolean hasAirGlide() {
		if (bPlayer.getAbilities().containsValue("AirGlide")) {
			return true;
		}
		return false;
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
		return "AirGlide";
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
		return "* JedCore Addon *\n" + JedCore.plugin.getConfig().getString("Abilities.Air.AirGlide.Description");
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
		return JedCore.plugin.getConfig().getBoolean("Abilities.Air.AirGlide.Enabled");
	}
}
