package com.jedk1.jedcore.ability.earthbending.combo;

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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MagmaBlast extends LavaAbility implements AddonAbility, ComboAbility {

	private long cooldown;
	private int maxSources;
	private int sourceRange;
	private double damage;

	private Location origin;
	private int counter;
	private ConcurrentSet<TempFallingBlock> sources = new ConcurrentSet<TempFallingBlock>();
	private List<TempBlock> blocks = new ArrayList<TempBlock>();
	
	Random rand = new Random();

	public MagmaBlast(Player player) {
		super(player);
		setFields();
		origin = player.getLocation();
		start();
		raiseSources();
	}

	public void setFields() {
		maxSources = 3;
		sourceRange = 4;
		damage = 4.0;
	}

	public void raiseSources() {
		int check = 0;
		while (sources.size() < maxSources && check < 10) {
			check++;
			Block block = getRandomSourceBlock(origin, sourceRange);
			sources.add(new TempFallingBlock(block.getLocation().add(0, 1, 0), Material.NETHERRACK, (byte) 0, new Vector(0, 0.9, 0), this));
		}
	}

	@Override
	public void progress() {
		if (player == null || !player.isOnline() || player.isDead()) {
			remove();
			return;
		}
		if (!bPlayer.canBendIgnoreBinds(this) || !bPlayer.canBend(getAbility("LavaFlow"))) {
			remove();
			return;
		}
		counter++;
		if (counter == 3) {
			counter = 0;
			displayAnimation();
		}
		if (!sources.isEmpty()) {
			handleSources();
		}
		if (TempFallingBlock.getFromAbility(this).isEmpty() && blocks.isEmpty()) {
			remove();
			return;
		}
	}

	public void handleSources() {
		for (TempFallingBlock tfb : sources) {
			if (tfb.getLocation().getBlockY() >= (origin.getBlockY() + 3)) {
				blocks.add(new TempBlock(tfb.getLocation().getBlock(), Material.NETHERRACK, (byte) 0));
				sources.remove(tfb);
				tfb.remove();
			}
		}
	}

	public void displayAnimation() {
		for (TempFallingBlock tfb : TempFallingBlock.getFromAbility(this)) {
			if (!tfb.getFallingBlock().isDead()) {
				playParticles(tfb.getLocation());
			}
		}
		for (TempBlock tb : blocks) {
			playParticles(tb.getLocation());
		}
	}

	public void playParticles(Location location) {
		location.add(.5,.5,.5);
		ParticleEffect.LAVA.display((float) Math.random(), (float) Math.random(), (float) Math.random(), 0f, 2, location, 257D);
		ParticleEffect.SMOKE.display((float) Math.random(), (float) Math.random(), (float) Math.random(), 0f, 2, location, 257D);
		for (int i = 0; i < 10; i++) {
			GeneralMethods.displayColoredParticle(getOffsetLocation(location, 2), "FFA400");
			GeneralMethods.displayColoredParticle(getOffsetLocation(location, 2), "FF8C00");
		}
	}

	public Location getOffsetLocation(Location loc, double offset) {
		return loc.clone().add((float) ((Math.random() - 0.5)*offset), (float) ((Math.random() - 0.5)*offset), (float) ((Math.random() - 0.5)*offset));
	}

	public static Block getRandomSourceBlock(Location location, int radius) {
		Random rand = new Random();
		List<Integer> checked = new ArrayList<Integer>();
		List<Block> blocks = GeneralMethods.getBlocksAroundPoint(location, radius);
		for (int i = 0; i < blocks.size(); i++) {
			int index = rand.nextInt(blocks.size());
			while (checked.contains(index)) {
				index = rand.nextInt(blocks.size());
			}
			checked.add(index);
			Block block = blocks.get(index);
			if (block == null || !isEarth(block)) {
				continue;
			}
			return block;
		}
		return null;
	}

	@Override
	public void remove() {
		super.remove();
		for (TempBlock tb : blocks) {
			tb.revertBlock();
		}
		for (TempFallingBlock tfb : TempFallingBlock.getFromAbility(this)) {
			tfb.remove();
		}
	}

	public static void performAction(Player player) {
		if (hasAbility(player, MagmaBlast.class)) {
			((MagmaBlast) getAbility(player, MagmaBlast.class)).performAction();
		}
	}

	public void performAction() {
		if (blocks.isEmpty()) return;
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
		Location location = tfb.getLocation().add(0.5, 0.5, 0.5);
		for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 2.0)) {
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