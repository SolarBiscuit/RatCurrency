package com.solarbiscuit.ratcurrency.loot;

import com.solarbiscuit.ratcurrency.config.RatCurrencyConfig;
import com.solarbiscuit.ratcurrency.enchantment.OpulenceEnchantment;
import com.solarbiscuit.ratcurrency.util.CoinHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.List;

/** Rolls a configured coin range for block-break compat drops (urns, skeletons), plus the tool's Opulence bonus. */
final class CoinPoolDrops {
    private CoinPoolDrops() {
    }

    static List<ItemStack> roll(RatCurrencyConfig.CoinRange range, LootContext context) {
        long first = range.minCopper();
        long second = range.maxCopper();
        long min = Math.min(first, second);
        long max = Math.max(first, second);
        // Uniform over [min, max]; done in double so huge ranges (up to netherite) can't overflow.
        long units = min + (long) (context.getRandom().nextDouble() * ((double) max - min + 1));
        units = Math.max(min, Math.min(max, units));
        units = OpulenceEnchantment.applyBonus(units, context.getParamOrNull(LootContextParams.TOOL), context.getRandom());
        return CoinHelper.denominate(units);
    }
}
