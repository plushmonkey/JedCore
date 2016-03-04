package com.jedk1.jedcore.ability.avatar.elementsphere;

import com.jedk1.jedcore.JCMethods;
import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.util.RegenTempBlock;
import com.jedk1.jedcore.util.TempFallingBlock;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AvatarAbility;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

/**
 * @author jedk1
 * @author Finn_Bueno_
 */
public class ESStream extends AvatarAbility implements AddonAbility {

	private long cooldown;
	private double knockback;
	private double range;
	private double damage;
	private boolean cancelAbility;
	private int requiredUses;

	private double radius;
	private long regen;

	private Location stream;
	private Location origin;
	private Vector dir;

	private int an;
	Random rand = new Random();

	public ESStream(Player player) {
		super(player);
		if (!hasAbility(player, ElementSphere.class)) {
			return;
		}
		ElementSphere currES = getAbility(player, ElementSphere.class);
		if (!bPlayer.canBendIgnoreBindsCooldowns(this) || bPlayer.isOnCooldown("ESStream")) {
			return;
		}
		setFields();
		
		if (currES.getAirUses() < requiredUses 
				|| currES.getEarthUses() < requiredUses 
				|| currES.getFireUses() < requiredUses 
				|| currES.getWaterUses() < requiredUses) {
			return;
		}
		
		if (cancelAbility) {
			currES.remove();
		} else {
			currES.setAirUses(currES.getAirUses()-requiredUses);
			currES.setEarthUses(currES.getEarthUses()-requiredUses);
			currES.setFireUses(currES.getFireUses()-requiredUses);
			currES.setWaterUses(currES.getWaterUses()-requiredUses);
		}
		
		bPlayer.addCooldown("ESStream", getCooldown());
		
		stream = player.getEyeLocation();
		origin = player.getEyeLocation();
		dir = player.getEyeLocation().getDirection();
		an = 0;
		
		start();
	}
	
	public void setFields() {
		cooldown = JedCore.plugin.getConfig().getLong("Abilities.Avatar.ElementSphere.Stream.Cooldown");
		range = JedCore.plugin.getConfig().getDouble("Abilities.Avatar.ElementSphere.Stream.Range");
		damage = JedCore.plugin.getConfig().getDouble("Abilities.Avatar.ElementSphere.Stream.Damage");
		knockback = JedCore.plugin.getConfig().getDouble("Abilities.Avatar.ElementSphere.Stream.Knockback");
		requiredUses = JedCore.plugin.getConfig().getInt("Abilities.Avatar.ElementSphere.Stream.RequiredUses");
		cancelAbility = JedCore.plugin.getConfig().getBoolean("Abilities.Avatar.ElementSphere.Stream.EndAbility");
		radius = JedCore.plugin.getConfig().getInt("Abilities.Avatar.ElementSphere.Stream.ImpactCraterSize");
		regen = JedCore.plugin.getConfig().getLong("Abilities.Avatar.ElementSphere.Stream.ImpactRevert");
	}

	@SuppressWarnings("deprecation")
	@Override
	public void progress() {
		if (player == null || !player.isOnline()) {
			remove();
			return;
		}

		if (origin.distance(stream) >= range) {
			remove();
			return;
		}

		if (GeneralMethods.isRegionProtectedFromBuild(player, "ElementSphere", stream)) {
			remove();
			return;
		}

		for (Entity e : GeneralMethods.getEntitiesAroundPoint(stream, 1.5)) {
			if (e instanceof Player && ((Player) e) == player) {
				continue;
			}
			e.setVelocity(dir.normalize().multiply(knockback));
			if (e instanceof LivingEntity) {
				DamageHandler.damageEntity(e, damage, this);
			}
		}

		if (!player.isDead() && hasAbility(player, ElementSphere.class)) {
			Location loc = stream.clone();
			dir = GeneralMethods.getDirection(loc, player.getTargetBlock((HashSet<Material>) null, (int) range).getLocation()).normalize().multiply(1.2);
		}

		stream.add(dir);
		
		if (!isTransparent(stream.getBlock())) {
			List<BlockState> blocks = new ArrayList<BlockState>();
			for (Location loc : GeneralMethods.getCircle(stream, (int) radius, 0, false, true, 0)) {
				if (JCMethods.isUnbreakable(loc.getBlock())) continue;
				blocks.add(loc.getBlock().getState());
				new RegenTempBlock(loc.getBlock(), Material.AIR, (byte) 0, regen, false);
			}
			for (Entity e : GeneralMethods.getEntitiesAroundPoint(stream, radius)) {
				if (e instanceof Player && ((Player) e) == player) {
					continue;
				}
				e.setVelocity(dir.normalize().multiply(knockback));
				if (e instanceof LivingEntity) {
					DamageHandler.damageEntity(e, damage, this);
				}
			}
			ParticleEffect.FLAME.display((float) Math.random(), (float) Math.random(), (float) Math.random(), 0.5f, 20, stream, 257D);
			ParticleEffect.LARGE_SMOKE.display((float) Math.random(), (float) Math.random(), (float) Math.random(), 0.5f, 20, stream, 257D);
			ParticleEffect.FIREWORKS_SPARK.display((float) Math.random(), (float) Math.random(), (float) Math.random(), 0.5f, 20, stream, 257D);
			ParticleEffect.LARGE_SMOKE.display((float) Math.random(), (float) Math.random(), (float) Math.random(), 0.5f, 20, stream, 257D);
			ParticleEffect.EXPLOSION_HUGE.display((float) Math.random(), (float) Math.random(), (float) Math.random(), 0.5f, 5, stream, 257D);
			stream.getWorld().playSound(stream, (rand.nextBoolean()) ? Sound.FIREWORK_LARGE_BLAST : Sound.FIREWORK_LARGE_BLAST2, 1f, 1f);
			stream.getWorld().playSound(stream, (rand.nextBoolean()) ? Sound.FIREWORK_TWINKLE : Sound.FIREWORK_TWINKLE2, 1f, 1f);
			for (BlockState block : blocks) {
				double x = rand.nextDouble() / 3;
				double z = rand.nextDouble() / 3;

				x = (rand.nextBoolean()) ? -x : x;
				z = (rand.nextBoolean()) ? -z : z;

				new TempFallingBlock(block.getLocation().add(0, 1, 0), block.getType(), block.getData().getData(), dir.clone().add(new Vector(x, 0, z)).normalize().multiply(-1), this);
			}
			remove();
			return;
		}
		
		an += 20;
		if (an > 360) {
			an = 0;
		}
		for (int i = 0; i < 4; i++) {
			for (double d = -4; d <= 0; d += .1) {
				if (origin.distance(stream) < d) {
					continue;	
				}
				Location l = stream.clone().add(dir.clone().normalize().multiply(d));
				double r = d * -1 / 5;
				if (r > .75) {
					r = .75;
				}

				Vector ov = GeneralMethods.getOrthogonalVector(dir, an + (90 * i) + d, r);
				Location pl = l.clone().add(ov.clone());
				switch (i) {
					case 0:
						ParticleEffect.FLAME.display(pl, 0.05F, 0.05F, 0.05F, 0.005F, 1);
						break;
					case 1:
						if (rand.nextInt(30) == 0) {
							ParticleEffect.MOB_SPELL.display((float) 255, (float) 255, (float) 255, 0.003F, 0, pl, 257D);
						} else {
							ParticleEffect.MOB_SPELL_AMBIENT.display(pl, 0.05F, 0.05F, 0.05F, 0.005F, 1);
						}
						break;
					case 2:
						GeneralMethods.displayColoredParticle(pl, "06C1FF");
						break;
					case 3:
						GeneralMethods.displayColoredParticle(pl, "754719");
						break;
				}
			}
		}
		return;
	}
	
	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return stream;
	}

	@Override
	public String getName() {
		return "ElementSphere Stream";
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
		return JedCore.plugin.getConfig().getBoolean("Abilities.Avatar.ElementSphere.Enabled");
	}
}
