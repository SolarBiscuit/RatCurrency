package com.solarbiscuit.ratcurrency.config;

import com.solarbiscuit.ratcurrency.item.CoinTier;
import com.solarbiscuit.ratcurrency.util.CoinHelper;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.LinkedHashMap;
import java.util.Map;

public class RatCurrencyConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.DoubleValue DROP_CHANCE;
    public static final ForgeConfigSpec.DoubleValue VANILLA_HOSTILE_DROP_CHANCE;
    public static final ForgeConfigSpec.DoubleValue MODDED_HOSTILE_DROP_CHANCE;
    public static final ForgeConfigSpec.IntValue MIN_COPPER;
    public static final ForgeConfigSpec.IntValue MAX_COPPER;
    public static final ForgeConfigSpec.BooleanValue REQUIRE_PLAYER_KILL;
    public static final ForgeConfigSpec.IntValue RAT_COIN_MAX_STACK;
    public static final ForgeConfigSpec.BooleanValue COPPER_ENABLED;
    public static final ForgeConfigSpec.BooleanValue SILVER_ENABLED;
    public static final ForgeConfigSpec.BooleanValue GOLD_ENABLED;
    public static final ForgeConfigSpec.BooleanValue PLATINUM_ENABLED;
    public static final ForgeConfigSpec.BooleanValue SAPPHIRE_ENABLED;
    public static final ForgeConfigSpec.BooleanValue EMERALD_ENABLED;
    public static final ForgeConfigSpec.BooleanValue RUBY_ENABLED;
    public static final ForgeConfigSpec.BooleanValue DIAMOND_ENABLED;
    public static final ForgeConfigSpec.BooleanValue OBSIDIAN_ENABLED;
    public static final ForgeConfigSpec.BooleanValue NETHERITE_ENABLED;
    public static final ForgeConfigSpec.BooleanValue RAT_COIN_ENABLED;
    public static final ForgeConfigSpec.BooleanValue OPULENCE_ENABLED;
    public static final ForgeConfigSpec.BooleanValue URN_COINS_ENABLED;
    public static final CoinRange URN_COINS;
    public static final ForgeConfigSpec.BooleanValue SKELETON_COINS_ENABLED;
    public static final CoinRange SKELETON_COINS;
    public static final ForgeConfigSpec.IntValue OPULENCE_LEVEL_1;
    public static final ForgeConfigSpec.IntValue OPULENCE_LEVEL_2;
    public static final ForgeConfigSpec.IntValue OPULENCE_LEVEL_3;
    public static final ForgeConfigSpec.IntValue OPULENCE_LEVEL_4;
    public static final ForgeConfigSpec.IntValue OPULENCE_LEVEL_5;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Hostile mob coin drops").push("drops");
        DROP_CHANCE = builder
                .comment("Global chance that a qualifying hostile mob drops coins, from 0.0 to 1.0. Applied before vanilla/modded chances.")
                .defineInRange("dropChance", 1.0, 0.0, 1.0);
        VANILLA_HOSTILE_DROP_CHANCE = builder
                .comment("Chance that a vanilla (minecraft namespace) hostile mob drops coins, from 0.0 to 1.0.")
                .defineInRange("vanillaHostileDropChance", 1.0, 0.0, 1.0);
        MODDED_HOSTILE_DROP_CHANCE = builder
                .comment("Chance that a non-vanilla hostile mob drops coins, from 0.0 to 1.0.")
                .defineInRange("moddedHostileDropChance", 1.0, 0.0, 1.0);
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
        builder.comment("Enable or disable individual coin types. Disabled coins do not drop from mobs and are hidden from the creative tab; crafting and existing items still work.")
                .push("coins");
        COPPER_ENABLED = coinToggle(builder, "copper", "Copper");
        SILVER_ENABLED = coinToggle(builder, "silver", "Silver");
        GOLD_ENABLED = coinToggle(builder, "gold", "Gold");
        PLATINUM_ENABLED = coinToggle(builder, "platinum", "Platinum");
        SAPPHIRE_ENABLED = coinToggle(builder, "sapphire", "Sapphire");
        EMERALD_ENABLED = coinToggle(builder, "emerald", "Emerald");
        RUBY_ENABLED = coinToggle(builder, "ruby", "Ruby");
        DIAMOND_ENABLED = coinToggle(builder, "diamond", "Diamond");
        OBSIDIAN_ENABLED = coinToggle(builder, "obsidian", "Obsidian");
        NETHERITE_ENABLED = coinToggle(builder, "netherite", "Netherite");
        RAT_COIN_ENABLED = builder
                .comment("If false, Rat Coins are hidden from the creative tab.")
                .define("ratCoin", true);
        builder.pop();
        builder.comment("Enchantments").push("enchantments");
        OPULENCE_ENABLED = builder
                .comment("If true, the Opulence enchantment can appear in the world and increase coin drops from mobs, Supplementaries treasure urns and RealmRPG skeletons.")
                .define("opulenceEnabled", true);
        OPULENCE_LEVEL_1 = opulenceLevel(builder, "opulenceLevel1", 1, 10);
        OPULENCE_LEVEL_2 = opulenceLevel(builder, "opulenceLevel2", 2, 20);
        OPULENCE_LEVEL_3 = opulenceLevel(builder, "opulenceLevel3", 3, 30);
        OPULENCE_LEVEL_4 = opulenceLevel(builder, "opulenceLevel4", 4, 40);
        OPULENCE_LEVEL_5 = opulenceLevel(builder, "opulenceLevel5", 5, 50);
        builder.pop();
        builder.comment("Supplementaries compatibility (ignored when Supplementaries is not installed)").push("supplementaries");
        URN_COINS_ENABLED = builder
                .comment("If true, breaking a treasure urn (the kind that generates in caves) also drops coins. Opulence on the breaking tool adds its bonus.")
                .define("urnCoinsEnabled", true);
        URN_COINS = coinRange(builder, "urn", "a treasure urn");
        builder.pop();
        builder.comment("RealmRPG: Fallen Adventurers compatibility (ignored when it is not installed)").push("realmrpg");
        SKELETON_COINS_ENABLED = builder
                .comment("If true, breaking an adventurer skeleton without Silk Touch also drops coins. Opulence on the breaking tool adds its bonus.")
                .define("skeletonCoinsEnabled", true);
        SKELETON_COINS = coinRange(builder, "skeleton", "an adventurer skeleton");
        builder.pop();
        builder.comment("Uncraftable Rat Coin").push("ratCoin");
        RAT_COIN_MAX_STACK = builder
                .comment("Maximum stack size for Rat Coins (1-64).")
                .defineInRange("maxStack", 8, 1, 64);
        builder.pop();
        SPEC = builder.build();
    }

    /**
     * Values received from a remote server, keyed by config path. While connected, clients use these instead of their
     * own file so stack sizes, slot limits and enchanting checks match the server. Null on servers and in singleplayer.
     */
    private static volatile Map<String, Object> serverValues;

    /** Reads a config value, preferring the server's value while connected to one. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> T get(ForgeConfigSpec.ConfigValue<T> value) {
        Map<String, Object> synced = serverValues;
        if (synced != null) {
            Object override = synced.get(String.join(".", value.getPath()));
            Object fallback = value.getDefault();
            if (fallback instanceof Enum<?> constant && override instanceof String name) {
                // Enums travel as their names.
                try {
                    return (T) Enum.valueOf((Class) constant.getDeclaringClass(), name);
                } catch (IllegalArgumentException ignored) {
                    // Unknown on this client; fall back to the local value.
                }
            } else if (override != null && override.getClass() == fallback.getClass()) {
                return (T) override;
            }
        }
        return value.get();
    }

    /** Every value in the spec with its current local value, for sending to clients. */
    public static Map<String, Object> snapshot() {
        Map<String, Object> values = new LinkedHashMap<>();
        collect(SPEC.getValues(), values);
        return values;
    }

    private static void collect(UnmodifiableConfig config, Map<String, Object> out) {
        for (Object entry : config.valueMap().values()) {
            if (entry instanceof ForgeConfigSpec.ConfigValue<?> value) {
                Object current = value.get();
                out.put(String.join(".", value.getPath()), current instanceof Enum<?> e ? e.name() : current);
            } else if (entry instanceof UnmodifiableConfig child) {
                collect(child, out);
            }
        }
    }

    public static void applyServerValues(Map<String, Object> values) {
        serverValues = Map.copyOf(values);
    }

    public static void clearServerValues() {
        serverValues = null;
    }

    /**
     * A random coin drop between two amounts, each given as a count of one coin tier, e.g. 23 gold to 2 ruby.
     * Values are in copper units (1 silver = 100 copper, 1 gold = 10,000, ...).
     */
    public record CoinRange(ForgeConfigSpec.IntValue minCount, ForgeConfigSpec.EnumValue<CoinTier> minTier,
                            ForgeConfigSpec.IntValue maxCount, ForgeConfigSpec.EnumValue<CoinTier> maxTier) {
        public long minCopper() {
            return copperValue(get(this.minCount), get(this.minTier));
        }

        public long maxCopper() {
            return copperValue(get(this.maxCount), get(this.maxTier));
        }
    }

    /** {@code count} coins of {@code tier} in copper units, saturating instead of overflowing. */
    public static long copperValue(int count, CoinTier tier) {
        long value = count;
        for (int i = 0; i < tier.ordinal(); i++) {
            if (value > Long.MAX_VALUE / CoinHelper.COINS_PER_TIER) {
                return Long.MAX_VALUE;
            }
            value *= CoinHelper.COINS_PER_TIER;
        }
        return value;
    }

    private static CoinRange coinRange(ForgeConfigSpec.Builder builder, String prefix, String source) {
        return new CoinRange(
                builder.comment("Minimum coins dropped by " + source + ": this many of minTier (1-100). Default: 1 copper.")
                        .defineInRange(prefix + "MinCount", 1, 1, 100),
                builder.comment("Coin tier of the minimum: COPPER, SILVER, GOLD, PLATINUM, SAPPHIRE, EMERALD, RUBY, DIAMOND, OBSIDIAN or NETHERITE.")
                        .defineEnum(prefix + "MinTier", CoinTier.COPPER),
                builder.comment("Maximum coins dropped by " + source + ": this many of maxTier (1-100). Default: 20 silver.",
                                "Example: min 23 GOLD, max 2 RUBY. If the minimum is larger, the two are swapped.")
                        .defineInRange(prefix + "MaxCount", 20, 1, 100),
                builder.comment("Coin tier of the maximum.")
                        .defineEnum(prefix + "MaxTier", CoinTier.SILVER));
    }

    public static boolean isSkeletonCoinsEnabled() {
        return !SPEC.isLoaded() || get(SKELETON_COINS_ENABLED);
    }

    public static boolean isUrnCoinsEnabled() {
        return !SPEC.isLoaded() || get(URN_COINS_ENABLED);
    }

    public static boolean isOpulenceEnabled() {
        return !SPEC.isLoaded() || get(OPULENCE_ENABLED);
    }

    public static int getOpulenceBonusPercent(int level) {
        int clamped = Math.max(1, Math.min(5, level));
        if (!SPEC.isLoaded()) {
            return clamped * 10;
        }
        return switch (clamped) {
            case 1 -> get(OPULENCE_LEVEL_1);
            case 2 -> get(OPULENCE_LEVEL_2);
            case 3 -> get(OPULENCE_LEVEL_3);
            case 4 -> get(OPULENCE_LEVEL_4);
            default -> get(OPULENCE_LEVEL_5);
        };
    }

    public static boolean isRatCoinEnabled() {
        return !SPEC.isLoaded() || get(RAT_COIN_ENABLED);
    }

    public static boolean isCoinEnabled(CoinTier tier) {
        if (!SPEC.isLoaded() || tier == null) {
            return true;
        }
        return switch (tier) {
            case COPPER -> get(COPPER_ENABLED);
            case SILVER -> get(SILVER_ENABLED);
            case GOLD -> get(GOLD_ENABLED);
            case PLATINUM -> get(PLATINUM_ENABLED);
            case SAPPHIRE -> get(SAPPHIRE_ENABLED);
            case EMERALD -> get(EMERALD_ENABLED);
            case RUBY -> get(RUBY_ENABLED);
            case DIAMOND -> get(DIAMOND_ENABLED);
            case OBSIDIAN -> get(OBSIDIAN_ENABLED);
            case NETHERITE -> get(NETHERITE_ENABLED);
        };
    }

    private static ForgeConfigSpec.BooleanValue coinToggle(ForgeConfigSpec.Builder builder, String key, String displayName) {
        return builder
                .comment("If false, " + displayName + " coins do not drop from mobs and are hidden from the creative tab.")
                .define(key, true);
    }

    private static ForgeConfigSpec.IntValue opulenceLevel(ForgeConfigSpec.Builder builder, String key, int level, int defaultPercent) {
        return builder
                .comment("Bonus percent added to coin drops for Opulence " + level + " (0-500).")
                .defineInRange(key, defaultPercent, 0, 500);
    }
}
