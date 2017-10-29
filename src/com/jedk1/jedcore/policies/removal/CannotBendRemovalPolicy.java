package com.jedk1.jedcore.policies.removal;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;

public class CannotBendRemovalPolicy implements RemovalPolicy {
    private BendingPlayer bPlayer;
    private boolean ignoreBinds;
    private boolean ignoreCooldowns;
    private CoreAbility ability;

    public CannotBendRemovalPolicy(BendingPlayer bPlayer, CoreAbility ability) {
        this(bPlayer, ability, false, false);
    }

    public CannotBendRemovalPolicy(BendingPlayer bPlayer, CoreAbility ability, boolean ignoreBinds, boolean ignoreCooldowns) {
        this.bPlayer = bPlayer;
        this.ability = ability;
        this.ignoreBinds = ignoreBinds;
        this.ignoreCooldowns = ignoreCooldowns;
    }

    @Override
    public boolean shouldRemove() {
        if (this.ignoreBinds && this.ignoreCooldowns) {
            return !this.bPlayer.canBendIgnoreBindsCooldowns(ability);
        }

        if (this.ignoreBinds) {
            return !this.bPlayer.canBendIgnoreBinds(ability);
        }

        if (this.ignoreCooldowns) {
            return !this.bPlayer.canBendIgnoreCooldowns(ability);
        }

        return !this.bPlayer.canBend(ability);
    }

    @Override
    public String getName() {
        return "CannotBend";
    }
}
