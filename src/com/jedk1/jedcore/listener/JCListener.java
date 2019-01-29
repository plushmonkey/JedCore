package com.jedk1.jedcore.listener;

import com.jedk1.jedcore.JCMethods;
import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.ability.avatar.elementsphere.ESEarth;
import com.jedk1.jedcore.ability.chiblocking.Backstab;
import com.jedk1.jedcore.ability.chiblocking.DaggerThrow;
import com.jedk1.jedcore.ability.earthbending.*;
import com.jedk1.jedcore.ability.earthbending.combo.MagmaBlast;
import com.jedk1.jedcore.ability.firebending.FireBreath;
import com.jedk1.jedcore.ability.firebending.FirePunch;
import com.jedk1.jedcore.ability.firebending.FireSki;
import com.jedk1.jedcore.ability.waterbending.IceClaws;
import com.jedk1.jedcore.ability.waterbending.IceWall;
import com.jedk1.jedcore.scoreboard.BendingBoard;
import com.jedk1.jedcore.util.RegenTempBlock;
import com.jedk1.jedcore.util.TempFallingBlock;
import com.jedk1.jedcore.util.UpdateChecker;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.IceAbility;
import com.projectkorra.projectkorra.earthbending.lava.LavaFlow;
import com.projectkorra.projectkorra.event.*;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class JCListener implements Listener {

	JedCore plugin;

	public JCListener(JedCore plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (BendingBoard.isDisabled(event.getPlayer())) {
			return;
		}
		BendingBoard bb = BendingBoard.get(event.getPlayer());
		if (bb != null) {
			new BukkitRunnable() {
				public void run() {
					bb.update();
				}
			}.runTaskLater(JedCore.plugin, 50);
		}
		if (UpdateChecker.hasUpdate() && JedCore.plugin.getConfig().getBoolean("Settings.Updater.Notify")) {
			if (event.getPlayer().hasPermission("jedcore.admin.notify")) {
				event.getPlayer().sendMessage(ChatColor.DARK_RED + "JedCore: " + ChatColor.RED + "There is an update available for JedCore!");
			}
		}
	}

	@EventHandler
	public void onAbilityStart(AbilityStartEvent event) {
		if (event.isCancelled()) return;

		if (event.getAbility() instanceof LavaFlow) {
			Player player = event.getAbility().getPlayer();
			MagmaBlast mb = CoreAbility.getAbility(player, MagmaBlast.class);

			if (mb != null && (mb.hasBlocks() || mb.shouldBlockLavaFlow())) {
				event.setCancelled(true);
				LavaFlow flow = (LavaFlow) event.getAbility();

				// Reset the cooldown of LavaFlow that was set before the call to start().
				flow.getBendingPlayer().removeCooldown(flow);
			}
		}
	}

	@EventHandler
	public void onFlow(BlockFromToEvent event) {
		if (!LavaDisc.canFlowFrom(event.getBlock())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerFallDamage(EntityDamageEvent event) {
		if (event.isCancelled() || event.getCause() != DamageCause.FALL || !(event.getEntity() instanceof Player)) {
			return;
		}

		Player player = (Player)event.getEntity();

		if (MudSurge.onFallDamage(player)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getCause().equals(DamageCause.SUFFOCATION)) {
			if (event.getEntity() instanceof LivingEntity) {
				LivingEntity entity = (LivingEntity) event.getEntity();
				Block block = entity.getEyeLocation().getBlock();
				if (RegenTempBlock.blocks.containsKey(block) && IceAbility.isIce(block)) {
					event.setCancelled(true);
					return;
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player && event.getEntity() instanceof LivingEntity) {
			if (event.getCause() == DamageCause.ENTITY_ATTACK) {
				double distSq = event.getDamager().getLocation().distanceSquared(event.getEntity().getLocation());

				// Only activate these in melee range
				if (distSq <= 5 * 5) {
					if (FirePunch.punch((Player) event.getDamager(), (LivingEntity) event.getEntity())) {
						event.setCancelled(true);
						return;
					}

					if (Backstab.punch((Player) event.getDamager(), (LivingEntity) event.getEntity())) {
						event.setDamage(Backstab.getDamage(event.getDamager().getWorld()));
						return;
					}
				}
			}

			if (IceClaws.freezeEntity((Player) event.getDamager(), (LivingEntity) event.getEntity())) {
				event.setCancelled(true);
				return;
			}
		}

		if (event.getDamager() instanceof Arrow) {
			Arrow arrow = (Arrow) event.getDamager();

			if (event.getEntity() instanceof LivingEntity) {
				if (arrow.hasMetadata("daggerthrow") && arrow.getShooter() instanceof Player) {
					if (event.getEntity().getEntityId() != ((Player) arrow.getShooter()).getEntityId()) {
						DaggerThrow.damageEntityFromArrow((Player) arrow.getShooter(), (LivingEntity) event.getEntity(), arrow);
					}
					event.setDamage(0);
					event.setCancelled(true);
					arrow.remove();
					arrow.removeMetadata("daggerthrow", JedCore.plugin);
				}

				if (arrow.hasMetadata("metalhook")) {
					arrow.remove();
					event.setDamage(0);
					event.setCancelled(true);
					arrow.removeMetadata("metalhook", JedCore.plugin);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void EntityChangeBlockEvent(EntityChangeBlockEvent event) {
		if (event.getEntityType() == EntityType.FALLING_BLOCK) {
			FallingBlock fb = (FallingBlock) event.getEntity();
			if (TempFallingBlock.isTempFallingBlock(fb)) {
				TempFallingBlock tfb = TempFallingBlock.get(fb);
				if (tfb.getAbility() instanceof ESEarth) {
					ESEarth.explodeEarth(tfb);
				}
				if (tfb.getAbility() instanceof MagmaBlast) {
					MagmaBlast.blast(tfb);
				}
				tfb.remove();
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockBreak(BlockBreakEvent event) {
		MetalFragments.remove(event.getPlayer(), event.getBlock());
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityExplode(EntityExplodeEvent event){
		if (IceWall.checkExplosions(event.getLocation(), event.getEntity())) {
			event.blockList().clear();
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onCollisionWithEntity(HorizontalVelocityChangeEvent event) {
		if (event.getEntity().getWorld() != event.getInstigator().getWorld()) {
			return;
		}
		IceWall.collisionDamage(event.getEntity(), event.getDistanceTraveled(), event.getDifference(), event.getInstigator());
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerSprint(PlayerToggleSprintEvent event) {
		if (!event.isSprinting())
			return;

		if (CoreAbility.hasAbility(event.getPlayer(), EarthSurf.class)) {
			event.setCancelled(true);
			return;
		}

		if (CoreAbility.hasAbility(event.getPlayer(), FireSki.class)) {
			event.setCancelled(true);
			return;
		}

		MetalShred.startShred(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void projectKorraReload(BendingReloadEvent event) {
		final CommandSender sender = event.getSender();
		// There's a PK bug where a new collision manager is set on reload without stopping the old task.
		ProjectKorra.getCollisionManager().stopCollisionDetection();
		new BukkitRunnable() {
			public void run() {
				JCMethods.reload();
				sender.sendMessage(ChatColor.DARK_AQUA + "JedCore config reloaded.");
			}
		}.runTaskLater(JedCore.plugin, 1);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerItemHoldEvent(PlayerItemHeldEvent event) {
		BendingBoard.update(event.getPlayer(), event.getNewSlot());
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onCooldownChange(PlayerCooldownChangeEvent event) {
		if (event.getPlayer() == null) return;

		// Fix a bug in ProjectKorra 1.8.4 that keeps IceWave around forever.
		// It will continuously add a cooldown to WaterWave, which makes this spam tasks / scoreboard updates.
		// It also happens with FastSwim when the player is a waterbender.
		if (BendingBoard.shouldIgnoreAbility(event.getAbility())) {
			return;
		}
		new BukkitRunnable() {
			public void run() {
				BendingBoard.update(event.getPlayer());
			}
		}.runTaskLater(JedCore.plugin, 1);
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onWorldChange(PlayerChangedWorldEvent event){
		if (event.getPlayer() == null) return;
		new BukkitRunnable() {
			public void run() {
				BendingBoard.update(event.getPlayer());
			}
		}.runTaskLater(JedCore.plugin, 1);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onElementChange(PlayerChangeElementEvent event){
		if (event.getTarget() == null) return;
		new BukkitRunnable() {
			public void run() {
				BendingBoard.update(event.getTarget());
			}
		}.runTaskLater(JedCore.plugin, 1);
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onChat(AsyncPlayerChatEvent event){
		Player player = event.getPlayer();
		String msg = event.getMessage();

		if (msg.toLowerCase().contains("bring fire and light together as one and allow the breath of color")) {
			FireBreath.toggleRainbowBreath(player, true);
			event.setCancelled(true);
		}

		if (msg.toLowerCase().contains("split the bond of fire and light and set the color free")) {
			FireBreath.toggleRainbowBreath(player, false);
			event.setCancelled(true);
		}

	}
}
