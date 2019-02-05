package com.jedk1.jedcore.ability.avatar;

import com.jedk1.jedcore.JCMethods;
import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.configuration.JedCoreConfig;
import com.jedk1.jedcore.util.RegenTempBlock;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AvatarAbility;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class SpiritBeam extends AvatarAbility implements AddonAbility {

	private Location location;
	private Vector direction;
	private long duration;
	private long cooldown;
	private double range;
	private boolean avataronly;
	private double damage;
	private boolean blockdamage;
	private long regen;
	private double radius;
	private long time;

	public SpiritBeam(Player player) {
		super(player);
		if (bPlayer.isOnCooldown(this)) {
			return;
		}

		setFields();

		if (avataronly && !bPlayer.isAvatarState()) {
			return;
		}

		time = System.currentTimeMillis();
		start();
	}

	public void setFields() {
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);
		
		duration = config.getInt("Abilities.Avatar.SpiritBeam.Duration");
		cooldown = config.getInt("Abilities.Avatar.SpiritBeam.Cooldown");
		damage = config.getDouble("Abilities.Avatar.SpiritBeam.Damage");
		range = config.getInt("Abilities.Avatar.SpiritBeam.Range");
		avataronly = config.getBoolean("Abilities.Avatar.SpiritBeam.AvatarStateOnly");
		blockdamage = config.getBoolean("Abilities.Avatar.SpiritBeam.BlockDamage.Enabled");
		regen = config.getLong("Abilities.Avatar.SpiritBeam.BlockDamage.Regen");
		radius = config.getDouble("Abilities.Avatar.SpiritBeam.BlockDamage.Radius");
	}

	@Override
	public void progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}
		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			bPlayer.addCooldown(this);
			remove();
			return;
		}
		if (System.currentTimeMillis() > time + duration) {
			bPlayer.addCooldown(this);
			remove();
			return;
		}
		if (!player.isSneaking()) {
			bPlayer.addCooldown(this);
			remove();
			return;
		}
		if (avataronly && !bPlayer.isAvatarState()) {
			bPlayer.addCooldown(this);
			remove();
			return;
		}
		createBeam();
	}

	private void createBeam() {
		location = player.getLocation().add(0, 1.2, 0);
		direction = location.getDirection();
		for (double i = 0; i < range; i += 0.5) {
			location = location.add(direction.multiply(0.5).normalize());

			if (GeneralMethods.isRegionProtectedFromBuild(player, "SpiritBeam", location)) {
				return;
			}

			ParticleEffect.SPELL_WITCH.display(location, 1, 0f, 0f, 0f, 0f);
			ParticleEffect.SPELL_WITCH.display(location, 1, (float) Math.random() / 3, (float) Math.random() / 3, (float) Math.random() / 3, 0f);
			ParticleEffect.BLOCK_CRACK.display(location, 1,(float) Math.random() / 3, (float) Math.random() / 3, (float) Math.random() / 3, 0.1F, Material.NETHER_PORTAL.createBlockData());
			ParticleEffect.BLOCK_CRACK.display(location, 1, direction.getX(), direction.getY(), direction.getZ(), 0.1F, Material.NETHER_PORTAL.createBlockData());

			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 2)) {
				if (entity instanceof LivingEntity && entity.getEntityId() != player.getEntityId() && !(entity instanceof ArmorStand)) {
					entity.setFireTicks(100);
					DamageHandler.damageEntity(entity, damage, this);
				}
			}

			if (location.getBlock().getType().isSolid()) {
				location.getWorld().createExplosion(location, 0F);
				if (blockdamage) {
					//new TempExplosion(player, location.getBlock(), "SpiritBeam", radius, regen, damage, false);
					for (Location loc : GeneralMethods.getCircle(location, (int) radius, 0, false, true, 0)) {
						if (JCMethods.isUnbreakable(loc.getBlock())) continue;
						new RegenTempBlock(loc.getBlock(), Material.AIR, Material.AIR.createBlockData(), regen, false);
					}
				}
				return;
			}
		}
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public String getName() {
		return "SpiritBeam";
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
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);
		return "* JedCore Addon *\n" + config.getString("Abilities.Avatar.SpiritBeam.Description");
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
		return config.getBoolean("Abilities.Avatar.SpiritBeam.Enabled");
	}
}
