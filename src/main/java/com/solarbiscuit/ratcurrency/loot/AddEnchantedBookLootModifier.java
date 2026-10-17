package com.solarbiscuit.ratcurrency.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.solarbiscuit.ratcurrency.config.RatCurrencyConfig;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.Mth;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class AddEnchantedBookLootModifier extends LootModifier {
    public static final Supplier<Codec<AddEnchantedBookLootModifier>> CODEC = Suppliers.memoize(() ->
            RecordCodecBuilder.create(inst -> codecStart(inst)
                    .and(ForgeRegistries.ENCHANTMENTS.getCodec().fieldOf("enchantment").forGetter(m -> m.enchantment))
                    .and(Codec.FLOAT.fieldOf("chance").forGetter(m -> m.chance))
                    .and(Codec.INT.optionalFieldOf("minLevel", 1).forGetter(m -> m.minLevel))
                    .and(Codec.INT.optionalFieldOf("maxLevel", 1).forGetter(m -> m.maxLevel))
                    .apply(inst, AddEnchantedBookLootModifier::new)));

    private final Enchantment enchantment;
    private final float chance;
    private final int minLevel;
    private final int maxLevel;

    public AddEnchantedBookLootModifier(LootItemCondition[] conditions, Enchantment enchantment, float chance,
                                        int minLevel, int maxLevel) {
        super(conditions);
        this.enchantment = enchantment;
        this.chance = chance;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!RatCurrencyConfig.isOpulenceEnabled()) {
            return generatedLoot;
        }
        if (context.getRandom().nextFloat() < this.chance) {
            int min = Math.max(this.enchantment.getMinLevel(), this.minLevel);
            int max = Math.min(this.enchantment.getMaxLevel(), this.maxLevel);
            int level = min >= max ? min : Mth.nextInt(context.getRandom(), min, max);
            generatedLoot.add(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(this.enchantment, level)));
        }
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
