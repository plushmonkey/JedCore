package com.jedk1.jedcore.ability.earthbending;

import com.jedk1.jedcore.JedCore;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.earthbending.EarthArmor;
import com.projectkorra.projectkorra.util.TempPotionEffect;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class MetalArmor extends EarthAbility implements AddonAbility {

	private boolean useIronArmor;
	private int strength;
	private Material head;

	public MetalArmor(Player player) {
		super(player);
		if (!bPlayer.canBendIgnoreCooldowns(getAbility("EarthArmor")) || !bPlayer.canMetalbend()) {
			return;
		}
		if (!hasAbility(player, EarthArmor.class)) {
			return;
		}
		this.useIronArmor = false;
		this.strength = 3;
		this.head = ((EarthArmor) getAbility(player, EarthArmor.class)).getHeadType();
		start();
	}

	@Override
	public void progress() {
		if (player == null || !player.isOnline() || player.isDead()) {
			remove();
			return;
		}
		if (!hasAbility(player, EarthArmor.class)) {
			remove();
			return;
		}
		EarthArmor ea = (EarthArmor) getAbility(player, EarthArmor.class);
		if (ea.isFormed()) {
			if (isMetal(head)) {
				ItemStack[] armors = { new ItemStack(Material.CHAINMAIL_BOOTS, 1),
						new ItemStack(Material.CHAINMAIL_LEGGINGS, 1),
						new ItemStack(Material.CHAINMAIL_CHESTPLATE, 1),
						new ItemStack(Material.CHAINMAIL_HELMET, 1) };

				if(useIronArmor){
					armors = new ItemStack[]{ new ItemStack(Material.IRON_BOOTS, 1),
							new ItemStack(Material.IRON_LEGGINGS, 1),
							new ItemStack(Material.IRON_CHESTPLATE, 1),
							new ItemStack(Material.IRON_HELMET, 1) };
				}
				if(useIronArmor && head.equals(Material.GOLD_BLOCK)){
					armors = new ItemStack[]{ new ItemStack(Material.GOLD_BOOTS, 1),
							new ItemStack(Material.GOLD_LEGGINGS, 1),
							new ItemStack(Material.GOLD_CHESTPLATE, 1),
							new ItemStack(Material.GOLD_HELMET, 1) };
				}
				player.getInventory().setArmorContents(armors);
				PotionEffect resistance = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, (int) ea.getDuration() / 50, strength - 1);
				new TempPotionEffect(player, resistance);
			}
			remove();
		}
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
		return "MetalArmor";
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
		return JedCore.plugin.getConfig().getBoolean("Abilities.Earth.EarthArmor.Enabled");
	}
}