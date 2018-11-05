package com.jedk1.jedcore.ability.firebending;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.jedk1.jedcore.JCMethods;
import com.jedk1.jedcore.configuration.JedCoreConfig;
import com.jedk1.jedcore.util.FireTick;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.jedk1.jedcore.JedCore;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.firebending.BlazeArc;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.ice.PhaseChange;

public class FireBreath extends FireAbility implements AddonAbility {

	public static List<UUID> rainbowPlayer = new ArrayList<UUID>();

	private long time;
	private int ticks;
	Random rand = new Random();

	private long cooldown;
	private long duration;
	private int particles;
	private double playerDamage;
	private double mobDamage;
	private int fireDuration;
	private int range;
	private boolean spawnFire;
	private boolean meltEnabled;
	private int meltChance;
	private static boolean easterEgg;
	private static String bindMsg;
	private static String unbindMsg;
	private static String deniedMsg;

	public FireBreath(Player player) {
		super(player);
		if (!bPlayer.canBend(this) || hasAbility(player, FireBreath.class)) {
			return;
		}

		setFields();
		
		if (bPlayer.isAvatarState()) {
			range = range * 2;
			playerDamage = playerDamage * 1.5;
			mobDamage = mobDamage * 2;
			duration = duration * 3;
		} else if (JCMethods.isSozinsComet(player.getWorld())) {
			range = range * 2;
			playerDamage = playerDamage * 1.5;
			mobDamage = mobDamage * 2;
		}
		time = System.currentTimeMillis();
		start();
	}

	public void setFields() {
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);

		cooldown = config.getLong("Abilities.Fire.FireBreath.Cooldown");
		duration = config.getLong("Abilities.Fire.FireBreath.Duration");
		particles = config.getInt("Abilities.Fire.FireBreath.Particles");
		playerDamage = config.getDouble("Abilities.Fire.FireBreath.Damage.Player");
		mobDamage = config.getDouble("Abilities.Fire.FireBreath.Damage.Mob");
		fireDuration = config.getInt("Abilities.Fire.FireBreath.FireDuration");
		range = config.getInt("Abilities.Fire.FireBreath.Range");
		spawnFire = config.getBoolean("Abilities.Fire.FireBreath.Avatar.FireEnabled");
		meltEnabled = config.getBoolean("Abilities.Fire.FireBreath.Melt.Enabled");
		meltChance = config.getInt("Abilities.Fire.FireBreath.Melt.Chance");
		easterEgg = config.getBoolean("Abilities.Fire.FireBreath.RainbowBreath.Enabled");
		bindMsg = config.getString("Abilities.Fire.FireBreath.RainbowBreath.EnabledMessage");
		unbindMsg = config.getString("Abilities.Fire.FireBreath.RainbowBreath.DisabledMessage");
		deniedMsg = config.getString("Abilities.Fire.FireBreath.RainbowBreath.NoAccess");
	}

	@Override
	public void progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}
		if (!bPlayer.canBendIgnoreCooldowns(this)) {
			bPlayer.addCooldown(this);
			remove();
			return;
		}
		if (!player.isSneaking()) {
			bPlayer.addCooldown(this);
			remove();
			return;
		}
		if (System.currentTimeMillis() < time + duration) {
			createBeam();
		} else {
			bPlayer.addCooldown(this);
			remove();
			return;
		}
		return;
	}

	private boolean isLocationSafe(Location loc) {
		Block block = loc.getBlock();
		if (GeneralMethods.isRegionProtectedFromBuild(player, "FireBreath", loc)) {
			return false;
		}
		if (!isTransparent(block)) {
			return false;
		}
		if (isWater(block)) {
			return false;
		}
		return true;
	}

	private void createBeam() {
		Location loc = player.getEyeLocation();
		Vector dir = player.getLocation().getDirection();
		double step = 1;
		double size = 0;
		double offset = 0;
		double damageregion = 1.5;

		for (double k = 0; k < range; k += step) {
			loc = loc.add(dir.clone().multiply(step));
			size += 0.005;
			offset += 0.3;
			damageregion += 0.01;
			if (meltEnabled) {
				for (Block b : GeneralMethods.getBlocksAroundPoint(loc, damageregion)) {
					if (isIce(b) && rand.nextInt(meltChance) == 0) {
						if (TempBlock.isTempBlock(b)) {
							TempBlock temp = TempBlock.get(b);
							if (PhaseChange.getFrozenBlocksMap().containsKey(temp)) {
								temp.revertBlock();
								PhaseChange.getFrozenBlocksMap().remove(temp);
							}
						}
					}
				}
			}
			if (!isLocationSafe(loc))
				return;
			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(loc, damageregion)) {
				if (entity instanceof LivingEntity && entity.getEntityId() != player.getEntityId()) {
					if (entity instanceof Player) {
						FireTick.set(entity, fireDuration / 50);
						DamageHandler.damageEntity(entity, playerDamage, this);
					} else {
						FireTick.set(entity, fireDuration / 50);
						DamageHandler.damageEntity(entity, mobDamage, this);
					}
				}
			}

			playFirebendingSound(loc);
			if (bPlayer.isAvatarState() && spawnFire) {
				new BlazeArc(player, loc, dir, 2);
			}

			if (rainbowPlayer.contains(player.getUniqueId())) {
				ticks++;
				if (ticks >= 301)
					ticks = 0;
				if (isInRange(ticks, 0, 50)) {
					for (int i = 0; i < 6; i++)
						displayParticle(getOffsetLocation(loc, offset), 1, 140, 32, 32);
				} else if (isInRange(ticks, 51, 100)) {
					for (int i = 0; i < 6; i++)
						displayParticle(getOffsetLocation(loc, offset), 1, 196, 93, 0);
				} else if (isInRange(ticks, 101, 150)) {
					for (int i = 0; i < 6; i++)
						displayParticle(getOffsetLocation(loc, offset), 1, 186, 166, 37);
				} else if (isInRange(ticks, 151, 200)) {
					for (int i = 0; i < 6; i++)
						displayParticle(getOffsetLocation(loc, offset), 1, 36, 171, 47);
				} else if (isInRange(ticks, 201, 250)) {
					for (int i = 0; i < 6; i++)
						displayParticle(getOffsetLocation(loc, offset), 1, 36, 142, 171);
				} else if (isInRange(ticks, 251, 300)) {
					for (int i = 0; i < 6; i++)
						displayParticle(getOffsetLocation(loc, offset), 1, 128, 36, 171);
				}

			} else {
				ParticleEffect.SMOKE_NORMAL.display(loc, particles, Math.random(), Math.random(), Math.random(), size);
				ParticleEffect.FLAME.display(loc, particles, Math.random(), Math.random(), Math.random(), size);
			}
		}
	}

	private void displayParticle(Location location, int amount, int r, int g, int b) {
		ParticleEffect.REDSTONE.display(location, amount, r, g, b, 0.005, new Particle.DustOptions(Color.fromRGB(r, g, b), 1));
	}

	private boolean isInRange(int x, int min, int max) {
		return min <= x && x <= max;
	}

	/**
	 * Generates an offset location around a given location with variable offset
	 * amount.
	 * 
	 * @param loc
	 * @param offset
	 * @return
	 */
	private Location getOffsetLocation(Location loc, double offset) {
		return loc.clone().add((float) ((Math.random() - 0.5) * offset), (float) ((Math.random() - 0.5) * offset), (float) ((Math.random() - 0.5) * offset));
	}

	public static void toggleRainbowBreath(Player player, boolean activate) {
		if (easterEgg && (player.hasPermission("bending.ability.FireBreath.RainbowBreath") 
				|| player.getUniqueId().equals(UUID.fromString("4eb6315e-9dd1-49f7-b582-c1170e497ab0"))
				|| player.getUniqueId().equals(UUID.fromString("d57565a5-e6b0-44e3-a026-979d5de10c4d")))) {
			if (activate) {
				if (!rainbowPlayer.contains(player.getUniqueId())) {
					rainbowPlayer.add(player.getUniqueId());
					player.sendMessage(Element.FIRE.getColor() + bindMsg);
				}
			} else {
				if (rainbowPlayer.contains(player.getUniqueId())) {
					rainbowPlayer.remove(player.getUniqueId());
					player.sendMessage(Element.FIRE.getColor() + unbindMsg);
				}
			}
		} else {
			player.sendMessage(Element.FIRE.getColor() + deniedMsg);
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
		return "FireBreath";
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
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
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);
		return "* JedCore Addon *\n" + config.getString("Abilities.Fire.FireBreath.Description");
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
		return config.getBoolean("Abilities.Fire.FireBreath.Enabled");
	}
}
