package com.jedk1.jedcore.ability.earthbending.combo;

import com.jedk1.jedcore.util.MaterialUtil;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.earthbending.lava.LavaFlow;
import io.netty.util.internal.ConcurrentSet;

import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.util.TempFallingBlock;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.LavaAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class MagmaBlast extends LavaAbility implements AddonAbility, ComboAbility {

	private long cooldown;
	private int maxSources;
	private int sourceRange;
	private double damage;
	// This will destroy the instance if LavaFlow is on cooldown.
	private boolean requireLavaFlow;

	private Location origin;
	private int counter;
	private ConcurrentSet<TempFallingBlock> sources = new ConcurrentSet<>();
	private List<TempBlock> blocks = new ArrayList<>();
	
	private Random rand = new Random();

	public MagmaBlast(Player player) {
		super(player);
		setFields();


		origin = player.getLocation();

		if (raiseSources()) {
			start();
		}
	}

	public void setFields() {
		maxSources = JedCore.plugin.getConfig().getInt("Abilities.Earth.EarthCombo.MagmaBlast.MaxShots");
		sourceRange = JedCore.plugin.getConfig().getInt("Abilities.Earth.EarthCombo.MagmaBlast.SearchRange");
		damage = JedCore.plugin.getConfig().getInt("Abilities.Earth.EarthCombo.MagmaBlast.ImpactDamage");
		cooldown = JedCore.plugin.getConfig().getInt("Abilities.Earth.EarthCombo.MagmaBlast.Cooldown");
		requireLavaFlow  = JedCore.plugin.getConfig().getBoolean("Abilities.Earth.EarthCombo.MagmaBlast.RequireLavaFlow");
	}

	// Select random nearby earth blocks as sources and raise them in the air.
	private boolean raiseSources() {
		List<Block> potentialBlocks = GeneralMethods.getBlocksAroundPoint(origin, sourceRange).stream().filter(ElementalAbility::isEarth).collect(Collectors.toList());

		Collections.shuffle(potentialBlocks);

		for (Block newSource : potentialBlocks) {
			if (!isValidSource(newSource)) continue;

			sources.add(new TempFallingBlock(newSource.getLocation().add(0, 1, 0), Material.NETHERRACK, (byte) 0, new Vector(0, 0.9, 0), this));

			if (sources.size() >= maxSources) {
				break;
			}
		}

		return !sources.isEmpty();
	}

	// Checks to make sure the source block has room to fly upwards.
	private boolean isValidSource(Block block) {
		for (int i = 0; i < 3; ++i) {
			if (!MaterialUtil.isTransparent(block.getRelative(BlockFace.UP, i + 1))) {
				return false;
			}
		}

		return true;
	}

	@Override
	public void progress() {
		if (player == null || !player.isOnline() || player.isDead()) {
			remove();
			return;
		}

		if (!bPlayer.canBendIgnoreBinds(this) || !(bPlayer.getBoundAbility() instanceof LavaFlow)) {
			remove();
			return;
		}

		if (requireLavaFlow && !bPlayer.canBend(getAbility("LavaFlow"))) {
			remove();
			return;
		}

		displayAnimation();
		handleSources();

		if (TempFallingBlock.getFromAbility(this).isEmpty() && blocks.isEmpty()) {
			remove();
		}
	}

	private void handleSources() {
		if (sources.isEmpty()) return;

		for (TempFallingBlock tfb : sources) {
			if (tfb.getLocation().getBlockY() >= (origin.getBlockY() + 3)) {
				blocks.add(new TempBlock(tfb.getLocation().getBlock(), Material.NETHERRACK, (byte) 0));
				sources.remove(tfb);
				tfb.remove();
			}
		}
	}

	private void displayAnimation() {
		if (++counter == 3) {
			counter = 0;
		} else {
			return;
		}

		for (TempFallingBlock tfb : TempFallingBlock.getFromAbility(this)) {
			if (!tfb.getFallingBlock().isDead()) {
				playParticles(tfb.getLocation());
			} else {
				tfb.remove();
			}
		}
		for (TempBlock tb : blocks) {
			playParticles(tb.getLocation());
		}
	}

	private void playParticles(Location location) {
		location.add(.5,.5,.5);
		ParticleEffect.LAVA.display((float) Math.random(), (float) Math.random(), (float) Math.random(), 0f, 2, location, 257D);
		ParticleEffect.SMOKE.display((float) Math.random(), (float) Math.random(), (float) Math.random(), 0f, 2, location, 257D);
		for (int i = 0; i < 10; i++) {
			GeneralMethods.displayColoredParticle(getOffsetLocation(location, 2), "FFA400");
			GeneralMethods.displayColoredParticle(getOffsetLocation(location, 2), "FF8C00");
		}
	}

	private Location getOffsetLocation(Location loc, double offset) {
		return loc.clone().add((float) ((Math.random() - 0.5)*offset), (float) ((Math.random() - 0.5)*offset), (float) ((Math.random() - 0.5)*offset));
	}

	@Override
	public void remove() {
		bPlayer.addCooldown(this);

		super.remove();
		for (TempBlock tb : blocks) {
			tb.revertBlock();
		}
		for (TempFallingBlock tfb : TempFallingBlock.getFromAbility(this)) {
			tfb.remove();
		}
	}

	public static void performAction(Player player) {
		MagmaBlast mb = getAbility(player, MagmaBlast.class);

		if (mb != null) {
			mb.performAction();
		}
	}

	private void performAction() {
		if (blocks.isEmpty()) {
			return;
		}

		Location target = GeneralMethods.getTargetedLocation(player, 30);
		double distance = 0;
		TempBlock tb = null;
		for (TempBlock tb1 : blocks) {
			if (distance == 0 || tb1.getLocation().distance(target) < distance) {
				distance = tb1.getLocation().distance(target);
				tb = tb1;
			}
		}
		blocks.remove(tb);
		Vector direction = GeneralMethods.getDirection(tb.getLocation(), GeneralMethods.getTargetedLocation(player, 30)).normalize();
		tb.revertBlock();
		new TempFallingBlock(tb.getLocation(), Material.NETHERRACK, (byte) 0, direction.multiply(1.5), this, true);
	}

	public static void blast(TempFallingBlock tfb) {
		MagmaBlast mb = (MagmaBlast) tfb.getAbility();
		Location location = tfb.getLocation().clone().add(0.5, 0.5, 0.5);
		for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 2.0)) {
			if (!(entity instanceof LivingEntity)) continue;

			DamageHandler.damageEntity(entity, ((MagmaBlast) tfb.getAbility()).getDamage(), tfb.getAbility());
		}
		ParticleEffect.FLAME.display((float) Math.random(), (float) Math.random(), (float) Math.random(), 0.5f, 20, location, 257D);
		ParticleEffect.LARGE_SMOKE.display((float) Math.random(), (float) Math.random(), (float) Math.random(), 0.5f, 20, location, 257D);
		ParticleEffect.FIREWORKS_SPARK.display((float) Math.random(), (float) Math.random(), (float) Math.random(), 0.5f, 20, location, 257D);
		ParticleEffect.LARGE_SMOKE.display((float) Math.random(), (float) Math.random(), (float) Math.random(), 0.5f, 20, location, 257D);
		location.getWorld().playSound(location, (mb.rand.nextBoolean()) ? Sound.ENTITY_FIREWORK_BLAST : Sound.ENTITY_FIREWORK_BLAST_FAR, 1f, 1f);
		location.getWorld().playSound(location, (mb.rand.nextBoolean()) ? Sound.ENTITY_FIREWORK_TWINKLE : Sound.ENTITY_FIREWORK_TWINKLE_FAR, 1f, 1f);
	}
	
	public double getDamage() {
		return damage;
	}
	
	public void setDamage(double damage) {
		this.damage = damage;
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
		return "MagmaBlast";
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
	public Object createNewComboInstance(Player player) {
		return new MagmaBlast(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combination = new ArrayList<>();
		combination.add(new AbilityInformation("EarthBlast", ClickType.SHIFT_DOWN));
		combination.add(new AbilityInformation("LavaFlow", ClickType.SHIFT_UP));
		combination.add(new AbilityInformation("LavaFlow", ClickType.SHIFT_DOWN));
		combination.add(new AbilityInformation("LavaFlow", ClickType.RIGHT_CLICK_BLOCK));
		return combination;
	}

	@Override
	public String getInstructions() {
		return "EarthBlast (Hold Shift) > LavaFlow (Release Shift) > LavaFlow (Hold Shift) > LavaFlow (Right Click a block) > LavaFlow (Left Click Multiple times)";
	}

	@Override
	public String getDescription() {
		return "* JedCore Addon *\n" + JedCore.plugin.getConfig().getString("Abilities.Earth.EarthCombo.MagmaBlast.Description");
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
	public void load() {
	}

	@Override
	public void stop() {
	}

	@Override
	public boolean isEnabled() {
		return JedCore.plugin.getConfig().getBoolean("Abilities.Earth.EarthCombo.MagmaBlast.Enabled");
	}
}