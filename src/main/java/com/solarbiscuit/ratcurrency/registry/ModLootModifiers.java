package com.solarbiscuit.ratcurrency.registry;

import com.mojang.serialization.Codec;
import com.solarbiscuit.ratcurrency.RatCurrency;
import com.solarbiscuit.ratcurrency.loot.AddItemLootModifier;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, RatCurrency.MOD_ID);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> ADD_ITEM =
            SERIALIZERS.register("add_item", () -> AddItemLootModifier.CODEC.get());
}
