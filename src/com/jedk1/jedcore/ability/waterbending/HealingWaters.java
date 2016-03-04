package com.jedk1.jedcore.ability.waterbending;

import com.jedk1.jedcore.JedCore;
import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.HealingAbility;
import com.projectkorra.projectkorra.chiblocking.Smokescreen;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Random;

public class HealingWaters extends HealingAbility implements AddonAbility {

	private static long time = 0;
	private static boolean enabled = true;

	public HealingWaters(Player player) {
		super(player);
	}

	public static void heal(Server server){
		if (enabled) {
			if(System.currentTimeMillis() - time >= 1000){
				time = System.currentTimeMillis();
				for(Player player : server.getOnlinePlayers()){
					BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
					if (bPlayer != null && bPlayer.canBend(getAbility("HealingWaters"))) {
						heal(player);
					}
				}
			}
		}
	}

	private static void heal(Player player) {
		if(inWater(player)){
			if(player.isSneaking()){
				Entity entity = GeneralMethods.getTargetedEntity(player, getRange(), new ArrayList<Entity>());
				if(entity instanceof LivingEntity && inWater(entity)){
					Location playerLoc = entity.getLocation();
					playerLoc.add(0, 1, 0);
					ParticleEffect.MOB_SPELL_AMBIENT.display(playerLoc, (float) Math.random(), (float) Math.random(), (float) Math.random(), 0.0F, 3);
					ParticleEffect.WAKE.display(playerLoc, 0, 0, 0, 0.05F, 25);
					giveHPToEntity((LivingEntity) entity);
				}
			}else{
				Location playerLoc = player.getLocation();
				playerLoc.add(0, 1, 0);
				ParticleEffect.MOB_SPELL_AMBIENT.display(playerLoc, (float) Math.random(), (float) Math.random(), (float) Math.random(), 0.0F, 3);
				ParticleEffect.WAKE.display(playerLoc, 0, 0, 0, 0.05F, 25);
				giveHP(player);
			}
		}else if(hasWaterSupply(player) && player.isSneaking()){
			Entity entity = GeneralMethods.getTargetedEntity(player, getRange(), new ArrayList<Entity>());
			if(entity != null){
				if(entity instanceof LivingEntity){
					Damageable dLe = (Damageable)entity;
					if(dLe.getHealth() < dLe.getMaxHealth()){
						Location playerLoc = entity.getLocation();
						playerLoc.add(0, 1, 0);
						ParticleEffect.MOB_SPELL_AMBIENT.display(playerLoc, (float) Math.random(), (float) Math.random(), (float) Math.random(), 0.0F, 3);
						ParticleEffect.WAKE.display(playerLoc, 0, 0, 0, 0.05F, 25);
						giveHPToEntity((LivingEntity) entity);
						entity.setFireTicks(0);
						Random rand = new Random();
						if(rand.nextInt(getDrainChance()) == 0)
							drainWaterSupply(player);
					}
				}
			}else{
				Location playerLoc = player.getLocation();
				playerLoc.add(0, 1, 0);
				ParticleEffect.MOB_SPELL_AMBIENT.display(playerLoc, (float) Math.random(), (float) Math.random(), (float) Math.random(), 0.0F, 3);
				ParticleEffect.WAKE.display(playerLoc, 0, 0, 0, 0.05F, 25);
				giveHP(player);
				player.setFireTicks(0);
				Random rand = new Random();
				if(rand.nextInt(getDrainChance()) == 0)
					drainWaterSupply(player);
			}
		}
	}

	private static void giveHPToEntity(LivingEntity le) {
		Damageable dLe = (Damageable)le;
		if (!le.isDead() && dLe.getHealth() < dLe.getMaxHealth()) {
			applyHealingToEntity(le);
		}
		for(PotionEffect effect : le.getActivePotionEffects()) {
			if(isNegativeEffect(effect.getType())) {
				le.removePotionEffect(effect.getType());
			}
		}
	}

	private static void giveHP(Player player){
		Damageable dP = (Damageable)player;
		if (!player.isDead() && dP.getHealth() < 20) {
			applyHealing(player);
		}
		for(PotionEffect effect : player.getActivePotionEffects()) {
			if(isNegativeEffect(effect.getType())) {
				if((effect.getType() == PotionEffectType.BLINDNESS) && Smokescreen.getBlindedTimes().containsKey(player.getName())) {
					return;
				}
				player.removePotionEffect(effect.getType());
			}
		}
	}



	private static boolean inWater(Entity entity) {
		Block block = entity.getLocation().getBlock();
		if (isWater(block) && !TempBlock.isTempBlock(block))
			return true;
		return false;
	}

	private static boolean hasWaterSupply(Player player){
		ItemStack heldItem = player.getItemInHand();
		if(heldItem.getType() == Material.POTION)
			return true;
		else if(heldItem.getType() == Material.WATER_BUCKET)
			return true;
		return false;
	}

	private static void drainWaterSupply(Player player){
		ItemStack heldItem = player.getItemInHand();
		if(heldItem.getType() == Material.POTION)
			player.setItemInHand(new ItemStack(Material.GLASS_BOTTLE, 1));
		else if(heldItem.getType() == Material.BUCKET);
	}

	private static void applyHealing(Player player) {
		Damageable dP = (Damageable)player;
		if (!GeneralMethods.isRegionProtectedFromBuild(player, "HealingWaters", player.getLocation()))
			if(dP.getHealth() < dP.getMaxHealth()) {
				player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 70, getPower()));
				AirAbility.breakBreathbendingHold(player);
			}
	}

	private static void applyHealingToEntity(LivingEntity le) {
		Damageable dLe = (Damageable)le;
		if(dLe.getHealth() < dLe.getMaxHealth()) {
			le.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 70, 1));
			AirAbility.breakBreathbendingHold(le);
		}
	}

	public static int getPower() {
		return JedCore.plugin.getConfig().getInt("Abilities.Water.HealingWaters.Power");
	}

	public static double getRange() {
		return JedCore.plugin.getConfig().getDouble("Abilities.Water.HealingWaters.Range");
	}

	public static int getDrainChance() {
		return JedCore.plugin.getConfig().getInt("Abilities.Water.HealingWaters.DrainChance");
	}

	@Override
	public long getCooldown() {
		return 0;
	}

	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public String getName() {
		return "HealingWaters";
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
		return "* JedCore Addon *\n" + JedCore.plugin.getConfig().getString("Abilities.Water.HealingWaters.Description");
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
		enabled = JedCore.plugin.getConfig().getBoolean("Abilities.Water.HealingWaters.Enabled");
		return enabled;
	}

	@Override
	public void progress() {
	}
}