package com.solarbiscuit.ratcurrency.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.solarbiscuit.ratcurrency.config.RatCurrencyConfig;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

/**
 * Adds coins when a Supplementaries treasure urn breaks. Only treasure urns (cave worldgen) count, so crafted
 * urns can't be placed and smashed for free coins. Opulence on the breaking tool adds its usual bonus.
 * Looks the block up by id so Supplementaries stays an optional dependency.
 */
public class UrnCoinsLootModifier extends LootModifier {
    public static final Supplier<Codec<UrnCoinsLootModifier>> CODEC = Suppliers.memoize(() ->
            RecordCodecBuilder.create(inst -> codecStart(inst).apply(inst, UrnCoinsLootModifier::new)));

    private static final ResourceLocation URN = ResourceLocation.fromNamespaceAndPath("supplementaries", "urn");

    public UrnCoinsLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!RatCurrencyConfig.isUrnCoinsEnabled()) {
            return generatedLoot;
        }
        BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
        if (state == null || !URN.equals(ForgeRegistries.BLOCKS.getKey(state.getBlock())) || !isTreasure(state)) {
            return generatedLoot;
        }
        generatedLoot.addAll(CoinPoolDrops.roll(RatCurrencyConfig.URN_COINS, context));
        return generatedLoot;
    }

    private static boolean isTreasure(BlockState state) {
        Property<?> property = state.getBlock().getStateDefinition().getProperty("treasure");
        return property instanceof BooleanProperty treasure && state.getValue(treasure);
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
