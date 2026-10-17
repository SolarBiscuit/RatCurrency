package com.solarbiscuit.ratcurrency.registry;

import com.mojang.serialization.Codec;
import com.solarbiscuit.ratcurrency.RatCurrency;
import com.solarbiscuit.ratcurrency.loot.AddEnchantedBookLootModifier;
import com.solarbiscuit.ratcurrency.loot.SkeletonCoinsLootModifier;
import com.solarbiscuit.ratcurrency.loot.UrnCoinsLootModifier;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, RatCurrency.MOD_ID);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> ADD_ENCHANTED_BOOK =
            SERIALIZERS.register("add_enchanted_book", () -> AddEnchantedBookLootModifier.CODEC.get());
    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> URN_COINS =
            SERIALIZERS.register("urn_coins", () -> UrnCoinsLootModifier.CODEC.get());
    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> SKELETON_COINS =
            SERIALIZERS.register("skeleton_coins", () -> SkeletonCoinsLootModifier.CODEC.get());
}
