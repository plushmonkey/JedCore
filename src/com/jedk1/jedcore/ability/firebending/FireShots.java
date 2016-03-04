package com.jedk1.jedcore.ability.firebending;

import com.jedk1.jedcore.JedCore;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.firebending.FireDamageTimer;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class FireShots extends FireAbility implements AddonAbility {

	public static List<FireShot> shots = new ArrayList<FireShot>();
	
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
				location = location.add(player.getLocation().getDirection().clone().multiply(1));
				if(GeneralMethods.isSolid(location.getBlock()) || isWater(location.getBlock())){
					return false;
				}
				
				ParticleEffect.SMOKE.display(location, 0.0F, 0.0F, 0.0F, 0.01F, 2);
				ParticleEffect.FLAME.display(location, 0.0F, 0.0F, 0.0F, 0.02F, 5);
				
				for(Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 1.5)){
					if(entity instanceof LivingEntity && entity.getEntityId() != player.getEntityId() && !(entity instanceof ArmorStand)){
						DamageHandler.damageEntity(entity, damage, ability);
						entity.setFireTicks(Math.round(fireticks));
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
		if(amount == 0){
			bPlayer.addCooldown(this);
			remove();
			return;
		}
		displayFireBalls();
		return;
	}
	
	public static void fireShot(Player player) {
		if(hasAbility(player, FireShots.class)) {
			FireShots fs = (FireShots) getAbility(player, FireShots.class);
			fs.fireShot();
			return;
		}
	}
	
	public void fireShot() {
		if (amount >= 1) {
			amount--;
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
	
	public static void progressFireShots() {
		List<Integer> ids = new ArrayList<Integer>();
		for (FireShot fs : shots) {
			if (!fs.progress()) {
				ids.add(shots.indexOf(fs));
			}
		}
		for (int id : ids) {
			if (id >= shots.size()) {
				continue;
			}
			shots.remove(id);
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
		return;
	}

	@Override
	public void stop() {
		return;
	}
	
	@Override
	public boolean isEnabled() {
		return JedCore.plugin.getConfig().getBoolean("Abilities.Fire.FireShots.Enabled");
	}
}