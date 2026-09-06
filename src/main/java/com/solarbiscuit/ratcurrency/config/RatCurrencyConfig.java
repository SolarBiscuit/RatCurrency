package com.solarbiscuit.ratcurrency.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class RatCurrencyConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.DoubleValue DROP_CHANCE;
    public static final ForgeConfigSpec.IntValue MIN_COPPER;
    public static final ForgeConfigSpec.IntValue MAX_COPPER;
    public static final ForgeConfigSpec.BooleanValue REQUIRE_PLAYER_KILL;
    public static final ForgeConfigSpec.IntValue RAT_COIN_MAX_STACK;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Hostile mob coin drops").push("drops");
        DROP_CHANCE = builder
                .comment("Chance that a qualifying hostile mob drops coins, from 0.0 to 1.0.")
                .defineInRange("dropChance", 1.0, 0.0, 1.0);
        MIN_COPPER = builder
                .comment("Minimum copper-unit value dropped (1 copper = 1).")
                .defineInRange("minCopper", 1, 1, 4096);
        MAX_COPPER = builder
                .comment("Maximum copper-unit value dropped (60 copper; 100 = 1 silver).")
                .defineInRange("maxCopper", 60, 1, 4096);
        REQUIRE_PLAYER_KILL = builder
                .comment("If true, coins only drop when a player (or their wolf, etc.) is credited for the kill.")
                .define("requirePlayerKill", true);
        builder.pop();
        builder.comment("Uncraftable Rat Coin").push("ratCoin");
        RAT_COIN_MAX_STACK = builder
                .comment("Maximum stack size for Rat Coins (1-64).")
                .defineInRange("maxStack", 8, 1, 64);
        builder.pop();
        SPEC = builder.build();
    }
}
