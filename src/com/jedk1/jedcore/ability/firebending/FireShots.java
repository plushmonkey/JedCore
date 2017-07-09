package com.jedk1.jedcore.ability.firebending;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.jedk1.jedcore.util.AirShieldReflector;
import com.jedk1.jedcore.util.FireTick;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.airbending.AirShield;
import com.projectkorra.projectkorra.firebending.util.FireDamageTimer;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.jedk1.jedcore.JedCore;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import org.bukkit.util.Vector;

import static java.util.stream.Collectors.toList;

public class FireShots extends FireAbility implements AddonAbility {
	private List<FireShot> shots = new ArrayList<>();
	private long cooldown;
	private int startAmount;
	private int fireticks;
	private int range;
	private double damage;

	public int amount;
	
	public FireShots(Player player){
		super(player);
		
		if (!bPlayer.canBend(this) || hasAbility(player, FireShots.class)) {
			return;
		}
		
		setFields();
		
		amount = startAmount;
		start();
	}
	
	public void setFields() {
		cooldown = JedCore.plugin.getConfig().getLong("Abilities.Fire.FireShots.Cooldown");
		startAmount = JedCore.plugin.getConfig().getInt("Abilities.Fire.FireShots.FireBalls");
		fireticks = JedCore.plugin.getConfig().getInt("Abilities.Fire.FireShots.FireDuration");
		range = JedCore.plugin.getConfig().getInt("Abilities.Fire.FireShots.Range");
		damage = JedCore.plugin.getConfig().getDouble("Abilities.Fire.FireShots.Damage");
	}
	
	public class FireShot {
		
		private Ability ability;
		private Player player;
		private Location location;
		private int range;
		private int fireticks;
		private double distanceTravelled;
		private double damage;
		private Vector direction = null;
		
		public FireShot(Ability ability, Player player, Location location, int range, int fireticks, double damage) {
			this.ability = ability;
			this.player = player;
			this.location = location;
			this.range = range;
			this.fireticks = fireticks;
			this.damage = damage;
		}
		
		public boolean progress(){
			if(player.isDead() || !player.isOnline()){
				return false;
			}
			if(distanceTravelled >= range){
				return false;
			}
			for(int i = 0; i < 2; i++){
				distanceTravelled ++;
				if(distanceTravelled >= range)
					return false;

				Vector dir = direction;
				if (dir == null) {
					dir = this.player.getLocation().getDirection().clone();
				}

				location = location.add(dir);

				if(GeneralMethods.isSolid(location.getBlock()) || isWater(location.getBlock())){
					return false;
				}
				
				ParticleEffect.SMOKE.display(location, 0.0F, 0.0F, 0.0F, 0.01F, 2);
				ParticleEffect.FLAME.display(location, 0.0F, 0.0F, 0.0F, 0.02F, 5);
				
				for(Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 1.5)){
					if(entity instanceof LivingEntity && entity.getEntityId() != player.getEntityId() && !(entity instanceof ArmorStand)){
						DamageHandler.damageEntity(entity, damage, ability);
						FireTick.set(entity, Math.round(fireticks / 50));
						new FireDamageTimer(entity, player);
						return false;
					}
				}
			}
			return true;
		}
	}

	@Override
	public void progress(){
		if(player.isDead() || !player.isOnline()){
			remove();
			return;
		}
		if (!bPlayer.canBendIgnoreCooldowns(this)) {
			bPlayer.addCooldown(this);
			remove();
			return;
		}

		for (Iterator<FireShot> iterator = shots.iterator(); iterator.hasNext();) {
			FireShot shot = iterator.next();

			if (!shot.progress()) {
				iterator.remove();
			}
		}

		if (amount <= 0 && shots.isEmpty()) {
			remove();
			return;
		}

		if (amount > 0) {
			displayFireBalls();
		}
	}
	
	public static void fireShot(Player player) {
		FireShots fs = getAbility(player, FireShots.class);
		if (fs != null) {
			fs.fireShot();
		}
	}
	
	public void fireShot() {
		if (amount >= 1) {
			if (--amount <= 0) {
				bPlayer.addCooldown(this);
			}
			shots.add(new FireShot(this, player, getRightHandPos(), range, fireticks, damage));
		}
	}

	public Location getRightHandPos(){
		return GeneralMethods.getRightSide(player.getLocation(), .55).add(0, 1.2, 0);
	}

	private void displayFireBalls(){
		ParticleEffect.FLAME.display(getRightHandPos().toVector().add(player.getEyeLocation().getDirection().clone().multiply(.8D)).toLocation(player.getWorld()), 0F, 0F, 0F, 0.01F, 3);
		ParticleEffect.SMOKE.display(getRightHandPos().toVector().add(player.getEyeLocation().getDirection().clone().multiply(.8D)).toLocation(player.getWorld()), 0F, 0F, 0F, 0.01F, 3);
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
	public List<Location> getLocations() {
		return shots.stream().map(shot -> shot.location).collect(toList());
	}

	@Override
	public void handleCollision(Collision collision) {
		if (collision.isRemovingFirst()) {
			Optional<FireShot> collidedShot = shots.stream().filter(shot -> shot.location.equals(collision.getLocationFirst())).findAny();

			if (collidedShot.isPresent()) {
				shots.remove(collidedShot.get());
			}
		} else {
			CoreAbility second = collision.getAbilitySecond();
			if (second instanceof AirShield) {
				boolean reflect = JedCore.plugin.getConfig().getBoolean("Abilities.Fire.FireShots.Collisions.AirShield.Reflect", true);

				if (reflect) {
					Optional<FireShot> collidedShot = shots.stream().filter(shot -> shot.location.equals(collision.getLocationFirst())).findAny();

					if (collidedShot.isPresent()) {
						FireShot fireShot = collidedShot.get();
						AirShield shield = (AirShield) second;

						fireShot.direction = player.getLocation().getDirection().clone();
						AirShieldReflector.reflect(shield, fireShot.location, fireShot.direction);
					}
				}
			}
		}
	}

	@Override
	public String getName() {
		return "FireShots";
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
		return "* JedCore Addon *\n" + JedCore.plugin.getConfig().getString("Abilities.Fire.FireShots.Description");
	}

	@Override
	public void load() {

	}

	@Override
	public void stop() {

	}
	
	@Override
	public boolean isEnabled() {
		return JedCore.plugin.getConfig().getBoolean("Abilities.Fire.FireShots.Enabled");
	}
}
