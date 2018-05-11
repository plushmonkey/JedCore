package com.jedk1.jedcore.configuration;

import com.jedk1.jedcore.JedCore;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;

public class JedCoreConfig {

	public static Config board;
	static JedCore plugin;
	
	public JedCoreConfig(JedCore plugin) {
		JedCoreConfig.plugin = plugin;
		board = new Config(new File("board.yml"));
		loadConfigBoard();
		loadConfigCore();
	}
	
	private void loadConfigBoard() {
		FileConfiguration config;
		config = board.getConfig();
		
		config.addDefault("Settings.Enabled", true);
		config.addDefault("Settings.Title", "&lSlots");
		config.addDefault("Settings.EmptySlot", "&8&o-- Slot % --");
		config.addDefault("Settings.Combos", "&fCombos:");
		config.addDefault("Settings.Toggle.Off", "&7You have hidden the bending board.");
		config.addDefault("Settings.Toggle.On", "&7You have toggled the bending board on.");
		config.addDefault("Settings.Display.DisabledWorlds", true);

		config.addDefault("Settings.OtherCooldowns.WallRun.Color", "GOLD");
		config.addDefault("Settings.OtherCooldowns.WallRun.Enabled", true);
		config.addDefault("Settings.OtherCooldowns.TorrentWave.Color", "AQUA");
		config.addDefault("Settings.OtherCooldowns.TorrentWave.Enabled", true);
		config.addDefault("Settings.OtherCooldowns.SurgeWave.Color", "AQUA");
		config.addDefault("Settings.OtherCooldowns.SurgeWave.Enabled", true);
		config.addDefault("Settings.OtherCooldowns.SurgeWall.Color", "AQUA");
		config.addDefault("Settings.OtherCooldowns.SurgeWall.Enabled", true);
		config.addDefault("Settings.OtherCooldowns.RaiseEarthPillar.Color", "GREEN");
		config.addDefault("Settings.OtherCooldowns.RaiseEarthPillar.Enabled", true);
		config.addDefault("Settings.OtherCooldowns.RaiseEarthWall.Color", "GREEN");
		config.addDefault("Settings.OtherCooldowns.RaiseEarthWall.Enabled", true);
		
		config.options().copyDefaults(true);
		board.saveConfig();
	}
	
	private void loadConfigCore() {
		FileConfiguration config;
		config = JedCore.plugin.getConfig();
		
		config.addDefault("Settings.Updater.Check", true);
		config.addDefault("Settings.Updater.Notify", true);
		config.addDefault("Properties.MobCollisions.Enabled", true);
		config.addDefault("Properties.AbilityCollisions.Enabled", true);
		config.addDefault("Properties.PerWorldConfig", true);
		config.addDefault("Properties.FireTickMethod", "larger");
		config.addDefault("Properties.LogDebug", false);

		config.addDefault("Properties.ChiRestrictor.Enabled", false);
		config.addDefault("Properties.ChiRestrictor.ResetCooldown", true);
		config.addDefault("Properties.ChiRestrictor.MeleeDistance", 7);
		config.addDefault("Properties.ChiRestrictor.Whitelist", new ArrayList<String>());

		config.addDefault("Properties.CooldownEnforcer.Enabled", false);
		config.addDefault("Properties.CooldownEnforcer.OnReload", true);
		
		config.addDefault("Abilities.Avatar.ElementSphere.Enabled", true);
		config.addDefault("Abilities.Avatar.ElementSphere.Description", "ElementSphere is a very all round ability, being "
				+ "able to shoot attacks of each element, each with a "
				+ "different affect. To use, simply Left-Click. Once active, "
				+ "Sneak (Default: Shift) to fly around. Sneak and double "
				+ "Left-Click to disable the ability! "
				+ "To use each element, simply select hotbar slots 1-4 and Left-Click. "
				+ "Each element has limited uses! Once an element is used up, "
				+ "the element's ring will disappear!");
		config.addDefault("Abilities.Avatar.ElementSphere.Cooldown", 180000);
		config.addDefault("Abilities.Avatar.ElementSphere.Duration", 60000);
		config.addDefault("Abilities.Avatar.ElementSphere.MaxControlledHeight", 40);
		config.addDefault("Abilities.Avatar.ElementSphere.FlySpeed", 1.5);
		config.addDefault("Abilities.Avatar.ElementSphere.Air.Cooldown", 500);
		config.addDefault("Abilities.Avatar.ElementSphere.Air.Range", 40);
		config.addDefault("Abilities.Avatar.ElementSphere.Air.Uses", 20);
		config.addDefault("Abilities.Avatar.ElementSphere.Air.Damage", 3.0);
		config.addDefault("Abilities.Avatar.ElementSphere.Air.Knockback", 2);
		config.addDefault("Abilities.Avatar.ElementSphere.Air.Speed", 3);
		config.addDefault("Abilities.Avatar.ElementSphere.Earth.Cooldown", 500);
		config.addDefault("Abilities.Avatar.ElementSphere.Earth.Uses", 20);
		config.addDefault("Abilities.Avatar.ElementSphere.Earth.Damage", 3.0);
		config.addDefault("Abilities.Avatar.ElementSphere.Earth.ImpactCraterSize", 3);
		config.addDefault("Abilities.Avatar.ElementSphere.Earth.ImpactRevert", 15000);
		config.addDefault("Abilities.Avatar.ElementSphere.Fire.Cooldown", 500);
		config.addDefault("Abilities.Avatar.ElementSphere.Fire.Range", 40);
		config.addDefault("Abilities.Avatar.ElementSphere.Fire.Uses", 20);
		config.addDefault("Abilities.Avatar.ElementSphere.Fire.Damage", 3.0);
		config.addDefault("Abilities.Avatar.ElementSphere.Fire.BurnDuration", 3000);
		config.addDefault("Abilities.Avatar.ElementSphere.Fire.Speed", 3);
		config.addDefault("Abilities.Avatar.ElementSphere.Fire.Controllable", false);
		config.addDefault("Abilities.Avatar.ElementSphere.Water.Cooldown", 500);
		config.addDefault("Abilities.Avatar.ElementSphere.Water.Range", 40);
		config.addDefault("Abilities.Avatar.ElementSphere.Water.Uses", 20);
		config.addDefault("Abilities.Avatar.ElementSphere.Water.Damage", 3.0);
		config.addDefault("Abilities.Avatar.ElementSphere.Water.Speed", 3);
		config.addDefault("Abilities.Avatar.ElementSphere.Stream.Cooldown", 500);
		config.addDefault("Abilities.Avatar.ElementSphere.Stream.Range", 40);
		config.addDefault("Abilities.Avatar.ElementSphere.Stream.Knockback", 2.0);
		config.addDefault("Abilities.Avatar.ElementSphere.Stream.Damage", 12.0);
		config.addDefault("Abilities.Avatar.ElementSphere.Stream.RequiredUses", 10);
		config.addDefault("Abilities.Avatar.ElementSphere.Stream.EndAbility", true);
		config.addDefault("Abilities.Avatar.ElementSphere.Stream.ImpactCraterSize", 3);
		config.addDefault("Abilities.Avatar.ElementSphere.Stream.ImpactRevert", 30000);
		
		config.addDefault("Abilities.Avatar.SpiritBeam.Enabled", true);
		config.addDefault("Abilities.Avatar.SpiritBeam.Description", "An energybending ability usable by the Avatar. "
				+ "To use, one must enter the AvatarState and hold down Sneak (Default: Shift). "
			+ "This ability lasts only for a few seconds before requiring "
			+ "another activation.");
		config.addDefault("Abilities.Avatar.SpiritBeam.Cooldown", 15000);
		config.addDefault("Abilities.Avatar.SpiritBeam.Duration", 1000);
		config.addDefault("Abilities.Avatar.SpiritBeam.Range", 40);
		config.addDefault("Abilities.Avatar.SpiritBeam.Damage", 10.0);
		config.addDefault("Abilities.Avatar.SpiritBeam.AvatarStateOnly", true);
		config.addDefault("Abilities.Avatar.SpiritBeam.BlockDamage.Enabled", true);
		config.addDefault("Abilities.Avatar.SpiritBeam.BlockDamage.Radius", 3);
		config.addDefault("Abilities.Avatar.SpiritBeam.BlockDamage.Regen", 20000);
		
		config.addDefault("Abilities.Air.AirBlade.Enabled", true);
		config.addDefault("Abilities.Air.AirBlade.Description", "With this ability bound, Left-Click to shoot "
				+ "a strong blade of air at your targets doing some damage!");
		config.addDefault("Abilities.Air.AirBlade.Cooldown", 3000);
		config.addDefault("Abilities.Air.AirBlade.Range", 30.0);
		config.addDefault("Abilities.Air.AirBlade.Damage", 4.0);
		config.addDefault("Abilities.Air.AirBlade.EntityCollisionRadius", 1.0);
		config.addDefault("Abilities.Air.AirBlade.AbilityCollisionRadius", 1.0);
		config.addDefault("Abilities.Air.AirBlade.Collisions.FireBlast.Enabled",true);
		config.addDefault("Abilities.Air.AirBlade.Collisions.FireBlast.RemoveFirst",true);
		config.addDefault("Abilities.Air.AirBlade.Collisions.FireBlast.RemoveSecond",true);
		config.addDefault("Abilities.Air.AirBlade.Collisions.FireBlastCharged.Enabled",true);
		config.addDefault("Abilities.Air.AirBlade.Collisions.FireBlastCharged.RemoveFirst",true);
		config.addDefault("Abilities.Air.AirBlade.Collisions.FireBlastCharged.RemoveSecond",false);
		
		config.addDefault("Abilities.Air.AirBreath.Enabled", true);
		config.addDefault("Abilities.Air.AirBreath.Description", "To use, hold Sneak (Default: Shift) to release "
				+ "a strong breath of wind knocking your opponents "
				+ "back. This ability also has a longer range and "
				+ "stronger knockback while in AvatarState!");
		config.addDefault("Abilities.Air.AirBreath.Cooldown", 3000);
		config.addDefault("Abilities.Air.AirBreath.Duration", 3000);
		config.addDefault("Abilities.Air.AirBreath.Particles", 3);
		config.addDefault("Abilities.Air.AirBreath.AffectBlocks.Lava", true);
		config.addDefault("Abilities.Air.AirBreath.AffectBlocks.Fire", true);
		config.addDefault("Abilities.Air.AirBreath.ExtinguishEntities", true);
		config.addDefault("Abilities.Air.AirBreath.Damage.Enabled", false);
		config.addDefault("Abilities.Air.AirBreath.Damage.Player", 1.0);
		config.addDefault("Abilities.Air.AirBreath.Damage.Mob", 2.0);
		config.addDefault("Abilities.Air.AirBreath.Knockback", 0.8);
		config.addDefault("Abilities.Air.AirBreath.Range", 10);
		config.addDefault("Abilities.Air.AirBreath.LaunchPower", 1.0);
		config.addDefault("Abilities.Air.AirBreath.RegenTargetOxygen", true);
		config.addDefault("Abilities.Air.AirBreath.Avatar.Enabled", true);
		config.addDefault("Abilities.Air.AirBreath.Avatar.Range", 20);
		config.addDefault("Abilities.Air.AirBreath.Avatar.Knockback", 3.5);
		
		config.addDefault("Abilities.Air.AirGlide.Enabled", true);
		config.addDefault("Abilities.Air.AirGlide.Description", "While falling, tap Sneak for a "
				+ "slow and steady descent, tap Sneak again to stop gliding.");
		config.addDefault("Abilities.Air.AirGlide.Speed", 0.5);
		config.addDefault("Abilities.Air.AirGlide.FallSpeed", 0.1);
		config.addDefault("Abilities.Air.AirGlide.Particles", 4);
		config.addDefault("Abilities.Air.AirGlide.AllowAirSpout", false);
		config.addDefault("Abilities.Air.AirGlide.Cooldown", 0);
		config.addDefault("Abilities.Air.AirGlide.Duration", 0);
		config.addDefault("Abilities.Air.AirGlide.RequireGround", false);
		
		config.addDefault("Abilities.Air.AirPunch.Enabled", true);
		config.addDefault("Abilities.Air.AirPunch.Description", "Left-Click in rapid succession to punch high desnity packets of air "
				+ "at enemies to do slight damage to them. A few punches can be thrown before the ability has a cooldown.");
		config.addDefault("Abilities.Air.AirPunch.Cooldown", 5000);
		config.addDefault("Abilities.Air.AirPunch.Threshold", 500);
		config.addDefault("Abilities.Air.AirPunch.Shots", 4);
		config.addDefault("Abilities.Air.AirPunch.Range", 30);
		config.addDefault("Abilities.Air.AirPunch.Damage", 1.0);
		config.addDefault("Abilities.Air.AirPunch.EntityCollisionRadius", 1.0);
		config.addDefault("Abilities.Air.AirPunch.AbilityCollisionRadius", 1.0);
		config.addDefault("Abilities.Air.AirPunch.Collisions.FireBlast.Enabled", true);
		config.addDefault("Abilities.Air.AirPunch.Collisions.FireBlast.RemoveFirst", true);
		config.addDefault("Abilities.Air.AirPunch.Collisions.FireBlast.RemoveSecond", false);
		config.addDefault("Abilities.Air.AirPunch.Collisions.FireBlastCharged.Enabled",true);
		config.addDefault("Abilities.Air.AirPunch.Collisions.FireBlastCharged.RemoveFirst",true);
		config.addDefault("Abilities.Air.AirPunch.Collisions.FireBlastCharged.RemoveSecond",false);
		config.addDefault("Abilities.Air.AirPunch.Collisions.AirBlade.Enabled", true);
		config.addDefault("Abilities.Air.AirPunch.Collisions.AirBlade.RemoveFirst", true);
		config.addDefault("Abilities.Air.AirPunch.Collisions.AirBlade.RemoveSecond", false);
		
		config.addDefault("Abilities.Air.Meditate.Enabled", true);
		config.addDefault("Abilities.Air.Meditate.Description", "Hold Sneak (Default: Shift) to start meditating. "
				+ "After you have focused your energy, you will obtain several buffs.");
		config.addDefault("Abilities.Air.Meditate.UnfocusMessage", "You have become unfocused from taking damage!");
		config.addDefault("Abilities.Air.Meditate.LossFocusMessage", true);
		config.addDefault("Abilities.Air.Meditate.ChargeTime", 5000);
		config.addDefault("Abilities.Air.Meditate.Cooldown", 60000);
		config.addDefault("Abilities.Air.Meditate.BoostDuration", 20000);
		config.addDefault("Abilities.Air.Meditate.ParticleDensity", 5);
		config.addDefault("Abilities.Air.Meditate.AbsorptionBoost", 2);
		config.addDefault("Abilities.Air.Meditate.SpeedBoost", 3);
		config.addDefault("Abilities.Air.Meditate.JumpBoost", 3);
		
		config.addDefault("Abilities.Air.SonicBlast.Enabled", true);
		config.addDefault("Abilities.Air.SonicBlast.Description", "SonicBlast is a soundbending ability, known by very few airbenders. "
			+ "It allows the airbender to stun and deafen an opponent by creating a sonic blast, "
			+ "this is achieved by creating two regions of high and low pressure and bringing them together. "
			+ "To use, hold Sneak (Default: Shift) in the direction of the target. Once particles start appearing "
			+ "around you, let go of Sneak to shoot a SonicBlast at your target! The technique is very powerful, "
			+ "even if it doesn't seem it, and comes with a short cooldown.");
		config.addDefault("Abilities.Air.SonicBlast.ChargeTime", 2000);
		config.addDefault("Abilities.Air.SonicBlast.Damage", 4.0);
		config.addDefault("Abilities.Air.SonicBlast.Effects.BlindnessDuration", 5000);
		config.addDefault("Abilities.Air.SonicBlast.Effects.NauseaDuration", 5000);
		config.addDefault("Abilities.Air.SonicBlast.Cooldown", 6000);
		config.addDefault("Abilities.Air.SonicBlast.EntityCollisionRadius", 1.3);
		config.addDefault("Abilities.Air.SonicBlast.AbilityCollisionRadius", 1.3);
		config.addDefault("Abilities.Air.SonicBlast.Range", 20);
		config.addDefault("Abilities.Air.SonicBlast.ChargeSwapping", true);
		
		config.addDefault("Abilities.Air.AirCombo.AirSlam.Enabled", true);
		config.addDefault("Abilities.Air.AirCombo.AirSlam.Description", "Kick your enemy up into the air then blast them away!");
		config.addDefault("Abilities.Air.AirCombo.AirSlam.Cooldown", 8000);
		config.addDefault("Abilities.Air.AirCombo.AirSlam.Power", 5.0);
		config.addDefault("Abilities.Air.AirCombo.AirSlam.Range", 8);
		
		config.addDefault("Abilities.Air.AirCombo.SwiftStream.Enabled", true);
		config.addDefault("Abilities.Air.AirCombo.SwiftStream.Description", "Create a stream of air as you fly which causes nearby "
				+ "entities to be thrown in your direction.");
		config.addDefault("Abilities.Air.AirCombo.SwiftStream.DragFactor", 1.5);
		config.addDefault("Abilities.Air.AirCombo.SwiftStream.Duration", 2000);
		config.addDefault("Abilities.Air.AirCombo.SwiftStream.Cooldown", 6000);
		
		config.addDefault("Abilities.Earth.EarthArmor.Enabled", true);
		config.addDefault("Abilities.Earth.EarthArmor.Description", "If the block is metal, then you will get metal armor!");
		config.addDefault("Abilities.Earth.EarthArmor.Resistance.Strength", 2);
		config.addDefault("Abilities.Earth.EarthArmor.Resistance.Duration", 4000);
		config.addDefault("Abilities.Earth.EarthArmor.UseIronArmor", false);
		
		config.addDefault("Abilities.Earth.EarthKick.Enabled", true);
		config.addDefault("Abilities.Earth.EarthKick.Description", "This move enables an earthbender to create a "
				+ "large earthen cover, ideal for defense. "
				+ "To use, Sneak (Default: Shift) at an earth "
				+ "source and it will raise and launch towards "
				+ "your foe!");
		config.addDefault("Abilities.Earth.EarthKick.Cooldown", 2000);
		config.addDefault("Abilities.Earth.EarthKick.EarthBlocks", 10);
		config.addDefault("Abilities.Earth.EarthKick.Damage", 2.0);
		config.addDefault("Abilities.Earth.EarthKick.EntityCollisionRadius", 1.5);
		config.addDefault("Abilities.Earth.EarthKick.AbilityCollisionRadius", 1.5);
		config.addDefault("Abilities.Earth.EarthKick.Collisions.FireBlast.Enabled", true);
		config.addDefault("Abilities.Earth.EarthKick.Collisions.FireBlast.RemoveFirst", false);
		config.addDefault("Abilities.Earth.EarthKick.Collisions.FireBlast.RemoveSecond", true);
		config.addDefault("Abilities.Earth.EarthKick.Collisions.EarthBlast.Enabled", true);
		config.addDefault("Abilities.Earth.EarthKick.Collisions.EarthBlast.RemoveFirst", false);
		config.addDefault("Abilities.Earth.EarthKick.Collisions.EarthBlast.RemoveSecond", true);
		config.addDefault("Abilities.Earth.EarthKick.Collisions.WaterManipulation.Enabled", true);
		config.addDefault("Abilities.Earth.EarthKick.Collisions.WaterManipulation.RemoveFirst", false);
		config.addDefault("Abilities.Earth.EarthKick.Collisions.WaterManipulation.RemoveSecond", true);
		config.addDefault("Abilities.Earth.EarthKick.Collisions.AirSwipe.Enabled", true);
		config.addDefault("Abilities.Earth.EarthKick.Collisions.AirSwipe.RemoveFirst", false);
		config.addDefault("Abilities.Earth.EarthKick.Collisions.AirSwipe.RemoveSecond", true);
		config.addDefault("Abilities.Earth.EarthKick.Collisions.Combustion.Enabled", true);
		config.addDefault("Abilities.Earth.EarthKick.Collisions.Combustion.RemoveFirst", false);
		config.addDefault("Abilities.Earth.EarthKick.Collisions.Combustion.RemoveSecond", true);
		config.addDefault("Abilities.Earth.EarthKick.Collisions.WaterSpout.Enabled", true);
		config.addDefault("Abilities.Earth.EarthKick.Collisions.WaterSpout.RemoveFirst", false);
		config.addDefault("Abilities.Earth.EarthKick.Collisions.WaterSpout.RemoveSecond", true);
		config.addDefault("Abilities.Earth.EarthKick.Collisions.AirSpout.Enabled", true);
		config.addDefault("Abilities.Earth.EarthKick.Collisions.AirSpout.RemoveFirst", false);
		config.addDefault("Abilities.Earth.EarthKick.Collisions.AirSpout.RemoveSecond", true);
		config.addDefault("Abilities.Earth.EarthKick.Collisions.AirWheel.Enabled", true);
		config.addDefault("Abilities.Earth.EarthKick.Collisions.AirWheel.RemoveFirst", false);
		config.addDefault("Abilities.Earth.EarthKick.Collisions.AirWheel.RemoveSecond", true);


		config.addDefault("Abilities.Earth.EarthLine.Enabled", true);
		config.addDefault("Abilities.Earth.EarthLine.Description", "To use, place your cursor over an earth-bendable block on the ground, "
			+ "then Sneak (Default: Shift) to select the block. After selecting the block you may release Sneak. "
			+ "If you then Left-Click at an object or player, a small piece of earth will come up "
			+ "from the ground and move towards your target to deal damage and knock them back. "
			+ "Additionally, hold Sneak to control the flow of the line!");
		config.addDefault("Abilities.Earth.EarthLine.Cooldown", 3000);
		config.addDefault("Abilities.Earth.EarthLine.Range", 30);
		config.addDefault("Abilities.Earth.EarthLine.PrepareRange", 7);
		config.addDefault("Abilities.Earth.EarthLine.AffectingRadius", 2);
		config.addDefault("Abilities.Earth.EarthLine.Damage", 3.0);
		
		config.addDefault("Abilities.Earth.EarthPillar.Enabled", true);
		config.addDefault("Abilities.Earth.EarthPillar.Description", "With this ability bound, tap Sneak (Default: Shift) on any Earthbendable "
				+ "surface to create pillar of earth in the direction of the block face!");
		config.addDefault("Abilities.Earth.EarthPillar.Height", 6);
		config.addDefault("Abilities.Earth.EarthPillar.Range", 10);
		
		config.addDefault("Abilities.Earth.EarthShard.Enabled", true);
		config.addDefault("Abilities.Earth.EarthShard.Description", "EarthShard is a variation of EarthBlast "
				+ "which the earthbender may use to hit a target. This "
				+ "ability deals a fair amount of damage and is easy to "
				+ "rapid-fire. To use, simply shift at an earthbendable block, "
				+ "and it will ascend to your eye height. Then, click towards your "
				+ "target and the block will launch itself towards it.");
		config.addDefault("Abilities.Earth.EarthShard.Cooldown", 1000);
		config.addDefault("Abilities.Earth.EarthShard.Damage.Normal", 1.0);
		config.addDefault("Abilities.Earth.EarthShard.Damage.Metal", 1.5);
		config.addDefault("Abilities.Earth.EarthShard.PrepareRange", 5);
		config.addDefault("Abilities.Earth.EarthShard.AbilityRange", 30);
		config.addDefault("Abilities.Earth.EarthShard.MaxShards", 3);
		config.addDefault("Abilities.Earth.EarthShard.AbilityCollisionRadius", 2.0);
		config.addDefault("Abilities.Earth.EarthShard.EntityCollisionRadius", 1.4);
		
		config.addDefault("Abilities.Earth.EarthSurf.Enabled", true);
		config.addDefault("Abilities.Earth.EarthSurf.Description", "This ability allows an earth bender to "
				+ "ride up on a wave of earth, allowing them to travel a little faster than "
				+ "normal. To use, simply be in the air just above "
				+ "the ground, and Left Click! Additionally, if an entity just so happens to get caught in "
				+ "the wave, they will be moved with the wave.");
		config.addDefault("Abilities.Earth.EarthSurf.Cooldown.Cooldown", 3000);
		config.addDefault("Abilities.Earth.EarthSurf.Cooldown.MinimumCooldown", 2000);
		config.addDefault("Abilities.Earth.EarthSurf.Cooldown.Scaled", true);
		config.addDefault("Abilities.Earth.EarthSurf.Cooldown.Enabled", false);
		config.addDefault("Abilities.Earth.EarthSurf.Duration.Duration", 7000);
		config.addDefault("Abilities.Earth.EarthSurf.Duration.Enabled", false);
		config.addDefault("Abilities.Earth.EarthSurf.RelaxedCollisions", true);
		config.addDefault("Abilities.Earth.EarthSurf.RemoveOnAnyDamage", false);
		config.addDefault("Abilities.Earth.EarthSurf.Speed", 0.55);
		config.addDefault("Abilities.Earth.EarthSurf.HeightTolerance", 3);
		config.addDefault("Abilities.Earth.EarthSurf.SpringStiffness", 0.35);
		
		config.addDefault("Abilities.Earth.Fissure.Enabled", true);
		config.addDefault("Abilities.Earth.Fissure.Description", "Fissure is an advanced Lavabending "
				+ "ability enabling a lavabender to tear up the ground, "
			+ "swallowing up any enemies. To use, simply swing at an enemy "
			+ "and a line of lava will crack open. "
			+ "Then, tap Sneak (Default: Shift) to expand the crevice. "
			+ "The crevice has a maximum width and depth. Once the crevice has reached it's maximum "
			+ "width, Sneak while looking at the crevice to close it!");
		config.addDefault("Abilities.Earth.Fissure.Cooldown", 20000);
		config.addDefault("Abilities.Earth.Fissure.Duration", 15000);
		config.addDefault("Abilities.Earth.Fissure.MaxWidth", 3);
		config.addDefault("Abilities.Earth.Fissure.SlapRange", 12);
		config.addDefault("Abilities.Earth.Fissure.SlapDelay", 50);
		
		config.addDefault("Abilities.Earth.LavaDisc.Enabled", true);
		config.addDefault("Abilities.Earth.LavaDisc.Description", "Hold Sneak (Default: Shift) on a lava source "
				+ "block to generate a disc of lava at your finger tips. Releasing "
				+ "Sneak will shoot the disc off in the direction "
				+ "you are looking! If you tap or hold Sneak again, "
				+ "the disc will attempt to return to you!");
		String[] meltable = {Material.COBBLESTONE.name(), Material.LOG.name(), Material.LOG_2.name()};
		config.addDefault("Abilities.Earth.LavaDisc.Cooldown", 7000);
		config.addDefault("Abilities.Earth.LavaDisc.Duration", 1000);
		config.addDefault("Abilities.Earth.LavaDisc.Damage", 4.0);
		config.addDefault("Abilities.Earth.LavaDisc.Particles", 3);
		config.addDefault("Abilities.Earth.LavaDisc.ContinueAfterEntityHit", false);
		config.addDefault("Abilities.Earth.LavaDisc.RecallLimit", 3);
		config.addDefault("Abilities.Earth.LavaDisc.Destroy.RegenTime", 5000);
		config.addDefault("Abilities.Earth.LavaDisc.Destroy.BlockDamage", true);
		config.addDefault("Abilities.Earth.LavaDisc.Destroy.AdditionalMeltableBlocks", meltable);
		config.addDefault("Abilities.Earth.LavaDisc.Destroy.LavaTrail", true);
		config.addDefault("Abilities.Earth.LavaDisc.Destroy.TrailFlow", false);
		config.addDefault("Abilities.Earth.LavaDisc.Source.RegenTime", 10000);
		config.addDefault("Abilities.Earth.LavaDisc.Source.LavaOnly", false);
		config.addDefault("Abilities.Earth.LavaDisc.Source.Range", 4.0);
		
		config.addDefault("Abilities.Earth.LavaFlux.Enabled", true);
		config.addDefault("Abilities.Earth.LavaFlux.Description", "This offensive ability enables a Lavabender to create a wave of lava, "
				+ "swiftly progressing forward and hurting/burning anything in its way. To use, "
				+ "simply swing your arm towards a target and the ability will activate.");
		config.addDefault("Abilities.Earth.LavaFlux.Range", 12);
		config.addDefault("Abilities.Earth.LavaFlux.Cooldown", 8000);
		config.addDefault("Abilities.Earth.LavaFlux.Duration", 4000);
		config.addDefault("Abilities.Earth.LavaFlux.Cleanup", 1000);
		config.addDefault("Abilities.Earth.LavaFlux.Damage", 1.0);
		config.addDefault("Abilities.Earth.LavaFlux.Speed", 1);
		config.addDefault("Abilities.Earth.LavaFlux.Wave", true);
		
		config.addDefault("Abilities.Earth.LavaThrow.Enabled", true);
		config.addDefault("Abilities.Earth.LavaThrow.Description", "Throwing lava is a fundamental technique for the rare subskill. "
				+ "Use Sneak(Deafult: Shift) while looking at a pool of lava infront of you, then "
				+ "Left-Click to splash the lava at your target. "
				+ "It can be used in rapid succession to create multiple streams of lava!");
		config.addDefault("Abilities.Earth.LavaThrow.Cooldown", 7000);
		config.addDefault("Abilities.Earth.LavaThrow.MaxShots", 6);
		config.addDefault("Abilities.Earth.LavaThrow.Range", 20);
		config.addDefault("Abilities.Earth.LavaThrow.Damage", 1.0);
		config.addDefault("Abilities.Earth.LavaThrow.SourceGrabRange", 4);
		config.addDefault("Abilities.Earth.LavaThrow.SourceRegenDelay", 10000);
		config.addDefault("Abilities.Earth.LavaThrow.FireTicks", 80);

		config.addDefault("Abilities.Earth.MagnetShield.Enabled", true);
		config.addDefault("Abilities.Earth.MagnetShield.Description", "Repel any metal projectiles using a strong magnetic shield. "
				+ "To activate, simply hold sneak with this ability bound.");
		
		config.addDefault("Abilities.Earth.MetalFragments.Enabled", true);
		config.addDefault("Abilities.Earth.MetalFragments.Description", "MetalFragments allows you to select a source and shoot "
				+ "multiple fragments of metal out of that source "
				+ "block towards your target, injuring them on impact. "
				+ "To use, tap Sneak (Default: Shift) at a metal "
				+ "source block and it will float up. Then, turn around "
				+ "and click at your target to fling metal fragments at them.");
		config.addDefault("Abilities.Earth.MetalFragments.Cooldown", 5000);
		config.addDefault("Abilities.Earth.MetalFragments.MaxSources", 3);
		config.addDefault("Abilities.Earth.MetalFragments.SourceRange", 5);
		config.addDefault("Abilities.Earth.MetalFragments.MaxFragments", 10);
		config.addDefault("Abilities.Earth.MetalFragments.Damage", 4.0);
		
		config.addDefault("Abilities.Earth.MetalHook.Enabled", true);
		config.addDefault("Abilities.Earth.MetalHook.Description", "This ability lets a Metalbender bend metal into "
				+ "grappling hooks, allowing them to easily manouver terrain. "
				+ "To use this ability, the user must either have Iron in their inventory "
				+ "or be wearing an Iron/Chainmail Chestplate. Left-Click in the direction "
				+ "you are looking to fire a grappling hook, several hooks can be active at once, "
				+ "allowing the bender to 'hang' in locations. To disengage the hooks, hold Shift (Default: Sneak) or Sprint.");
		config.addDefault("Abilities.Earth.MetalHook.Cooldown", 3000);
		config.addDefault("Abilities.Earth.MetalHook.Range", 30);
		config.addDefault("Abilities.Earth.MetalHook.MaxHooks", 3);
		config.addDefault("Abilities.Earth.MetalHook.TotalHooks", 0);
		config.addDefault("Abilities.Earth.MetalHook.RequireItems", true);
		
		config.addDefault("Abilities.Earth.MetalShred.Enabled", true);
		config.addDefault("Abilities.Earth.MetalShred.Description", "MetalShred allows you to tear a metal surface allowing you to sneak in to the other side."
				+ "To use, you must find a flat metal surface. Then, Sneak(Default: Shift) "
				+ "at a piece of metal on that surface, and two pieces of metal "
				+ "will be pulled toward you. Finally, run alongside the surface to coil "
				+ "the metal around those two pieces. The way will be open, and the blocks "
				+ "will not reset until you either select a new source or you switch "
				+ "abilities. If you click after having torn a hole in a vertical surface, "
				+ "you can Left-Click in any direction and the metal will unfold in that "
				+ "direction. If you are fast and precise enough, the metal can bend in "
				+ "any shape. The length of this sheet of metal depends on how much was "
				+ "coiled in the first place.");
		config.addDefault("Abilities.Earth.MetalShred.SourceRange", 5);
        config.addDefault("Abilities.Earth.MetalShred.ExtendTick", 80);
        config.addDefault("Abilities.Earth.MetalShred.Damage", 6.0);
        
        config.addDefault("Abilities.Earth.MudSurge.Enabled", true);
		config.addDefault("Abilities.Earth.MudSurge.Description", "This ability lets an earthbender send a surge of mud "
				+ "in any direction, knocking back enemies and "
				+ "dealing moderate damage. This ability has a chance "
				+ "of blinding the target. To use, select "
				+ "a source of earth and click in any direction.");
		config.addDefault("Abilities.Earth.MudSurge.Cooldown", 6000);
		config.addDefault("Abilities.Earth.MudSurge.Damage", 1.0);
		config.addDefault("Abilities.Earth.MudSurge.Waves", 5);
		config.addDefault("Abilities.Earth.MudSurge.SourceRange", 7);
		config.addDefault("Abilities.Earth.MudSurge.BlindChance", 10);
		config.addDefault("Abilities.Earth.MudSurge.WetSourceOnly", false);
		config.addDefault("Abilities.Earth.MudSurge.WaterSearchRadius", 5);
		config.addDefault("Abilities.Earth.MudSurge.BlindTicks", 60);
		config.addDefault("Abilities.Earth.MudSurge.CollisionRadius", 2.0);
		config.addDefault("Abilities.Earth.MudSurge.MultipleHits", true);
		config.addDefault("Abilities.Earth.MudSurge.AllowFallDamage", false);
		config.addDefault("Abilities.Earth.MudSurge.RemovalPolicy.SwappedSlots.Enabled", true);
		config.addDefault("Abilities.Earth.MudSurge.RemovalPolicy.OutOfRange.Enabled", true);
		config.addDefault("Abilities.Earth.MudSurge.RemovalPolicy.OutOfRange.Range", 25.0);
		
		config.addDefault("Abilities.Earth.SandBlast.Enabled", true);
		config.addDefault("Abilities.Earth.SandBlast.Description", "This ability lets an earthbender blast a bunch of sand at an enemy "
				+ "damaging them and temporarily blinding them! Just Sneak (Default: Shift) "
				+ "on a sand bendable block, then Left-Click in a direction to shoot a "
				+ "blast of sand!");
		config.addDefault("Abilities.Earth.SandBlast.Cooldown", 3000);
		config.addDefault("Abilities.Earth.SandBlast.Damage", 3.0);
		config.addDefault("Abilities.Earth.SandBlast.SourceRange", 8);
		config.addDefault("Abilities.Earth.SandBlast.Range", 30);
		config.addDefault("Abilities.Earth.SandBlast.MaxSandBlocks", 10);
		
		config.addDefault("Abilities.Earth.EarthCombo.Crevice.Enabled", true);
		config.addDefault("Abilities.Earth.EarthCombo.Crevice.Description", "Create a Crevice in the ground! Once opened, "
				+ "anyone can Tap Sneak with Shockwave to close the Crevice!");
		config.addDefault("Abilities.Earth.EarthCombo.Crevice.Range", 50);
		config.addDefault("Abilities.Earth.EarthCombo.Crevice.RevertDelay", 7500);
		config.addDefault("Abilities.Earth.EarthCombo.Crevice.Depth", 5);
		config.addDefault("Abilities.Earth.EarthCombo.Crevice.AvatarStateDepth", 8);
		config.addDefault("Abilities.Earth.EarthCombo.Crevice.Cooldown", 10000);
		
		config.addDefault("Abilities.Earth.EarthCombo.MagmaBlast.Enabled", true);
		config.addDefault("Abilities.Earth.EarthCombo.MagmaBlast.Description", "Fire balls of magma at your enemy!");
		config.addDefault("Abilities.Earth.EarthCombo.MagmaBlast.MaxShots", 3);
		config.addDefault("Abilities.Earth.EarthCombo.MagmaBlast.ImpactDamage", 2.0);
		config.addDefault("Abilities.Earth.EarthCombo.MagmaBlast.SearchRange", 4);
		config.addDefault("Abilities.Earth.EarthCombo.MagmaBlast.Cooldown", 6000);
		config.addDefault("Abilities.Earth.EarthCombo.MagmaBlast.ShotCooldown", 1500);
		config.addDefault("Abilities.Earth.EarthCombo.MagmaBlast.RequireLavaFlow", false);
		config.addDefault("Abilities.Earth.EarthCombo.MagmaBlast.PlayerCollisions", true);
		config.addDefault("Abilities.Earth.EarthCombo.MagmaBlast.EntitySelection", true);
		config.addDefault("Abilities.Earth.EarthCombo.MagmaBlast.SelectRange", 30.0);
		config.addDefault("Abilities.Earth.EarthCombo.MagmaBlast.ExplosionRadius", 2.0);
		config.addDefault("Abilities.Earth.EarthCombo.MagmaBlast.FireSpeed", 1.5);
		config.addDefault("Abilities.Earth.EarthCombo.MagmaBlast.MaxDuration", 15000);
		config.addDefault("Abilities.Earth.EarthCombo.MagmaBlast.MaxDistanceFromSources", 15);
		
		config.addDefault("Abilities.Fire.Combustion.Enabled", true);
		config.addDefault("Abilities.Fire.Combustion.Description", "Hold Shift to focus large amounts of energy into your body, "
				+ "Release Shift to fire Combustion. Move your mouse to "
				+ "direct where the beam travels. Left-Click to detonate "
				+ "the beam manually");
		config.addDefault("Abilities.Fire.Combustion.Damage", 4.0);
		config.addDefault("Abilities.Fire.Combustion.FireTick", 100);
		config.addDefault("Abilities.Fire.Combustion.MisfireModifier", -1);
		config.addDefault("Abilities.Fire.Combustion.Power", 3);
		config.addDefault("Abilities.Fire.Combustion.Range", 100);
		config.addDefault("Abilities.Fire.Combustion.Warmup", 1500);
		config.addDefault("Abilities.Fire.Combustion.Cooldown", 5000);
		config.addDefault("Abilities.Fire.Combustion.RegenTime", 10000);
		config.addDefault("Abilities.Fire.Combustion.EntityCollisionRadius", 1.3);
		config.addDefault("Abilities.Fire.Combustion.AbilityCollisionRadius", 1.3);
		config.addDefault("Abilities.Fire.Combustion.DamageBlocks", true);
		config.addDefault("Abilities.Fire.Combustion.RegenBlocks", true);
		config.addDefault("Abilities.Fire.Combustion.WaitForRegen", true);
		config.addDefault("Abilities.Fire.Combustion.InstantExplodeIfHit", true);
		config.addDefault("Abilities.Fire.Combustion.ExplodeOnDeath", true);
		config.addDefault("Abilities.Fire.Combustion.RemovalPolicy.SwappedSlots.Enabled", false);
		
		config.addDefault("Abilities.Fire.Discharge.Enabled", true);
		config.addDefault("Abilities.Fire.Discharge.Description", "Left-Click to shoot bolts of electricity out "
				+ "of your fingertips zapping what ever it hits!");
		config.addDefault("Abilities.Fire.Discharge.Damage", 3.0);
		config.addDefault("Abilities.Fire.Discharge.Cooldown", 5000);
		config.addDefault("Abilities.Fire.Discharge.Duration", 1000);
		config.addDefault("Abilities.Fire.Discharge.SlotSwapping", false);
		config.addDefault("Abilities.Fire.Discharge.EntityCollisionRadius", 1.0);
		config.addDefault("Abilities.Fire.Discharge.AbilityCollisionRadius", 1.0);
		
		config.addDefault("Abilities.Fire.FireBall.Enabled", true);
		config.addDefault("Abilities.Fire.FireBall.Description", "To use, simply Left-Click to shoot a fireball at your target!");
		config.addDefault("Abilities.Fire.FireBall.Cooldown", 3000);
		config.addDefault("Abilities.Fire.FireBall.Range", 50);
		config.addDefault("Abilities.Fire.FireBall.Damage", 3.0);
		config.addDefault("Abilities.Fire.FireBall.FireDuration", 2000);
		config.addDefault("Abilities.Fire.FireBall.Controllable", false);
		config.addDefault("Abilities.Fire.FireBall.FireTrail", true);
		config.addDefault("Abilities.Fire.FireBall.CollisionRadius", 1.1);
		config.addDefault("Abilities.Fire.FireBall.Collisions.FireShield.Enabled", true);
		config.addDefault("Abilities.Fire.FireBall.Collisions.FireShield.RemoveFirst", true);
		config.addDefault("Abilities.Fire.FireBall.Collisions.FireShield.RemoveSecond", false);
		config.addDefault("Abilities.Fire.FireBall.Collisions.AirShield.Enabled", true);
		config.addDefault("Abilities.Fire.FireBall.Collisions.AirShield.RemoveFirst", false);
		config.addDefault("Abilities.Fire.FireBall.Collisions.AirShield.RemoveSecond", false);
		config.addDefault("Abilities.Fire.FireBall.Collisions.AirShield.Reflect", true);
		
		config.addDefault("Abilities.Fire.FireBreath.Enabled", true);
		config.addDefault("Abilities.Fire.FireBreath.Description", "To use, hold Sneak (Default: Shift) to start breathing "
				+ "fire! Some Firebenders possess the power to infuse color "
				+ "when they breathe, it's unclear how they do this, but some suggest "
				+ "it can be obtained by saying \"Bring fire and light together as one and allow the breath of color\" "
				+ "and can be brought back to normal by saying \"Split the bond of fire "
				+ "and light and set the color free\".");
		config.addDefault("Abilities.Fire.FireBreath.Cooldown", 5000);
		config.addDefault("Abilities.Fire.FireBreath.Duration", 3000);
		config.addDefault("Abilities.Fire.FireBreath.Particles", 3);
		config.addDefault("Abilities.Fire.FireBreath.Damage.Player", 1.0);
		config.addDefault("Abilities.Fire.FireBreath.Damage.Mob", 2.0);
		config.addDefault("Abilities.Fire.FireBreath.FireDuration", 3000);
		config.addDefault("Abilities.Fire.FireBreath.Range", 10);
		config.addDefault("Abilities.Fire.FireBreath.Avatar.FireEnabled", true);
		config.addDefault("Abilities.Fire.FireBreath.Melt.Enabled", true);
		config.addDefault("Abilities.Fire.FireBreath.Melt.Chance", 3);
		config.addDefault("Abilities.Fire.FireBreath.RainbowBreath.Enabled", true);
		config.addDefault("Abilities.Fire.FireBreath.RainbowBreath.EnabledMessage", "You have bonded fire with light and can now breathe pure color.");
		config.addDefault("Abilities.Fire.FireBreath.RainbowBreath.DisabledMessage", "You have split your bond of color and light.");
		config.addDefault("Abilities.Fire.FireBreath.RainbowBreath.NoAccess", "You don't possess the power to bond light with fire.");
		
		config.addDefault("Abilities.Fire.FireComet.Enabled", true);
		config.addDefault("Abilities.Fire.FireComet.Description", "Harnessing the power of Sozin's Comet, a firebender can create a great "
				+ "ball of fire, with much destructive power. Only useable during Sozin's Comet or while in the AvatarState, hold Sneak (Default: Shift) "
				+ "to start charging the ability up. Once the ability is charged, a large mass of particles will follow your cursor, until you release sneak, "
				+ "launching the great ball of fire in the direction you are looking.");
		config.addDefault("Abilities.Fire.FireComet.Cooldown", 45000);
		config.addDefault("Abilities.Fire.FireComet.ChargeUp", 7000);
		config.addDefault("Abilities.Fire.FireComet.Damage", 6.0);
		config.addDefault("Abilities.Fire.FireComet.BlastRadius", 3.0);
		config.addDefault("Abilities.Fire.FireComet.SozinsComet.Cooldown", 30000);
		config.addDefault("Abilities.Fire.FireComet.SozinsComet.ChargeUp", 5000);
		config.addDefault("Abilities.Fire.FireComet.SozinsComet.Damage", 12.0);
		config.addDefault("Abilities.Fire.FireComet.SozinsComet.BlastRadius", 5.0);
		config.addDefault("Abilities.Fire.FireComet.Range", 50);
		config.addDefault("Abilities.Fire.FireComet.RegenDelay", 15000);
		config.addDefault("Abilities.Fire.FireComet.SozinsCometOnly", true);
		config.addDefault("Abilities.Fire.FireComet.AvatarStateBypassComet", true);
		
		config.addDefault("Abilities.Fire.FirePunch.Enabled", true);
		config.addDefault("Abilities.Fire.FirePunch.Description", "This basic ability allows a Firebender to channel their energies into a "
				+ "single punch, igniting and damaging the victim.");
		config.addDefault("Abilities.Fire.FirePunch.Cooldown", 4000);
		config.addDefault("Abilities.Fire.FirePunch.FireTicks", 2000);
		config.addDefault("Abilities.Fire.FirePunch.Damage", 2.0);
		
		config.addDefault("Abilities.Fire.FireShots.Enabled", true);
		config.addDefault("Abilities.Fire.FireShots.Description", "To use, tap Sneak (Default: Shift) to summon a "
				+ "FireBalls at your hand, then Left Click to shoot off each ball! "
				+ "Each shot will follow the cursor until it runs out or hits something!");
		config.addDefault("Abilities.Fire.FireShots.Cooldown", 3000);
		config.addDefault("Abilities.Fire.FireShots.Range", 50);
		config.addDefault("Abilities.Fire.FireShots.FireBalls", 4);
		config.addDefault("Abilities.Fire.FireShots.FireDuration", 3000);
		config.addDefault("Abilities.Fire.FireShots.Damage", 2.0);
		config.addDefault("Abilities.Fire.FireShots.CollisionRadius", 0.9);
		config.addDefault("Abilities.Fire.FireShots.Collisions.FireShield.Enabled", true);
		config.addDefault("Abilities.Fire.FireShots.Collisions.FireShield.RemoveFirst", true);
		config.addDefault("Abilities.Fire.FireShots.Collisions.FireShield.RemoveSecond", false);
		config.addDefault("Abilities.Fire.FireShots.Collisions.AirShield.Enabled", true);
		config.addDefault("Abilities.Fire.FireShots.Collisions.AirShield.RemoveFirst", false);
		config.addDefault("Abilities.Fire.FireShots.Collisions.AirShield.RemoveSecond", false);
		config.addDefault("Abilities.Fire.FireShots.Collisions.AirShield.Reflect", true);
		
		config.addDefault("Abilities.Fire.FireSki.Enabled", true);
		config.addDefault("Abilities.Fire.FireSki.Cooldown", 6000);
		config.addDefault("Abilities.Fire.FireSki.Duration", 6000);
		config.addDefault("Abilities.Fire.FireSki.Speed", 0.7);
		config.addDefault("Abilities.Fire.FireSki.IgniteEntities", true);
		config.addDefault("Abilities.Fire.FireSki.FireTicks", 60);
		config.addDefault("Abilities.Fire.FireSki.RequiredHeight", 0.7);
		config.addDefault("Abilities.Fire.FireSki.PunchActivated", false);
		
		config.addDefault("Abilities.Fire.LightningBurst.Enabled", true);
		config.addDefault("Abilities.Fire.LightningBurst.Description", "To use the most explosive lightning move available to a firebender, hold "
				+ "Sneak (Default: Shift) until blue sparks appear in front of you. Upon releasing, "
				+ "you will unleash an electrical sphere, shocking anyone who gets too close");
		config.addDefault("Abilities.Fire.LightningBurst.Cooldown", 25000);
		config.addDefault("Abilities.Fire.LightningBurst.ChargeUp", 4000);
		config.addDefault("Abilities.Fire.LightningBurst.Radius", 12);
		config.addDefault("Abilities.Fire.LightningBurst.Damage", 9.0);
		
		config.addDefault("Abilities.Water.Bloodbending.Enabled", true);
		config.addDefault("Abilities.Water.Bloodbending.Description", "This ability allows a skilled waterbender "
				+ "to bend the water within an enemy's blood, granting them full "
				+ "control over the enemy's limbs. This ability is extremely dangerous "
				+ "and is to be used carefully. To use, sneak while looking at an entity "
				+ "and its body will follow your movement. If you click, you will launch "
				+ "the entity towards whatever you were looking at when you clicked. The "
				+ "entity may collide with others, injuring them and the other one further.");
		config.addDefault("Abilities.Water.Bloodbending.NightOnly", false);
		config.addDefault("Abilities.Water.Bloodbending.FullMoonOnly", false);
		config.addDefault("Abilities.Water.Bloodbending.UndeadMobs", true);
		config.addDefault("Abilities.Water.Bloodbending.IgnoreWalls", false);
		config.addDefault("Abilities.Water.Bloodbending.RequireBound", false);
		config.addDefault("Abilities.Water.Bloodbending.Distance", 6);
		config.addDefault("Abilities.Water.Bloodbending.HoldTime", 10000);
		config.addDefault("Abilities.Water.Bloodbending.Cooldown", 4000);
		
		config.addDefault("Abilities.Water.BloodPuppet.Enabled", true);
		config.addDefault("Abilities.Water.BloodPuppet.Description", "This very high-level bloodbending ability lets "
				+ "a master control entities' limbs, forcing them to "
				+ "attack the master's target. To use this ability, you must "
				+ "be a bloodbender. Next, sneak while targeting "
				+ "a mob or player and you will start controlling them. To "
				+ "make the entity hit another, click. To release your "
				+ "target, stop sneaking. This ability has NO cooldown, but "
				+ "may only be usable during the night depending on the "
				+ "server configuration.");
		config.addDefault("Abilities.Water.BloodPuppet.NightOnly", false);
		config.addDefault("Abilities.Water.BloodPuppet.FullMoonOnly", false);
		config.addDefault("Abilities.Water.BloodPuppet.UndeadMobs", true);
		config.addDefault("Abilities.Water.BloodPuppet.IgnoreWalls", false);
		config.addDefault("Abilities.Water.BloodPuppet.RequireBound", false);
		config.addDefault("Abilities.Water.BloodPuppet.Distance", 6);
		config.addDefault("Abilities.Water.BloodPuppet.HoldTime", 10000);
		config.addDefault("Abilities.Water.BloodPuppet.Cooldown", 4000);
		
		config.addDefault("Abilities.Water.Drain.Enabled", true);
		config.addDefault("Abilities.Water.Drain.Description", "Inspired by how Hama drained water from the fire lilies, many benders "
				+ "have practiced in the skill of draining water from plants! With this ability bound, "
				+ "Sneak (Default: Shift) near/around plant sources to drain the water out of them to fill up any "
				+ "bottles/buckets in your inventory! Alternatively, if you have nothing to fill"
				+ " and blasts are enabled in the config, you will be able to create mini blasts "
				+ "of water to shoot at your targets! Aleternatively, this ability can also be used to quickly fill up "
				+ "bottles from straight water sources or from falling rain!");
		config.addDefault("Abilities.Water.Drain.RegenDelay", 15000);
		config.addDefault("Abilities.Water.Drain.Duration", 2000);
		config.addDefault("Abilities.Water.Drain.Cooldown", 2000);
		config.addDefault("Abilities.Water.Drain.AbsorbSpeed", 0.1);
		config.addDefault("Abilities.Water.Drain.AbsorbChance", 20);
		config.addDefault("Abilities.Water.Drain.AbsorbRate", 6);
		config.addDefault("Abilities.Water.Drain.Radius", 6);
		config.addDefault("Abilities.Water.Drain.HoldRange", 2);
		config.addDefault("Abilities.Water.Drain.AllowRainSource", true);
		config.addDefault("Abilities.Water.Drain.BlastsEnabled", true);
		config.addDefault("Abilities.Water.Drain.KeepSource", false);
		config.addDefault("Abilities.Water.Drain.BlastSpeed", 1);
		config.addDefault("Abilities.Water.Drain.BlastDamage", 1.5);
		config.addDefault("Abilities.Water.Drain.BlastRange", 20);
		config.addDefault("Abilities.Water.Drain.MaxBlasts", 4);
		config.addDefault("Abilities.Water.Drain.DrainTempBlocks", true);
		
		config.addDefault("Abilities.Water.FrostBreath.Enabled", true);
		config.addDefault("Abilities.Water.FrostBreath.Description", "As demonstrated by Katara, a Waterbender is able to freeze their breath, "
				+ "causing anything it touches to be frozen! With this ability bound, simply hold "
				+ "Sneak (Default: Shift) to start breathing frost!");
		config.addDefault("Abilities.Water.FrostBreath.Cooldown", 15000);
		config.addDefault("Abilities.Water.FrostBreath.Duration", 3000);
		config.addDefault("Abilities.Water.FrostBreath.Particles", 3);
		config.addDefault("Abilities.Water.FrostBreath.FrostDuration", 5000);
		config.addDefault("Abilities.Water.FrostBreath.Range", 10);
		config.addDefault("Abilities.Water.FrostBreath.Snow", true);
		config.addDefault("Abilities.Water.FrostBreath.SnowDuration", 5000);
		config.addDefault("Abilities.Water.FrostBreath.BendableSnow", false);
		config.addDefault("Abilities.Water.FrostBreath.Damage.Enabled", false);
		config.addDefault("Abilities.Water.FrostBreath.Damage.Player", 1.0);
		config.addDefault("Abilities.Water.FrostBreath.Damage.Mob", 2.0);
		config.addDefault("Abilities.Water.FrostBreath.Slow.Enabled", true);
		config.addDefault("Abilities.Water.FrostBreath.Slow.Duration", 4000);
		config.addDefault("Abilities.Water.FrostBreath.RestrictBiomes", true);
		
		config.addDefault("Abilities.Water.HealingWaters.Enabled", true);
		config.addDefault("Abilities.Water.HealingWaters.Description", "To use this ability, the bender has to be partially submerged "
				+ "in water, OR be holding either a bottle of water or a water bucket."
				+ " This move will heal the player automatically if they have it equipped "
				+ "and are standing in water. If the player sneaks while in water and is targeting"
				+ " another entity, the bender will heal the targeted entity. The alternate "
				+ "healing method requires the bender to be holding a bottle of water or a water"
				+ " bucket. To start healing simply sneak, however if the bender is targeting "
				+ "a mob while sneaking, the bender will heal the targeted mob.");
		config.addDefault("Abilities.Water.HealingWaters.Power", 1);
		config.addDefault("Abilities.Water.HealingWaters.Range", 5);
		config.addDefault("Abilities.Water.HealingWaters.DrainChance", 5);
		
		config.addDefault("Abilities.Water.IceClaws.Enabled", true);
		config.addDefault("Abilities.Water.IceClaws.Description", "As demonstrated by Hama, a Waterbender can pull water out of thin air to create claws "
				+ "at the tips of their fingers. With IceClaws bound, hold Sneak (Default: Shift) to "
				+ "start pulling water out the air until you form claws at your finger "
				+ "tips, then attack an enemy to slow them down and do a bit of damage!");
		config.addDefault("Abilities.Water.IceClaws.Cooldown", 6000);
		config.addDefault("Abilities.Water.IceClaws.ChargeTime", 1000);
		config.addDefault("Abilities.Water.IceClaws.SlowDuration", 5000);
		config.addDefault("Abilities.Water.IceClaws.Damage", 3.0);
		config.addDefault("Abilities.Water.IceClaws.Range", 10);
		config.addDefault("Abilities.Water.IceClaws.Throwable", true);
		
		config.addDefault("Abilities.Water.IceWall.Enabled", true);
		config.addDefault("Abilities.Water.IceWall.Description", "IceWall allows an icebender to create a wall of ice, similar to "
				+ "raiseearth. To use, simply sneak while targeting either water, ice, or snow. "
				+ "To break the wall, you must sneak again while targeting it. Be aware that "
				+ "other icebenders can break your own shields, and if you are too close you "
				+ "can get hurt by the shards.");
		config.addDefault("Abilities.Water.IceWall.Cooldown", 4000);
		config.addDefault("Abilities.Water.IceWall.Width", 6);
		config.addDefault("Abilities.Water.IceWall.MaxHeight", 5);
		config.addDefault("Abilities.Water.IceWall.MinHeight", 3);
		config.addDefault("Abilities.Water.IceWall.MaxWallHealth", 12);
		config.addDefault("Abilities.Water.IceWall.MinWallHealth", 8);
		config.addDefault("Abilities.Water.IceWall.Range", 8);
		config.addDefault("Abilities.Water.IceWall.Damage", 4.0);
		config.addDefault("Abilities.Water.IceWall.CanBreak", true);
		config.addDefault("Abilities.Water.IceWall.Stackable", false);
		config.addDefault("Abilities.Water.IceWall.LifeTime.Enabled", false);
		config.addDefault("Abilities.Water.IceWall.LifeTime.Duration", 10000);
		config.addDefault("Abilities.Water.IceWall.WallDamage", true);
		config.addDefault("Abilities.Water.IceWall.WallDamage.Torrent", 5);
		config.addDefault("Abilities.Water.IceWall.WallDamage.TorrentFreeze", 9);
		config.addDefault("Abilities.Water.IceWall.WallDamage.IceBlast", 8);
		config.addDefault("Abilities.Water.IceWall.WallDamage.Fireblast", 3);
		config.addDefault("Abilities.Water.IceWall.WallDamage.FireblastCharged", 5);
		config.addDefault("Abilities.Water.IceWall.WallDamage.Lightning", 12);
		config.addDefault("Abilities.Water.IceWall.WallDamage.Combustion", 12);
		config.addDefault("Abilities.Water.IceWall.WallDamage.EarthSmash", 8);
		config.addDefault("Abilities.Water.IceWall.WallDamage.AirBlast", 2);
		
		config.addDefault("Abilities.Water.WakeFishing.Enabled", true);
		config.addDefault("Abilities.Water.WakeFishing.Description", "With this ability bound, hold Shift (Default: Sneak) at a water block and "
				+ "don't lose focus of that block. Eventually some fish will investigate "
				+ "the wake and swim out at you!");
		config.addDefault("Abilities.Water.WakeFishing.Cooldown", 10000);
		config.addDefault("Abilities.Water.WakeFishing.Duration", 20000);
		config.addDefault("Abilities.Water.WakeFishing.Range", 5);
		
		config.addDefault("Abilities.Water.WaterCombo.Maelstrom.Enabled", true);
		config.addDefault("Abilities.Water.WaterCombo.Maelstrom.Description", "Create a swirling mass of water that drags any entity that enters it to the bottom "
				+ "of the whirlpool.");
		config.addDefault("Abilities.Water.WaterCombo.Maelstrom.Cooldown", 25000);
		config.addDefault("Abilities.Water.WaterCombo.Maelstrom.Duration", 15000);
		config.addDefault("Abilities.Water.WaterCombo.Maelstrom.MaxDepth", 5);
		config.addDefault("Abilities.Water.WaterCombo.Maelstrom.Range", 10);
		
		config.addDefault("Abilities.Water.WaterCombo.WaterFlow.Enabled", true);
		config.addDefault("Abilities.Water.WaterCombo.WaterFlow.Description", "Some Waterbenders have managed to create torrents of water much stronger than a regular torrent, "
				+ "that can carry them selves and others, as well as being able to freeze the entire stream whenever. The bender must stay focused on the flow or else the flow will stop."
				+ " If you Sneak (Default: Shift) while controlling the stream, the stream will return to you.");
		config.addDefault("Abilities.Water.WaterCombo.WaterFlow.Cooldown", 8000);
		config.addDefault("Abilities.Water.WaterCombo.WaterFlow.Duration", 8000);
		config.addDefault("Abilities.Water.WaterCombo.WaterFlow.MeltDelay", 5000);
		config.addDefault("Abilities.Water.WaterCombo.WaterFlow.SourceRange", 10);
		config.addDefault("Abilities.Water.WaterCombo.WaterFlow.MaxRange", 40);
		config.addDefault("Abilities.Water.WaterCombo.WaterFlow.MinRange", 8);
		config.addDefault("Abilities.Water.WaterCombo.WaterFlow.Trail", 80);
		config.addDefault("Abilities.Water.WaterCombo.WaterFlow.BottleSource", false);
		config.addDefault("Abilities.Water.WaterCombo.WaterFlow.PlantSource", false);
		config.addDefault("Abilities.Water.WaterCombo.WaterFlow.RemoveOnAnyDamage", false);
		config.addDefault("Abilities.Water.WaterCombo.WaterFlow.Size.Normal", 1);
		config.addDefault("Abilities.Water.WaterCombo.WaterFlow.Size.AvatarState", 3);
		config.addDefault("Abilities.Water.WaterCombo.WaterFlow.Size.FullmoonSmall", 2);
		config.addDefault("Abilities.Water.WaterCombo.WaterFlow.Size.FullmoonLarge", 3);
		config.addDefault("Abilities.Water.WaterCombo.WaterFlow.IsAvatarStateToggle", true);
		config.addDefault("Abilities.Water.WaterCombo.WaterFlow.AvatarStateDuration", 60000);
		config.addDefault("Abilities.Water.WaterCombo.WaterFlow.PlayerStayNearSource", true);
		config.addDefault("Abilities.Water.WaterCombo.WaterFlow.MaxDistanceFromSource", 100);
		config.addDefault("Abilities.Water.WaterCombo.WaterFlow.FullMoon.Enabled", true);
		config.addDefault("Abilities.Water.WaterCombo.WaterFlow.FullMoon.Modifier.Cooldown", 3);
		config.addDefault("Abilities.Water.WaterCombo.WaterFlow.FullMoon.Modifier.Duration", 2);
		config.addDefault("Abilities.Water.WaterCombo.WaterFlow.PlayerRideOwnFlow", true);
		
		config.addDefault("Abilities.Water.WaterCombo.WaterGimbal.Enabled", true);
		config.addDefault("Abilities.Water.WaterCombo.WaterGimbal.Description", "Skilled Waterbenders are able to create two spinning rings of water around their bodies, "
				+ "which can be used as a defensive ability or for an offensive attack.");
		config.addDefault("Abilities.Water.WaterCombo.WaterGimbal.Cooldown", 7000);
		config.addDefault("Abilities.Water.WaterCombo.WaterGimbal.Damage", 3.0);
		config.addDefault("Abilities.Water.WaterCombo.WaterGimbal.RingSize", 3.5);
		config.addDefault("Abilities.Water.WaterCombo.WaterGimbal.Range", 40);
		config.addDefault("Abilities.Water.WaterCombo.WaterGimbal.SourceRange", 10);
		config.addDefault("Abilities.Water.WaterCombo.WaterGimbal.Speed", 2);
		config.addDefault("Abilities.Water.WaterCombo.WaterGimbal.AnimationSpeed", 3);
		config.addDefault("Abilities.Water.WaterCombo.WaterGimbal.PlantSource", true);
		config.addDefault("Abilities.Water.WaterCombo.WaterGimbal.RequireAdjacentPlants", true);
		config.addDefault("Abilities.Water.WaterCombo.WaterGimbal.BottleSource", false);
		config.addDefault("Abilities.Water.WaterCombo.WaterGimbal.AbilityCollisionRadius", 1.6);
		config.addDefault("Abilities.Water.WaterCombo.WaterGimbal.EntityCollisionRadius", 1.6);
		config.addDefault("Abilities.Water.WaterCombo.WaterGimbal.Collisions.FireShield.Enabled", false);
		config.addDefault("Abilities.Water.WaterCombo.WaterGimbal.Collisions.FireShield.RemoveFirst", true);
		config.addDefault("Abilities.Water.WaterCombo.WaterGimbal.Collisions.FireShield.RemoveSecond", false);
		
		config.addDefault("Abilities.Water.Ice.Passive.Skate.Enabled", true);
		config.addDefault("Abilities.Water.Ice.Passive.Skate.SpeedFactor", 4);
		
		config.addDefault("Abilities.Chi.Backstab.Enabled", true);
		config.addDefault("Abilities.Chi.Backstab.Description", "Strike your foe in the back with a hard jab, temporariliy blocking their Chi, and "
				+ "inflicting a lot of damage! This ability has a long cooldown. You must hit the target in the back or this ability won't work!");
		config.addDefault("Abilities.Chi.Backstab.Cooldown", 8500);
		config.addDefault("Abilities.Chi.Backstab.Damage", 6.0);
		config.addDefault("Abilities.Chi.Backstab.MaxActivationAngle", 90);
		
		config.addDefault("Abilities.Chi.DaggerThrow.Enabled", true);
		config.addDefault("Abilities.Chi.DaggerThrow.Description", "With this ability bound, Left-Click in "
				+ "rapid succession to shoot arrows out of your inventory at your target!");
		config.addDefault("Abilities.Chi.DaggerThrow.Cooldown", 3000);
		config.addDefault("Abilities.Chi.DaggerThrow.MaxDaggers.Enabled", true);
		config.addDefault("Abilities.Chi.DaggerThrow.MaxDaggers.Amount", 6);
		config.addDefault("Abilities.Chi.DaggerThrow.Damage", 1.0);
		config.addDefault("Abilities.Chi.DaggerThrow.ParticleTrail", true);
		config.addDefault("Abilities.Chi.DaggerThrow.AbilityCollisionRadius", 0.5);
		config.addDefault("Abilities.Chi.DaggerThrow.Interactions.WaterSpout.Enabled", true);
		config.addDefault("Abilities.Chi.DaggerThrow.Interactions.WaterSpout.Cooldown", 1000);
		config.addDefault("Abilities.Chi.DaggerThrow.Interactions.WaterSpout.HitsRequired", 1);
		config.addDefault("Abilities.Chi.DaggerThrow.Interactions.AirSpout.Enabled", true);
		config.addDefault("Abilities.Chi.DaggerThrow.Interactions.AirSpout.Cooldown", 1000);
		config.addDefault("Abilities.Chi.DaggerThrow.Interactions.AirSpout.HitsRequired", 1);

		String[] invalidWallRun = {Material.BARRIER.name()};
		config.addDefault("Abilities.Passives.WallRun.Enabled", true);
		config.addDefault("Abilities.Passives.WallRun.Cooldown", 6000);
		config.addDefault("Abilities.Passives.WallRun.Duration", 20000);
		config.addDefault("Abilities.Passives.WallRun.Particles", true);
		config.addDefault("Abilities.Passives.WallRun.Air", true);
		config.addDefault("Abilities.Passives.WallRun.Earth", false);
		config.addDefault("Abilities.Passives.WallRun.Water", false);
		config.addDefault("Abilities.Passives.WallRun.Fire", true);
		config.addDefault("Abilities.Passives.WallRun.Chi", true);
		config.addDefault("Abilities.Passives.WallRun.InvalidBlocks", invalidWallRun);
		
		config.options().copyDefaults(true);
		plugin.saveConfig();
	}

	public static ConfigurationSection getConfig(Player player) {
		if (player == null)
			return getConfig((World)null);
		return getConfig(player.getWorld());
	}

	public static ConfigurationSection getConfig(World world) {
		boolean perWorldConfig = plugin.getConfig().getBoolean("Properties.PerWorldConfig");

		if (world == null || !perWorldConfig) {
			return plugin.getConfig();
		}

		String prefix = "Worlds." + world.getName();
		return new SubsectionConfigurationDecorator(plugin.getConfig(), prefix);
	}
}
