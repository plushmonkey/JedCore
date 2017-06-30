# JedCore
This is my fork of jedk1's JedCore addon for ProjectKorra.  
Download releases [here](https://github.com/plushmonkey/JedCore/releases).  

## Features
- Works with ProjectKorra 1.8.3 and 1.8.4.
- Make LavaThrow fire ticks configurable.
- Fix Backstab NPE.
- Make FireSki fire ticks configurable.
- Improve SonicBlast.
  - Remove the ability after it does damage.
  - Add configuration option for allowing slot swapping while charging.
- Make AirGlide configurable.
  - Make cooldown configurable.
  - Make duration configurable.
- Improve MudSurge.
  - Change default damage to 1.
  - Make blind ticks configurable.
  - Add configuration option for having MudSurge hit an entity multiple times.
  - Change it so the cooldown starts on launch rather than on sourcing.
- Improve FireBall and FireShots.
  - Add configuration option for shield collisions for both FireBall and FireShots.
  - Reflect both of the abilities off of AirShield when ShieldCollisions is enabled.
  - Destroy both of the abilities in FireShield when ShieldCollisions is enabled.
  - Make FireBall blaze trail configurable.
  - Change FireBall blaze trail so it lags behind the attack.
- Revert passive sand blocks before using them. This stops abilities from turning the blocks into permanent sand.
- Make EarthSurf usable on transparent blocks.
- Improve EarthShard.
  - Prevent selecting positions that already have EarthShard TempBlock. This fixes the bug where the FallingBlock can't reach its destination, putting the instance into a broken state.
  - Fix a bug where damage wouldn't happen when swapping slots after launching.
  - Disable the destruction of the current instance when selecting an air block.
  - Read the damage as a double.
- Fix bending board combos.
  - Register the combos with the bending board after a delay on startup. This ensures all addons have registered their combos.
- Fix SandBlast.
  - Fix SandBlast so it doesn't get stuck in a broken state.
  - Fix SandBlast so it actually affects entities.
  - Change SandBlast so it reverts passive sand.
- Fix bending board ability name length.
  - Remove the code that added multiple reset codes. This could make the board ability names so long that they would kick players offline.
- Fix IceWall so old instances don't block new ones. 
- Add ability collisions.
  - Add global configuration option to disable all JedCore ability collisions. They are disabled by default.
  - Collisions are configurable under Abilities.{element}.{abilityname}.Collisions.{abilityname}.
    - Some default examples were added to AirBlade and AirPunch.
  - This hasn't been tested with every ability, so it might not work with all of them.