package com.solarbiscuit.ratcurrency.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.solarbiscuit.ratcurrency.config.RatCurrencyConfig;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

import java.util.function.Supplier;

/**
 * Adds coins when a RealmRPG: Fallen Adventurers skeleton block breaks. Matches every skeleton's own block loot
 * table, so new skeletons are covered automatically. Like the mod's own loot, nothing drops with Silk Touch (which
 * just picks the skeleton up), so moving a skeleton never pays out twice. Opulence adds its usual bonus.
 */
public class SkeletonCoinsLootModifier extends LootModifier {
    public static final Supplier<Codec<SkeletonCoinsLootModifier>> CODEC = Suppliers.memoize(() ->
            RecordCodecBuilder.create(inst -> codecStart(inst).apply(inst, SkeletonCoinsLootModifier::new)));

    private static final String NAMESPACE = "realmrpg_skeletons";

    public SkeletonCoinsLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!RatCurrencyConfig.isSkeletonCoinsEnabled()) {
            return generatedLoot;
        }
        ResourceLocation table = context.getQueriedLootTableId();
        if (!NAMESPACE.equals(table.getNamespace()) || !table.getPath().startsWith("blocks/")
                || !table.getPath().contains("skeleton") || !context.hasParam(LootContextParams.BLOCK_STATE)) {
            return generatedLoot;
        }
        ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
        if (tool != null && tool.getEnchantmentLevel(Enchantments.SILK_TOUCH) > 0) {
            return generatedLoot;
        }
        generatedLoot.addAll(CoinPoolDrops.roll(RatCurrencyConfig.SKELETON_COINS, context));
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
