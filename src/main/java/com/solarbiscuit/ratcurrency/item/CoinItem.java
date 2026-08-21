package com.solarbiscuit.ratcurrency.item;

import net.minecraft.world.item.Item;

public class CoinItem extends Item {
    private final CoinTier tier;

    public CoinItem(CoinTier tier, Properties properties) {
        super(properties);
        this.tier = tier;
    }

    public CoinTier getTier() {
        return this.tier;
    }
}
