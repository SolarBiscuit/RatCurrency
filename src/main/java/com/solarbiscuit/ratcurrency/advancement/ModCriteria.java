package com.solarbiscuit.ratcurrency.advancement;

import net.minecraft.advancements.CriteriaTriggers;

public final class ModCriteria {
    public static CoinCountTrigger COIN_COUNT;

    private ModCriteria() {
    }

    public static void register() {
        COIN_COUNT = CriteriaTriggers.register(new CoinCountTrigger());
    }
}
