package com.jedk1.jedcore.util;

import com.jedk1.jedcore.JedCore;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.Collision;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

public class CollisionInitializer<T extends CoreAbility> {
    // This is used for special mappings where collisions are stored in a separate place.
    public static Map<String, String> abilityMap = new HashMap<>();
    private Class<T> type;

    public CollisionInitializer(Class<T> type) {
        this.type = type;
    }

    public boolean initialize() {
        CoreAbility ability = CoreAbility.getAbility(type);

        if (ability == null) return false;

        String abilityName = ability.getName();

        Element element = ability.getElement();
        if (element instanceof Element.SubElement) {
            element = ((Element.SubElement) element).getParentElement();
            if (element == null) {
                element = ability.getElement();
            }
        }

        if (abilityMap.containsKey(abilityName)) {
            abilityName = abilityMap.get(abilityName);
        }

        String collisionPath = getCollisionPath(abilityName, element);

        ConfigurationSection section = JedCore.plugin.getConfig().getConfigurationSection(collisionPath);
        for (String key : section.getKeys(false)) {
            ConfigurationSection abilityConfig = section.getConfigurationSection(key);

            boolean enabled = abilityConfig.getBoolean("Enabled");
            if (!enabled) continue;

            if (key.equalsIgnoreCase("-small-")) {
                JedCore.logDebug("Initializing small collision for " + abilityName + ".");
                ProjectKorra.getCollisionInitializer().addSmallAbility(ability);

                continue;
            } else if (key.equalsIgnoreCase("-large-")) {
                JedCore.logDebug("Initializing large collision for " + abilityName + ".");
                ProjectKorra.getCollisionInitializer().addLargeAbility(ability);

                continue;
            }

            boolean removeFirst = abilityConfig.getBoolean("RemoveFirst");
            boolean removeSecond = abilityConfig.getBoolean("RemoveSecond");

            CoreAbility secondAbility = AbilitySelector.getAbility(key);

            if (secondAbility != null) {
                JedCore.logDebug("Initializing collision for " + abilityName + " => " + key);

                ProjectKorra.getCollisionManager().addCollision(new Collision(ability, secondAbility, removeFirst, removeSecond));
            }
        }

        return true;
    }

    private String getCollisionPath(String abilityName, Element element) {
        CoreAbility ability = CoreAbility.getAbility(abilityName);
        if (ability == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        sb.append("Abilities.").append(element.getName()).append(".");

        if (ability instanceof ComboAbility) {
            sb.append(element.getName()).append("Combo.");
        }

        sb.append(abilityName).append(".Collisions");

        return sb.toString();
    }
}
