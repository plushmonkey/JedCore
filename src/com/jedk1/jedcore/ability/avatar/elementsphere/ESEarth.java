package com.jedk1.jedcore.ability.avatar.elementsphere;

import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.configuration.JedCoreConfig;
import com.jedk1.jedcore.util.RegenTempBlock;
import com.jedk1.jedcore.util.TempFallingBlock;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AvatarAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Random;

public class ESEarth extends AvatarAbility implements AddonAbility {

	private long revertDelay;
	private double damage;
	private int impactSize;
	private long cooldown;
	private TempFallingBlock tfb;

	static Random rand = new Random();

	public ESEarth(Player player) {
		super(player);
		if (!hasAbility(player, ElementSphere.class)) {
			return;
		}
		ElementSphere currES = getAbility(player, ElementSphere.class);
		if (currES.getEarthUses() == 0) {
			return;
		}
		if (bPlayer.isOnCooldown("ESEarth")) {
			return;
		}
		setFields();
		bPlayer.addCooldown("ESEarth", getCooldown());
		currES.setEarthUses(currES.getEarthUses() - 1);
		Location location = player.getEyeLocation().clone().add(player.getEyeLocation().getDirection().multiply(1));
		tfb = new TempFallingBlock(location, Material.DIRT, (byte) 0, location.getDirection().multiply(3), this);
		start();
	}

	public void setFields() {
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);
		revertDelay = config.getLong("Abilities.Avatar.ElementSphere.Earth.ImpactRevert");
		damage = config.getDouble("Abilities.Avatar.ElementSphere.Earth.Damage");
		impactSize = config.getInt("Abilities.Avatar.ElementSphere.Earth.ImpactCraterSize");
		cooldown = config.getLong("Abilities.Avatar.ElementSphere.Earth.Cooldown");
	}

	@Override
	public void progress() {
		if (player == null || !player.isOnline()) {
			tfb.remove();
			remove();
			return;
		}
		if (tfb.getFallingBlock().isDead()) {
			remove();
			return;
		}

		EarthAbility.playEarthbendingSound(tfb.getLocation());

		for (Entity entity : GeneralMethods.getEntitiesAroundPoint(tfb.getLocation(), 2.5)) {
			if (entity instanceof LivingEntity && !(entity instanceof ArmorStand) && entity.getEntityId() != player.getEntityId()) {
				//explodeEarth(fb);
				DamageHandler.damageEntity(entity, damage, this);
			}
		}
	}

	public static void explodeEarth(TempFallingBlock tempfallingblock) {
		FallingBlock fb = tempfallingblock.getFallingBlock();
		ESEarth es = (ESEarth) tempfallingblock.getAbility();
		Player player = es.getPlayer();

		ParticleEffect.SMOKE_LARGE.display(fb.getLocation(), 0, 0, 0, 0.3F, 25);
		fb.getWorld().playSound(fb.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2f, 0.5f);

		for (Location l : GeneralMethods.getCircle(fb.getLocation(), es.impactSize, 1, false, true, 0)) {
			//if (TempBlock.isTempBlock(l.getBlock())) {
			//	TempBlock.revertBlock(l.getBlock(), Material.AIR);
			//	TempBlock.removeBlock(l.getBlock());
			//}
			if (!isUnbreakable(l.getBlock()) && !GeneralMethods.isRegionProtectedFromBuild(player, "ElementSphere", l) && EarthAbility.isEarthbendable(player, l.getBlock())) {
				ParticleEffect.SMOKE_LARGE.display(l, 0, 0, 0, 0.1F, 2);
				//new RegenTempBlock(l.getBlock(), Material.AIR, (byte) 0, (long) rand.nextInt((int) es.revertDelay - (int) (es.revertDelay - 1000)) + (es.revertDelay - 1000));
				new RegenTempBlock(l.getBlock(), Material.AIR, (byte) 0, (long) rand.nextInt((int) es.revertDelay - (int) (es.revertDelay - 1000)) + (es.revertDelay - 1000), false);
			}

			if (GeneralMethods.isSolid(l.getBlock().getRelative(BlockFace.DOWN)) && !isUnbreakable(l.getBlock()) && l.getBlock().getType().equals(Material.AIR) && rand.nextInt(20) == 0 && EarthAbility.isEarthbendable(player, l.getBlock().getRelative(BlockFace.DOWN))) {
				new RegenTempBlock(l.getBlock(), l.getBlock().getRelative(BlockFace.DOWN).getType(), (byte) 0, (long) rand.nextInt((int) es.revertDelay - (int) (es.revertDelay - 1000)) + (es.revertDelay - 1000));
			}
		}

		tempfallingblock.remove();
	}

	static Material[] unbreakables = { Material.BEDROCK, Material.BARRIER, Material.PORTAL, Material.ENDER_PORTAL, Material.ENDER_PORTAL_FRAME, Material.ENDER_CHEST, Material.CHEST, Material.TRAPPED_CHEST };

	public static boolean isUnbreakable(Block block) {
		if (Arrays.asList(unbreakables).contains(block.getType()))
			return true;
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
		return "ElementSphere Earth";
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
		return config.getBoolean("Abilities.Avatar.ElementSphere.Enabled");
	}
}
