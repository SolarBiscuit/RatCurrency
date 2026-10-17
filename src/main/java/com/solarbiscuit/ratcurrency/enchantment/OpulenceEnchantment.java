package com.solarbiscuit.ratcurrency.enchantment;

import com.solarbiscuit.ratcurrency.config.RatCurrencyConfig;
import com.solarbiscuit.ratcurrency.registry.ModEnchantments;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.common.ToolActions;

public class OpulenceEnchantment extends Enchantment {
    public OpulenceEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMinCost(int level) {
        return 15 + (level - 1) * 9;
    }

    @Override
    public int getMaxCost(int level) {
        return getMinCost(level) + 50;
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return RatCurrencyConfig.isOpulenceEnabled() && isSword(stack);
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return RatCurrencyConfig.isOpulenceEnabled() && isCompatibleItem(stack);
    }

    @Override
    public boolean isAllowedOnBooks() {
        return RatCurrencyConfig.isOpulenceEnabled();
    }

    @Override
    public boolean isDiscoverable() {
        return RatCurrencyConfig.isOpulenceEnabled();
    }

    @Override
    public boolean isTradeable() {
        return RatCurrencyConfig.isOpulenceEnabled();
    }

    /** Adds the Opulence bonus for {@code tool}'s level to a copper-unit amount; fractional coins round up by chance. */
    public static long applyBonus(long units, ItemStack tool, RandomSource random) {
        if (!RatCurrencyConfig.isOpulenceEnabled() || units <= 0 || tool == null || tool.isEmpty()) {
            return units;
        }
        int level = tool.getEnchantmentLevel(ModEnchantments.OPULENCE.get());
        if (level <= 0) {
            return units;
        }
        int bonusPercent = RatCurrencyConfig.getOpulenceBonusPercent(level);
        if (units > Long.MAX_VALUE / (1 + bonusPercent / 100 + 1)) {
            return units; // already astronomically large; avoid overflowing
        }
        long extra = units / 100 * bonusPercent + (units % 100) * bonusPercent / 100;
        long remainder = (units % 100) * bonusPercent % 100;
        if (random.nextInt(100) < remainder) {
            extra++;
        }
        return units + extra;
    }

    public static boolean isSword(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return stack.getItem() instanceof SwordItem || stack.is(ItemTags.SWORDS);
    }

    public static boolean isCompatibleItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        Item item = stack.getItem();
        if (item instanceof SwordItem || item instanceof PickaxeItem || item instanceof AxeItem) {
            return true;
        }
        if (stack.is(ItemTags.SWORDS) || stack.is(ItemTags.PICKAXES) || stack.is(ItemTags.AXES)) {
            return true;
        }
        return stack.canPerformAction(ToolActions.PICKAXE_DIG) && stack.canPerformAction(ToolActions.AXE_DIG);
    }
}
