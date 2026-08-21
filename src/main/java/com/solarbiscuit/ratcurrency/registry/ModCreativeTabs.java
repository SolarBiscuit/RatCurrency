package com.solarbiscuit.ratcurrency.registry;

import com.solarbiscuit.ratcurrency.RatCurrency;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RatCurrency.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.ratcurrency"))
            .icon(() -> new ItemStack(ModItems.GOLD_COIN.get()))
            .displayItems((params, output) -> {
                output.accept(ModItems.COPPER_COIN.get());
                output.accept(ModItems.SILVER_COIN.get());
                output.accept(ModItems.GOLD_COIN.get());
                output.accept(ModItems.PLATINUM_COIN.get());
                output.accept(ModItems.SAPPHIRE_COIN.get());
                output.accept(ModItems.EMERALD_COIN.get());
                output.accept(ModItems.RUBY_COIN.get());
                output.accept(ModItems.DIAMOND_COIN.get());
                output.accept(ModItems.NETHERITE_COIN.get());
                output.accept(ModItems.COIN_POUCH.get());
                output.accept(ModItems.COIN_CASE.get());
                output.accept(ModItems.COIN_CRATE.get());
            })
            .build());
}
