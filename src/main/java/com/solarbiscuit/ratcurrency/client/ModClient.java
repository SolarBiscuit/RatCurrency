package com.solarbiscuit.ratcurrency.client;

import com.solarbiscuit.ratcurrency.RatCurrency;
import com.solarbiscuit.ratcurrency.config.RatCurrencyConfig;
import com.solarbiscuit.ratcurrency.inventory.CoinContainerMenu;
import com.solarbiscuit.ratcurrency.item.CoinTier;
import com.solarbiscuit.ratcurrency.registry.ModMenus;
import com.solarbiscuit.ratlib.client.config.SolConfigEntries;
import com.solarbiscuit.ratlib.client.config.SolConfigScreen;
import com.solarbiscuit.ratlib.client.config.SolConfigTabs;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = RatCurrency.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModClient {
    @SubscribeEvent
    @SuppressWarnings("removal")
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(ModMenus.COIN_CONTAINER.get(), CoinContainerScreen::new));
        // Client setup runs mods in parallel; register on the main thread.
        event.enqueueWork(() -> SolConfigTabs.register(RatCurrency.MOD_ID, Component.translatable("screen.solconfig.tab.ratcurrency"), 2, entries -> {
            entries.hint(Component.translatable("screen.solconfig.common_hint"));
            entries.header(Component.translatable("options.ratcurrency.drops"));
            entries.percentSlider(Component.translatable("options.ratcurrency.drop_chance"),
                    RatCurrencyConfig.DROP_CHANCE, RatCurrencyConfig.SPEC);
            entries.percentSlider(Component.translatable("options.ratcurrency.vanilla_drop"),
                    RatCurrencyConfig.VANILLA_HOSTILE_DROP_CHANCE, RatCurrencyConfig.SPEC);
            entries.percentSlider(Component.translatable("options.ratcurrency.modded_drop"),
                    RatCurrencyConfig.MODDED_HOSTILE_DROP_CHANCE, RatCurrencyConfig.SPEC);
            entries.intSlider(Component.translatable("options.ratcurrency.min_copper"),
                    RatCurrencyConfig.MIN_COPPER, 1, 4096, RatCurrencyConfig.SPEC);
            entries.intSlider(Component.translatable("options.ratcurrency.max_copper"),
                    RatCurrencyConfig.MAX_COPPER, 1, 4096, RatCurrencyConfig.SPEC);
            entries.toggle(Component.translatable("options.ratcurrency.require_player_kill"),
                    RatCurrencyConfig.REQUIRE_PLAYER_KILL, RatCurrencyConfig.SPEC);
            entries.header(Component.translatable("options.ratcurrency.coins"));
            entries.toggle(Component.translatable("item.ratcurrency.copper_coin"),
                    RatCurrencyConfig.COPPER_ENABLED, RatCurrencyConfig.SPEC);
            entries.toggle(Component.translatable("item.ratcurrency.silver_coin"),
                    RatCurrencyConfig.SILVER_ENABLED, RatCurrencyConfig.SPEC);
            entries.toggle(Component.translatable("item.ratcurrency.gold_coin"),
                    RatCurrencyConfig.GOLD_ENABLED, RatCurrencyConfig.SPEC);
            entries.toggle(Component.translatable("item.ratcurrency.platinum_coin"),
                    RatCurrencyConfig.PLATINUM_ENABLED, RatCurrencyConfig.SPEC);
            entries.toggle(Component.translatable("item.ratcurrency.sapphire_coin"),
                    RatCurrencyConfig.SAPPHIRE_ENABLED, RatCurrencyConfig.SPEC);
            entries.toggle(Component.translatable("item.ratcurrency.emerald_coin"),
                    RatCurrencyConfig.EMERALD_ENABLED, RatCurrencyConfig.SPEC);
            entries.toggle(Component.translatable("item.ratcurrency.ruby_coin"),
                    RatCurrencyConfig.RUBY_ENABLED, RatCurrencyConfig.SPEC);
            entries.toggle(Component.translatable("item.ratcurrency.diamond_coin"),
                    RatCurrencyConfig.DIAMOND_ENABLED, RatCurrencyConfig.SPEC);
            entries.toggle(Component.translatable("item.ratcurrency.obsidian_coin"),
                    RatCurrencyConfig.OBSIDIAN_ENABLED, RatCurrencyConfig.SPEC);
            entries.toggle(Component.translatable("item.ratcurrency.netherite_coin"),
                    RatCurrencyConfig.NETHERITE_ENABLED, RatCurrencyConfig.SPEC);
            entries.toggle(Component.translatable("item.ratcurrency.rat_coin"),
                    RatCurrencyConfig.RAT_COIN_ENABLED, RatCurrencyConfig.SPEC);
            entries.header(Component.translatable("options.ratcurrency.other"));
            entries.toggle(Component.translatable("enchantment.ratcurrency.opulence"),
                    RatCurrencyConfig.OPULENCE_ENABLED, RatCurrencyConfig.SPEC);
            entries.intSlider(Component.translatable("options.ratcurrency.opulence_1"),
                    RatCurrencyConfig.OPULENCE_LEVEL_1, 0, 500, RatCurrencyConfig.SPEC);
            entries.intSlider(Component.translatable("options.ratcurrency.opulence_2"),
                    RatCurrencyConfig.OPULENCE_LEVEL_2, 0, 500, RatCurrencyConfig.SPEC);
            entries.intSlider(Component.translatable("options.ratcurrency.opulence_3"),
                    RatCurrencyConfig.OPULENCE_LEVEL_3, 0, 500, RatCurrencyConfig.SPEC);
            entries.intSlider(Component.translatable("options.ratcurrency.opulence_4"),
                    RatCurrencyConfig.OPULENCE_LEVEL_4, 0, 500, RatCurrencyConfig.SPEC);
            entries.intSlider(Component.translatable("options.ratcurrency.opulence_5"),
                    RatCurrencyConfig.OPULENCE_LEVEL_5, 0, 500, RatCurrencyConfig.SPEC);
            entries.intSlider(Component.translatable("options.ratcurrency.rat_coin_stack"),
                    RatCurrencyConfig.RAT_COIN_MAX_STACK, 1, 64, RatCurrencyConfig.SPEC);
            entries.header(Component.translatable("options.ratcurrency.urns"));
            entries.toggle(Component.translatable("options.ratcurrency.urn_coins"),
                    RatCurrencyConfig.URN_COINS_ENABLED, RatCurrencyConfig.SPEC);
            coinRange(entries, RatCurrencyConfig.URN_COINS);
            entries.header(Component.translatable("options.ratcurrency.skeletons"));
            entries.toggle(Component.translatable("options.ratcurrency.skeleton_coins"),
                    RatCurrencyConfig.SKELETON_COINS_ENABLED, RatCurrencyConfig.SPEC);
            coinRange(entries, RatCurrencyConfig.SKELETON_COINS);
        }));
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (mc, parent) -> SolConfigScreen.open(parent, RatCurrency.MOD_ID)));
    }

    private static void coinRange(SolConfigEntries entries, RatCurrencyConfig.CoinRange range) {
        entries.intSlider(Component.translatable("options.ratcurrency.min_amount"),
                range.minCount(), 1, 100, RatCurrencyConfig.SPEC);
        entries.enumCycle(Component.translatable("options.ratcurrency.min_coin"),
                range.minTier(), ModClient::coinName, RatCurrencyConfig.SPEC);
        entries.intSlider(Component.translatable("options.ratcurrency.max_amount"),
                range.maxCount(), 1, 100, RatCurrencyConfig.SPEC);
        entries.enumCycle(Component.translatable("options.ratcurrency.max_coin"),
                range.maxTier(), ModClient::coinName, RatCurrencyConfig.SPEC);
    }

    private static Component coinName(CoinTier tier) {
        return Component.translatable("item.ratcurrency." + tier.itemId());
    }
}
