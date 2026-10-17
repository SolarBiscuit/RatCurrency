package com.solarbiscuit.ratcurrency.item;

import com.solarbiscuit.ratcurrency.config.RatCurrencyConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class RatCoinItem extends Item {
    public static final int DEFAULT_MAX_STACK = 8;

    public RatCoinItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        if (RatCurrencyConfig.SPEC.isLoaded()) {
            return RatCurrencyConfig.get(RatCurrencyConfig.RAT_COIN_MAX_STACK);
        }
        return DEFAULT_MAX_STACK;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(this.getDescriptionId()).withStyle(ChatFormatting.LIGHT_PURPLE);
    }
}
