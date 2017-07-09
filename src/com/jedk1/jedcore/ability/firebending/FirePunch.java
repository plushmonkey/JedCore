package com.jedk1.jedcore.ability.firebending;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.jedk1.jedcore.util.FireTick;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.jedk1.jedcore.JedCore;
import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.firebending.util.FireDamageTimer;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;

public class FirePunch extends FireAbility implements AddonAbility {

	public static List<UUID> recent = new ArrayList<UUID>();
	
	public FirePunch(Player player) {
		super(player);
		if (!getRecent().contains(player.getUniqueId())) {
			getRecent().add(player.getUniqueId());
		}
	}

	public static void display(Server server) {
		if (!getEnabled()) return;
		for (Player player : server.getOnlinePlayers()) {
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
			if (bPlayer != null && bPlayer.canBend(getAbility("FirePunch"))) {
				display(player);
			}
		}
	}

	private static void display(Player player) {
		Location offset = GeneralMethods.getRightSide(player.getLocation(), .55).add(0, 1.2, 0);
		Vector dir = player.getEyeLocation().getDirection();
		Location righthand = offset.toVector().add(dir.clone().multiply(.8D)).toLocation(player.getWorld());
		ParticleEffect.FLAME.display(0f, 0f, 0f, 0f, 3, righthand, 257D);
		ParticleEffect.SMOKE.display(0f, 0f, 0f, 0f, 3, righthand, 257D);
	}

	public static boolean punch(Player player, LivingEntity target) {
		if (!getEnabled()) return false;
		if (player == null || player.isDead() || !player.isOnline()) {
			return false;
		}
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer.canBend(getAbility("FirePunch")) && getRecent().contains(player.getUniqueId())) {
			bPlayer.addCooldown(getAbility("FirePunch"), getStaticCooldown());
			getRecent().remove(player.getUniqueId());
			playFirebendingParticles(target.getLocation().add(0, 1, 0), 1, 0f, 0f, 0f);
			DamageAbility da = new DamageAbility(player);
			da.remove();
			DamageHandler.damageEntity(target, getDamage(), da);

			FireTick.set(target, getFireTicks() / 50);
			if (getStaticCooldown() > getFireTicks()) {
				new FireDamageTimer(target, player);
			}
			return true;
		}
		return false;
	}

	public static boolean getEnabled() {
		return JedCore.plugin.getConfig().getBoolean("Abilities.Fire.FirePunch.Enabled");
	}
	
	public static double getDamage() {
		return JedCore.plugin.getConfig().getDouble("Abilities.Fire.FirePunch.Damage");
	}
	
	public static int getFireTicks() {
		return JedCore.plugin.getConfig().getInt("Abilities.Fire.FirePunch.FireTicks");
	}
	
	public static long getStaticCooldown() {
		return JedCore.plugin.getConfig().getLong("Abilities.Fire.FirePunch.Cooldown");
	}
	
	public static List<UUID> getRecent() {
		return recent;
	}

	@Override
	public void progress() {
	}

	@Override
	public long getCooldown() {
		return JedCore.plugin.getConfig().getLong("Abilities.Fire.FirePunch.Cooldown");
	}

	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public String getName() {
		return "FirePunch";
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
		return "* JedCore Addon *\n" + JedCore.plugin.getConfig().getString("Abilities.Fire.FirePunch.Description");
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
		return JedCore.plugin.getConfig().getBoolean("Abilities.Fire.FirePunch.Enabled");
	}
	
	public static class DamageAbility extends FirePunch {
		
		public DamageAbility(Player player) {
			super(player);
			start();
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
			return "FirePunch";
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
		public void progress() {
			remove();
			return;
		}
	}
}
