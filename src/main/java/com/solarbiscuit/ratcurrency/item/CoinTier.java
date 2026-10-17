package com.solarbiscuit.ratcurrency.item;

import com.solarbiscuit.ratcurrency.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public enum CoinTier {
    COPPER,
    SILVER,
    GOLD,
    PLATINUM,
    SAPPHIRE,
    EMERALD,
    RUBY,
    DIAMOND,
    OBSIDIAN,
    NETHERITE;

    private static final Map<String, CoinTier> BY_PATH;
    private static final List<String> PATHS;

    static {
        Map<String, CoinTier> byPath = new HashMap<>();
        List<String> paths = new ArrayList<>();
        for (CoinTier tier : values()) {
            byPath.put(tier.itemId(), tier);
            paths.add(tier.itemId());
        }
        BY_PATH = Map.copyOf(byPath);
        PATHS = List.copyOf(paths);
    }

    public String itemId() {
        return name().toLowerCase(Locale.ROOT) + "_coin";
    }

    public boolean canUpgrade() {
        return next() != null;
    }

    @Nullable
    public CoinTier next() {
        int index = ordinal() + 1;
        CoinTier[] values = values();
        return index < values.length ? values[index] : null;
    }

    public Item getItem() {
        return itemRegistry().get();
    }

    public RegistryObject<Item> itemRegistry() {
        return switch (this) {
            case COPPER -> ModItems.COPPER_COIN;
            case SILVER -> ModItems.SILVER_COIN;
            case GOLD -> ModItems.GOLD_COIN;
            case PLATINUM -> ModItems.PLATINUM_COIN;
            case SAPPHIRE -> ModItems.SAPPHIRE_COIN;
            case EMERALD -> ModItems.EMERALD_COIN;
            case RUBY -> ModItems.RUBY_COIN;
            case DIAMOND -> ModItems.DIAMOND_COIN;
            case OBSIDIAN -> ModItems.OBSIDIAN_COIN;
            case NETHERITE -> ModItems.NETHERITE_COIN;
        };
    }

    @Nullable
    public static CoinTier of(Item item) {
        if (item instanceof CoinItem coinItem) {
            return coinItem.getTier();
        }
        return null;
    }

    @Nullable
    public static CoinTier byPath(String path) {
        return path == null ? null : BY_PATH.get(path);
    }

    /** Item paths in tier order, lowest value first. */
    public static List<String> paths() {
        return PATHS;
    }
}
