package com.solarbiscuit.ratcurrency.item;

import com.solarbiscuit.ratcurrency.config.RatCurrencyConfig;
import net.minecraft.world.item.ItemStack;

public class RatCoinSatchelItem extends CoinContainerItem {
    public RatCoinSatchelItem(Properties properties) {
        super(6, "container.ratcurrency.rat_coin_satchel", properties);
    }

    @Override
    public boolean isItemAllowed(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof RatCoinItem;
    }

    @Override
    public int getSlotLimit() {
        if (RatCurrencyConfig.SPEC.isLoaded()) {
            return RatCurrencyConfig.get(RatCurrencyConfig.RAT_COIN_MAX_STACK);
        }
        return RatCoinItem.DEFAULT_MAX_STACK;
    }

    @Override
    public boolean shouldCompress() {
        return false;
    }
}
