package com.jedk1.jedcore.util;

import com.jedk1.jedcore.JedCore;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.firebending.FireBlast;
import com.projectkorra.projectkorra.firebending.FireBlastCharged;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class CollisionInitializer<T extends CoreAbility> {
    private static Map<String, CoreAbility> specialAbilities = new HashMap<>();
    private Class<T> type;

    static {
        specialAbilities.put("FireBlast", CoreAbility.getAbility(FireBlast.class));
        specialAbilities.put("FireBlastCharged", CoreAbility.getAbility(FireBlastCharged.class));
    }

    public CollisionInitializer(Class<T> type) {
        this.type = type;
    }

    public boolean initialize() {
        CoreAbility ability = CoreAbility.getAbility(type);

        if (ability == null) return false;

        String abilityName = ability.getName();
        String elementName = ability.getElement().getName();
        String collisionPath = "Abilities." + elementName + "." + abilityName + ".Collisions";

        ConfigurationSection section = JedCore.plugin.getConfig().getConfigurationSection(collisionPath);
        for (String key : section.getKeys(false)) {
            ConfigurationSection abilityConfig = section.getConfigurationSection(key);

            boolean enabled = abilityConfig.getBoolean("Enabled", true);
            if (!enabled) continue;

            boolean removeFirst = abilityConfig.getBoolean("RemoveFirst");
            boolean removeSecond = abilityConfig.getBoolean("RemoveSecond");

            CoreAbility secondAbility = getAbility(key);

            if (secondAbility != null) {
                JedCore.plugin.getLogger().info("Initializing collision for " + abilityName + " => " + key);

                ProjectKorra.getCollisionManager().addCollision(new Collision(ability, secondAbility, removeFirst, removeSecond));
            }
        }

        return true;
    }

    private CoreAbility getAbility(String name) {
        CoreAbility ability = specialAbilities.get(name);

        if (ability != null)
            return ability;

        return CoreAbility.getAbility(name);
    }
}
