package com.jedk1.jedcore.ability.firebending;

import com.jedk1.jedcore.JCMethods;
import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.util.RegenTempBlock;
import com.jedk1.jedcore.util.TempFallingBlock;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
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
import java.util.List;
import java.util.Random;

public class FireComet extends FireAbility implements AddonAbility {

	private long cooldown;
	private long charge;
	private int range;
	private double damage;
	private double blastRadius;
	private boolean cometOnly;
	private boolean avatarBypass;
	private Location location;
	private Location launchLoc;
	private Vector vector;

	private boolean fire;
	private long time;

	private int point;
	
	Random rand = new Random();

	public FireComet(Player player) {
		super(player);
		if (!bPlayer.canBend(this) || hasAbility(player, FireComet.class)) {
			return;
		}
		setFields();
		if (!isSozinsComet(player.getWorld())) {
			if (GeneralMethods.hasRPG() && getSozinsCometOnly()) {
				if (!(bPlayer.isAvatarState() && avatarBypass)) {
					return;
				}
			}
		}
		start();
	}

	public void setFields() {
		if (isSozinsComet(player.getWorld())) {
			cooldown = JedCore.plugin.getConfig().getLong("Abilities.Fire.FireComet.SozinsComet.Cooldown");
			charge = JedCore.plugin.getConfig().getLong("Abilities.Fire.FireComet.SozinsComet.ChargeUp");
			damage = JedCore.plugin.getConfig().getDouble("Abilities.Fire.FireComet.SozinsComet.Damage");
			blastRadius = JedCore.plugin.getConfig().getDouble("Abilities.Fire.FireComet.SozinsComet.BlastRadius");
		} else {
			cooldown = JedCore.plugin.getConfig().getLong("Abilities.Fire.FireComet.Cooldown");
			charge = JedCore.plugin.getConfig().getLong("Abilities.Fire.FireComet.ChargeUp");
			damage = JedCore.plugin.getConfig().getDouble("Abilities.Fire.FireComet.Damage");
			blastRadius = JedCore.plugin.getConfig().getDouble("Abilities.Fire.FireComet.BlastRadius");
		}
		range = JedCore.plugin.getConfig().getInt("Abilities.Fire.FireComet.Range");
		cometOnly = JedCore.plugin.getConfig().getBoolean("Abilities.Fire.FireComet.SozinsCometOnly");
		avatarBypass = JedCore.plugin.getConfig().getBoolean("Abilities.Fire.FireComet.AvatarStateBypassComet");
		time = System.currentTimeMillis();
	}

	@Override
	public void progress() {
		if (player == null || !player.isOnline() || player.isDead()) {
			remove();
			return;
		}
		if (System.currentTimeMillis() > getTime() + getCharge()) {
			if (!isFired()) {
				if (!player.isSneaking()) {
					vector = player.getLocation().getDirection();
					launchLoc = location.clone();
					setFired(true);
				} else {
					location = GeneralMethods.getTargetedLocation(player, 6);
				}
			} else {
				if (!advance()) {
					blast();
					remove();
					return;
				}
			}
			displayComet();
		} else {
			if (!player.isSneaking()) {
				remove();
				return;
			}
			if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
				remove();
				return;
			}
		}
	}

	public boolean advance() {
		location = location.add(vector.multiply(1));
		playFirebendingSound(location);
		if (location.distance(launchLoc) > range || !isTransparent(location.getBlock())) {
			return false;
		}
		for (Entity e : GeneralMethods.getEntitiesAroundPoint(location, 3.0)) {
			if (e instanceof LivingEntity && e.getEntityId() != player.getEntityId()) {
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("deprecation")
	public void blast() {
		List<BlockState> blocks = new ArrayList<BlockState>();
		for (Location loc : GeneralMethods.getCircle(location, (int) blastRadius, 0, false, true, 0)) {
			if (JCMethods.isUnbreakable(loc.getBlock())) continue;
			if (GeneralMethods.isRegionProtectedFromBuild(this, loc)) continue;
			blocks.add(loc.getBlock().getState());
			new RegenTempBlock(loc.getBlock(), Material.AIR, (byte) 0, 10000, false);
		}
		for (Entity e : GeneralMethods.getEntitiesAroundPoint(location, blastRadius)) {
			if (e instanceof Player && ((Player) e) == player) {
				continue;
			}
			if (e instanceof LivingEntity) {
				DamageHandler.damageEntity(e, getDamage(), this);
			}
		}
		ParticleEffect.FLAME.display((float) Math.random(), (float) Math.random(), (float) Math.random(), 0.5f, 20, location, 257D);
		ParticleEffect.LARGE_SMOKE.display((float) Math.random(), (float) Math.random(), (float) Math.random(), 0.5f, 20, location, 257D);
		ParticleEffect.FIREWORKS_SPARK.display((float) Math.random(), (float) Math.random(), (float) Math.random(), 0.5f, 20, location, 257D);
		ParticleEffect.LARGE_SMOKE.display((float) Math.random(), (float) Math.random(), (float) Math.random(), 0.5f, 20, location, 257D);
		location.getWorld().playSound(location, (rand.nextBoolean()) ? Sound.FIREWORK_LARGE_BLAST : Sound.FIREWORK_LARGE_BLAST2, 5f, 1f);
		location.getWorld().playSound(location, Sound.EXPLODE, 5f, 0.8f);
		for (BlockState block : blocks) {
			double x = rand.nextDouble() / 3;
			double z = rand.nextDouble() / 3;

			x = (rand.nextBoolean()) ? -x : x;
			z = (rand.nextBoolean()) ? -z : z;
			
			if (rand.nextInt(5) == 0) {
				new TempFallingBlock(block.getLocation(), block.getType(), block.getData().getData(), vector.clone().add(new Vector(x, 0, z)).normalize().multiply(-1), this);
			}
		}
	}
	
	public void displayComet() {
		for (int angle = 0; angle < 360; angle+=45) {
			for (Location l : JCMethods.getVerticalCirclePoints(location.clone().subtract(0, 1.5, 0), 45, 1.5, angle)) {
				ParticleEffect.FLAME.display(0f, 0f, 0f, 0.05f, 1, l, 257D);
			}
		}
		point++;
		Location location = this.location.clone();
		for (int i = -180; i < 180; i += 45) {
			double angle = (i * Math.PI / 180);
			double xRotation = 3.141592653589793D / 3 * 2.1;
			Vector v = new Vector(Math.cos(angle + point), Math.sin(angle + point), 0.0D).multiply(2.2);
			Vector v1 = v.clone();
			rotateAroundAxisX(v, xRotation);
			rotateAroundAxisY(v, -((location.getYaw() * Math.PI / 180) - 1.575));
			rotateAroundAxisX(v1, -xRotation);
			rotateAroundAxisY(v1, -((location.getYaw() * Math.PI / 180) - 1.575));

			ParticleEffect.FLAME.display(0f, 0f, 0f, 0.02f, 1, location.clone().add(v), 257D);
			ParticleEffect.LARGE_SMOKE.display(0f, 0f, 0f, 0.02f, 1, location.clone().add(v), 257D);
			ParticleEffect.FLAME.display(0f, 0f, 0f, 0.01f, 1, location.clone().add(v1), 257D);
			ParticleEffect.LARGE_SMOKE.display(0f, 0f, 0f, 0.02f, 1, location.clone().add(v1), 257D);
		}
		if (point == 360) {
			point = 0;
		}
	}

	private Vector rotateAroundAxisX(Vector v, double angle) {
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);
		double y = v.getY() * cos - v.getZ() * sin;
		double z = v.getY() * sin + v.getZ() * cos;
		return v.setY(y).setZ(z);
	}

	private Vector rotateAroundAxisY(Vector v, double angle) {
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);
		double x = v.getX() * cos + v.getZ() * sin;
		double z = v.getX() * -sin + v.getZ() * cos;
		return v.setX(x).setZ(z);
	}
	
	@Override
	public void remove() {
		if (player != null && player.isOnline() && isFired()) {
			bPlayer.addCooldown(this);
		}
		super.remove();
	}
	
	public double getDamage() {
		return damage;
	}

	public int getRange() {
		return range;
	}
	
	public void setFired(boolean fire) {
		this.fire = fire;
	}

	public boolean isFired() {
		return fire;
	}

	public long getCharge() {
		return charge;
	}

	public long getTime() {
		return time;
	}

	public boolean getSozinsCometOnly() {
		return cometOnly;
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
		return "FireComet";
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
		return "* JedCore Addon *\n" + JedCore.plugin.getConfig().getString("Abilities.Fire.FireComet.Description");
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
		return JedCore.plugin.getConfig().getBoolean("Abilities.Fire.FireComet.Enabled");
	}
}
