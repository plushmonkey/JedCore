package com.jedk1.jedcore.ability.waterbending;

import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.configuration.JedCoreConfig;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.ParticleEffect;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WakeFishing extends WaterAbility implements AddonAbility {

	private Block focusedBlock;
	private Location location;
	private int point;

	private long cooldown;
	private long duration;
	private long range;
	private long time;

	Random rand = new Random();

	public WakeFishing(Player player) {
		super(player);
		if (!bPlayer.canBend(this)) {
			return;
		}

		setFields();
		
		time = System.currentTimeMillis();
		if (prepare())
			start();
	}
	
	public void setFields() {
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);

		cooldown = config.getLong("Abilities.Water.WakeFishing.Cooldown");
		duration = config.getLong("Abilities.Water.WakeFishing.Duration");
		range = config.getLong("Abilities.Water.WakeFishing.Range");
	}

	@SuppressWarnings("deprecation")
	private boolean prepare() {
		Block block = BlockSource.getWaterSourceBlock(player, range, ClickType.SHIFT_DOWN, true, false, false);
		if (block != null && isWater(block) && block.getData() == 0) {
			focusedBlock = block;
			location = focusedBlock.getLocation();
			return true;
		}
		return false;
	}

	private boolean isFocused() {
		Block block = BlockSource.getWaterSourceBlock(player, range, ClickType.SHIFT_DOWN, true, false, false);
		if (block != null && block.equals(focusedBlock)) {
			return true;
		}
		return false;
	}

	@Override
	public void progress() {
		if (player.isDead() || !player.isOnline() || !player.isSneaking()) {
			remove();
			return;
		}
		if (!bPlayer.canBendIgnoreCooldowns(this) || !isFocused()) {
			bPlayer.addCooldown(this);
			remove();
			return;
		}
		if (System.currentTimeMillis() > time + duration) {
			bPlayer.addCooldown(this);
			remove();
			return;
		}
		displayParticles();
		spawnFishRandom();
		return;
	}

	private void displayParticles() {
		point++;
		if (point == 32)
			point = 0;
		for (int i = 0; i < 4; i++) {
			ParticleEffect.SPLASH.display(getCirclePoints(focusedBlock.getLocation().clone().add(0.5, 0, 0.5), 32, (i * 90), 1).get(point), 0f, 0f, 0f, 0.05F, 3);
			ParticleEffect.WAKE.display(getCirclePoints(focusedBlock.getLocation().clone().add(0.5, -0.6, 0.5), 32, (i * 90), 1).get(point), 0f, 0f, 0f, 0.02F, 1);
		}

		ParticleEffect.SMOKE.display(focusedBlock.getLocation().clone().add(.5, .5, .5), 0F, 0F, 0F, 0.001F, 2);
	}

	private void spawnFishRandom() {
		if (rand.nextInt(50) == 0) {
			ItemStack fish = new ItemStack(Material.RAW_FISH, 1, (byte) rand.nextInt(3));
			Item item = player.getWorld().dropItemNaturally(focusedBlock.getLocation().clone().add(.5, 1.5, .5), fish);
			Vector v = player.getEyeLocation().toVector().subtract(focusedBlock.getLocation().clone().add(.5, 1.5, .5).toVector());
			item.setVelocity(v.multiply(.15));
		}
	}

	private List<Location> getCirclePoints(Location location, int points, int startAngle, double size) {
		List<Location> locations = new ArrayList<Location>();
		for (int i = 0; i < 360; i += 360 / points) {
			double angle = (i * Math.PI / 180);
			double x = size * Math.cos(angle + startAngle);
			double z = size * Math.sin(angle + startAngle);
			Location loc = location.clone();
			loc.add(x, 1, z);
			locations.add(loc);
		}
		return locations;
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
		return "WakeFishing";
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
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);
		return "* JedCore Addon *\n" + config.getString("Abilities.Water.WakeFishing.Description");
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
		return config.getBoolean("Abilities.Water.WakeFishing.Enabled");
	}
}
