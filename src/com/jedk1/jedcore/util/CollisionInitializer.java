package com.jedk1.jedcore.util;

import com.jedk1.jedcore.JedCore;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.Collision;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class CollisionInitializer<T extends CoreAbility> {
    private Class<T> type;

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

            for (CoreAbility secondAbility : getAbility(key)) {
                System.out.println("Initializing collision for " + abilityName + " => " + secondAbility.getName());

                ProjectKorra.getCollisionManager().addCollision(new Collision(ability, secondAbility, removeFirst, removeSecond));
            }
        }

        return true;
    }

    // Helper function to get list of abilities by name. Some abilities, like FireBlast, have multiple classes with the same ability name.
    private List<CoreAbility> getAbility(String name) {
       return CoreAbility.getAbilities().stream().filter(abil -> abil.getName().equalsIgnoreCase(name)).collect(toList());
    }
}
