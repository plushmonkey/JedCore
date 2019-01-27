package com.jedk1.jedcore.listener;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.projectkorra.projectkorra.firebending.FireJet;
import com.projectkorra.projectkorra.util.MovementHandler;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.ability.airbending.AirBlade;
import com.jedk1.jedcore.ability.airbending.AirBreath;
import com.jedk1.jedcore.ability.airbending.AirGlide;
import com.jedk1.jedcore.ability.airbending.AirPunch;
import com.jedk1.jedcore.ability.airbending.Meditate;
import com.jedk1.jedcore.ability.airbending.SonicBlast;
import com.jedk1.jedcore.ability.avatar.SpiritBeam;
import com.jedk1.jedcore.ability.avatar.elementsphere.ElementSphere;
import com.jedk1.jedcore.ability.chiblocking.DaggerThrow;
import com.jedk1.jedcore.ability.earthbending.EarthKick;
import com.jedk1.jedcore.ability.earthbending.EarthLine;
import com.jedk1.jedcore.ability.earthbending.EarthPillar;
import com.jedk1.jedcore.ability.earthbending.EarthShard;
import com.jedk1.jedcore.ability.earthbending.EarthSurf;
import com.jedk1.jedcore.ability.earthbending.Fissure;
import com.jedk1.jedcore.ability.earthbending.LavaDisc;
import com.jedk1.jedcore.ability.earthbending.LavaFlux;
import com.jedk1.jedcore.ability.earthbending.LavaThrow;
import com.jedk1.jedcore.ability.earthbending.MagnetShield;
import com.jedk1.jedcore.ability.earthbending.MetalArmor;
import com.jedk1.jedcore.ability.earthbending.MetalFragments;
import com.jedk1.jedcore.ability.earthbending.MetalHook;
import com.jedk1.jedcore.ability.earthbending.MetalShred;
import com.jedk1.jedcore.ability.earthbending.MudSurge;
import com.jedk1.jedcore.ability.earthbending.SandBlast;
import com.jedk1.jedcore.ability.earthbending.combo.Crevice;
import com.jedk1.jedcore.ability.earthbending.combo.MagmaBlast;
import com.jedk1.jedcore.ability.firebending.Combustion;
import com.jedk1.jedcore.ability.firebending.Discharge;
import com.jedk1.jedcore.ability.firebending.FireBall;
import com.jedk1.jedcore.ability.firebending.FireBreath;
import com.jedk1.jedcore.ability.firebending.FireComet;
import com.jedk1.jedcore.ability.firebending.FirePunch;
import com.jedk1.jedcore.ability.firebending.FireShots;
import com.jedk1.jedcore.ability.firebending.FireSki;
import com.jedk1.jedcore.ability.firebending.LightningBurst;
import com.jedk1.jedcore.ability.passive.WallRun;
import com.jedk1.jedcore.ability.waterbending.BloodPuppet;
import com.jedk1.jedcore.ability.waterbending.Drain;
import com.jedk1.jedcore.ability.waterbending.FrostBreath;
import com.jedk1.jedcore.ability.waterbending.IceClaws;
import com.jedk1.jedcore.ability.waterbending.IceWall;
import com.jedk1.jedcore.ability.waterbending.WakeFishing;
import com.jedk1.jedcore.ability.waterbending.combo.WaterFlow;
import com.jedk1.jedcore.ability.waterbending.combo.WaterGimbal;
import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.AvatarAbility;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;
import com.projectkorra.projectkorra.airbending.Suffocate;
import com.projectkorra.projectkorra.waterbending.blood.Bloodbending;
import org.bukkit.inventory.EquipmentSlot;

public class AbilityListener implements Listener {

	JedCore plugin;

	public AbilityListener(JedCore plugin) {
		this.plugin = plugin;
	}


	@EventHandler(priority = EventPriority.LOWEST)
	// Abilities that should bypass punch cancels should be handled here.
	public void onPlayerSwingBypassCancel(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (event.getHand() != EquipmentSlot.HAND) {
			return;
		}
		if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_AIR) {
			return;
		}
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return;
		}

		if (Suffocate.isBreathbent(player) || bPlayer.isChiBlocked()) {
			return;
		}

		if (Bloodbending.isBloodbent(player) || MovementHandler.isStopped(player)) {
			return;
		}

		CoreAbility coreAbil = bPlayer.getBoundAbility();
		if (coreAbil == null) {
			return;
		}

		if (!bPlayer.canBendIgnoreCooldowns(coreAbil)) {
			return;
		}

		if (coreAbil instanceof FireAbility && bPlayer.isElementToggled(Element.FIRE)) {
			if (GeneralMethods.isWeapon(player.getInventory().getItemInMainHand().getType()) && !ProjectKorra.plugin.getConfig().getBoolean("Properties.Fire.CanBendWithWeapons")) {
				return;
			}

			// FireSki bypasses punch cancel because the normal version is activated from sneaking alone.
			// The punch activation exists just so people don't accidentally activate it, so they should have the same
			// requirements for activation.
			if (coreAbil instanceof FireJet && FireSki.isPunchActivated(player.getWorld())) {
				if (player.isSneaking()) {
					FireSki ski = new FireSki(player);
					if (ski.isStarted() && !bPlayer.isOnCooldown("FireJet")) {
						// The event only needs to be cancelled when FireSki is set to have no cooldown.
						// This is to prevent FireJet from activating from the same swing event.
						event.setCancelled(true);
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerSwing(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (event.getHand() != EquipmentSlot.HAND) {
			return;
		}
		if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_AIR) {
			return;
		}
		if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.isCancelled()){
			return;
		}
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return;
		}

		if (Suffocate.isBreathbent(player)) {
			event.setCancelled(true);
			return;
		} else if (Bloodbending.isBloodbent(player) || MovementHandler.isStopped(player)) {
			event.setCancelled(true);
			return;
		} else if (bPlayer.isChiBlocked()) {
			event.setCancelled(true);
			return;
		} else if (GeneralMethods.isInteractable(player.getTargetBlock((Set<Material>)null, 5))) {
			return;
		}
		
		if (bPlayer.isToggled()) {
			new WallRun(player);
		}

		String abil = bPlayer.getBoundAbilityName();
		CoreAbility coreAbil = bPlayer.getBoundAbility();

		if (coreAbil == null && !MultiAbilityManager.hasMultiAbilityBound(player)) {
			return;
		} else if (bPlayer.canBendIgnoreCooldowns(coreAbil)) {

			if (coreAbil instanceof AirAbility && bPlayer.isElementToggled(Element.AIR) == true) {
				if (GeneralMethods.isWeapon(player.getInventory().getItemInMainHand().getType()) && !ProjectKorra.plugin.getConfig().getBoolean("Properties.Air.CanBendWithWeapons")) {
					return;
				}
				if (abil.equalsIgnoreCase("airblade")) {
					new AirBlade(player);
				}
				if (abil.equalsIgnoreCase("airpunch")) {
					new AirPunch(player);
				}
			}
			
			if (coreAbil instanceof EarthAbility && bPlayer.isElementToggled(Element.EARTH) == true) {
				if (GeneralMethods.isWeapon(player.getInventory().getItemInMainHand().getType()) && !ProjectKorra.plugin.getConfig().getBoolean("Properties.Earth.CanBendWithWeapons")) {
					return;
				}
				if (abil.equalsIgnoreCase("eartharmor")) {
					new MetalArmor(player);
				}
				if (abil.equalsIgnoreCase("earthline")) {
					EarthLine.shootLine(player);
				}
				if (abil.equalsIgnoreCase("earthshard")) {
					EarthShard.throwShard(player);
				}
				if (abil.equalsIgnoreCase("earthsurf")) {
					new EarthSurf(player);
				}
				if (abil.equalsIgnoreCase("fissure")) {
					new Fissure(player);
				}
				if (abil.equalsIgnoreCase("lavaflux")) {
					new LavaFlux(player);
				}
				if (abil.equalsIgnoreCase("lavathrow")) {
					new LavaThrow(player);
				}
				if (abil.equalsIgnoreCase("metalfragments")) {
					MetalFragments.shootFragment(player, true);
				}
				if (abil.equalsIgnoreCase("metalhook")) {
					new MetalHook(player);
				}
				if (abil.equalsIgnoreCase("metalshred")) {
					MetalShred.extend(player);
				}
				if (abil.equalsIgnoreCase("mudsurge")) {
					MudSurge.mudSurge(player);
				}
				if (abil.equalsIgnoreCase("sandblast")) {
					SandBlast.blastSand(player);
				}
				if (abil.equalsIgnoreCase("lavaflow")) {
					MagmaBlast.performAction(player);
				}
			}

			if (coreAbil instanceof FireAbility && bPlayer.isElementToggled(Element.FIRE) == true) {
				if (GeneralMethods.isWeapon(player.getInventory().getItemInMainHand().getType()) && !ProjectKorra.plugin.getConfig().getBoolean("Properties.Fire.CanBendWithWeapons")) {
					return;
				}
				if (abil.equalsIgnoreCase("combustion")) {
					Combustion.combust(player);
				}
				if (abil.equalsIgnoreCase("discharge")) {
					new Discharge(player);
				}
				if (abil.equalsIgnoreCase("fireball")) {
					new FireBall(player);
				}
				if (abil.equalsIgnoreCase("firepunch")) {
					new FirePunch(player);
				}
				if (abil.equalsIgnoreCase("fireshots")) {
					FireShots.fireShot(player);
				}
			}
			
			if (coreAbil instanceof WaterAbility && bPlayer.isElementToggled(Element.WATER) == true) {
				if (GeneralMethods.isWeapon(player.getInventory().getItemInMainHand().getType()) && !ProjectKorra.plugin.getConfig().getBoolean("Properties.WATER.CanBendWithWeapons")) {
					return;
				}
				if (abil.equalsIgnoreCase("bloodbending")) {
					com.jedk1.jedcore.ability.waterbending.Bloodbending.launch(player);
				}
				if (abil.equalsIgnoreCase("bloodpuppet")) {
					BloodPuppet.attack(player);
				}
				if (abil.equalsIgnoreCase("iceclaws")) {
					IceClaws.throwClaws(player);
				}
				if (abil.equalsIgnoreCase("drain")) {
					Drain.fireBlast(player);
				}
				if (abil.equalsIgnoreCase("octopusform")) {
					WaterGimbal.prepareBlast(player);
				}
				if (abil.equalsIgnoreCase("watermanipulation")) {
					WaterFlow.freeze(player);
				}
			}
			
			if (coreAbil instanceof ChiAbility && bPlayer.isElementToggled(Element.CHI) == true) {
				if (GeneralMethods.isWeapon(player.getInventory().getItemInMainHand().getType()) && !ProjectKorra.plugin.getConfig().getBoolean("Properties.Chi.CanBendWithWeapons")) {
					return;
				}
				if (abil.equalsIgnoreCase("daggerthrow")) {
					new DaggerThrow(player);
				}
			}
			
			if (coreAbil instanceof AvatarAbility) {
				if (abil.equalsIgnoreCase("elementsphere")) {
					new ElementSphere(player);
				}
			}
		}
		
		if(MultiAbilityManager.hasMultiAbilityBound(player)){
			abil = MultiAbilityManager.getBoundMultiAbility(player);
			if (abil.equalsIgnoreCase("elementsphere")) {
				new ElementSphere(player);
			}
		}
	}
	
	public static ConcurrentHashMap<UUID, Long> recent = new ConcurrentHashMap<UUID, Long>();

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerSneak(PlayerToggleSneakEvent event) {
		Player player = event.getPlayer();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (event.isCancelled() || bPlayer == null) {
			return;
		}

		String abilName = bPlayer.getBoundAbilityName();
		if (Suffocate.isBreathbent(player)) {
			if (!abilName.equalsIgnoreCase("AirSwipe") 
					|| !abilName.equalsIgnoreCase("FireBlast") 
					|| !abilName.equalsIgnoreCase("EarthBlast") 
					|| !abilName.equalsIgnoreCase("WaterManipulation")) {
				if(!player.isSneaking()) {
					event.setCancelled(true);
				}
			}
		}

		if (MovementHandler.isStopped(player) || Bloodbending.isBloodbent(player)) {
			event.setCancelled(true);
			return;
		}

		CoreAbility coreAbil = bPlayer.getBoundAbility();
		String abil = bPlayer.getBoundAbilityName();
		if (coreAbil == null) {
			return;
		}

		if (bPlayer.isChiBlocked()) {
			event.setCancelled(true);
			return;
		}

		if (!player.isSneaking() && bPlayer.canBendIgnoreCooldowns(coreAbil)) {
			if (coreAbil instanceof AirAbility && bPlayer.isElementToggled(Element.AIR) == true) {
				if (GeneralMethods.isWeapon(player.getInventory().getItemInMainHand().getType()) && !plugin.getConfig().getBoolean("Properties.Air.CanBendWithWeapons")) {
					return;
				}
				if (abil.equalsIgnoreCase("airbreath")) {
					new AirBreath(player);
				}
				if (abil.equalsIgnoreCase("airglide")) {
					new AirGlide(player);
				}
				if (abil.equalsIgnoreCase("meditate")) {
					new Meditate(player);
				}
				if (abil.equalsIgnoreCase("sonicblast")) {
					new SonicBlast(player);
				}
			}
			
			if (coreAbil instanceof EarthAbility && bPlayer.isElementToggled(Element.EARTH) == true) {
				if (GeneralMethods.isWeapon(player.getInventory().getItemInMainHand().getType()) && !ProjectKorra.plugin.getConfig().getBoolean("Properties.Earth.CanBendWithWeapons")) {
					return;
				}
				if (abil.equalsIgnoreCase("earthkick")) {
					new EarthKick(player);
				}
				if (abil.equalsIgnoreCase("earthline")) {
					new EarthLine(player);
				}
				if (abil.equalsIgnoreCase("earthpillar")) {
					new EarthPillar(player);
				}
				if (abil.equalsIgnoreCase("earthshard")) {
					new EarthShard(player);
				}
				if (abil.equalsIgnoreCase("fissure")) {
					Fissure.performAction(player);
				}
				if (abil.equalsIgnoreCase("lavadisc")) {
					new LavaDisc(player);
				}
				if (abil.equalsIgnoreCase("magnetshield")) {
					new MagnetShield(player);
				}
				if (abil.equalsIgnoreCase("metalfragments")) {
					new MetalFragments(player);
				}
				if (abil.equalsIgnoreCase("metalshred")) {
					new MetalShred(player);
				}
				if (abil.equalsIgnoreCase("mudsurge")) {
					new MudSurge(player);
				}
				if (abil.equalsIgnoreCase("sandblast")) {
					new SandBlast(player);
				}
				if (abil.equalsIgnoreCase("shockwave")) {
					Crevice.closeCrevice(player);
				}
			}
			
			if (coreAbil instanceof FireAbility && bPlayer.isElementToggled(Element.FIRE) == true) {
				if (GeneralMethods.isWeapon(player.getInventory().getItemInMainHand().getType()) && !ProjectKorra.plugin.getConfig().getBoolean("Properties.Fire.CanBendWithWeapons")) {
					return;
				}
				if (abil.equalsIgnoreCase("combustion")) {
					new Combustion(event.getPlayer());
				}
				if (abil.equalsIgnoreCase("firebreath")) {
					new FireBreath(player);
				}
				if (abil.equalsIgnoreCase("firecomet")) {
					new FireComet(player);
				}
				if (abil.equalsIgnoreCase("firejet")) {
					if (FireSki.isPunchActivated(player.getWorld())) {
						FireSki fs = CoreAbility.getAbility(player, FireSki.class);

						if (fs != null) {
							fs.remove();
						}
					} else {
						new FireSki(player);
					}
				}
				if (abil.equalsIgnoreCase("fireshots")) {
					new FireShots(player);
				}
				if (abil.equalsIgnoreCase("lightningburst")) {
					new LightningBurst(player);
				}
			}
			
			if (coreAbil instanceof WaterAbility && bPlayer.isElementToggled(Element.WATER) == true) {
				if (GeneralMethods.isWeapon(player.getInventory().getItemInMainHand().getType()) && !ProjectKorra.plugin.getConfig().getBoolean("Properties.Water.CanBendWithWeapons")) {
					return;
				}
				if (abil.equalsIgnoreCase("bloodbending")) {
					new com.jedk1.jedcore.ability.waterbending.Bloodbending(player);
				}
				if (abil.equalsIgnoreCase("bloodpuppet")) {
					new BloodPuppet(player);
				}
				if (abil.equalsIgnoreCase("frostbreath")) {
					new FrostBreath(player);
				}
				if (abil.equalsIgnoreCase("iceclaws")) {
					new IceClaws(player);
				}
				if (abil.equalsIgnoreCase("icewall")) {
					new IceWall(player);
				}
				if (abil.equalsIgnoreCase("drain")) {
					new Drain(player);
				}
				if (abil.equalsIgnoreCase("wakefishing")) {
					new WakeFishing(player);
				}
			}
			
			if (coreAbil instanceof AvatarAbility) {
				if (abil.equalsIgnoreCase("spiritbeam")) {
					new SpiritBeam(player);
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteraction(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			MetalFragments.shootFragment(event.getPlayer(), false);
		}
	}
}
