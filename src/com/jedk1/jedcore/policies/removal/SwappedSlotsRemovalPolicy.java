package com.jedk1.jedcore.policies.removal;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;

public class SwappedSlotsRemovalPolicy<T extends CoreAbility> implements RemovalPolicy {
    private BendingPlayer bPlayer;
    private Class<? extends T> type;

    public SwappedSlotsRemovalPolicy(BendingPlayer bPlayer, Class<? extends T> type) {
        this.bPlayer = bPlayer;
        this.type = type;
    }

    @Override
    public boolean shouldRemove() {
        CoreAbility bound = this.bPlayer.getBoundAbility();

        return bound == null || !bound.getClass().isAssignableFrom(type);
    }

    @Override
    public String getName() {
        return "SwappedSlots";
    }
}
