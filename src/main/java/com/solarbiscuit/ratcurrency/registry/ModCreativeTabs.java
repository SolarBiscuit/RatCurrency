package com.solarbiscuit.ratcurrency.registry;

import com.solarbiscuit.ratcurrency.RatCurrency;
import com.solarbiscuit.ratcurrency.config.RatCurrencyConfig;
import com.solarbiscuit.ratcurrency.item.CoinTier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = RatCurrency.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RatCurrency.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.ratcurrency"))
            .icon(() -> new ItemStack(ModItems.GOLD_COIN.get()))
            .displayItems((params, output) -> {
                for (CoinTier tier : CoinTier.values()) {
                    if (RatCurrencyConfig.isCoinEnabled(tier)) {
                        output.accept(tier.getItem());
                    }
                }
                if (RatCurrencyConfig.isRatCoinEnabled()) {
                    output.accept(ModItems.RAT_COIN.get());
                }
                output.accept(ModItems.COIN_POUCH.get());
                output.accept(ModItems.COIN_CASE.get());
                output.accept(ModItems.COIN_CRATE.get());
                output.accept(ModItems.RAT_COIN_SATCHEL.get());
            })
            .build());

    @SubscribeEvent
    public static void onBuildCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.COIN_POUCH.get());
            event.accept(ModItems.COIN_CASE.get());
            event.accept(ModItems.COIN_CRATE.get());
            event.accept(ModItems.RAT_COIN_SATCHEL.get());
        }
    }
}
