package com.solarbiscuit.ratcurrency;

import com.solarbiscuit.ratcurrency.advancement.ModCriteria;
import com.solarbiscuit.ratcurrency.config.RatCurrencyConfig;
import com.solarbiscuit.ratcurrency.economy.RatCoinService;
import com.solarbiscuit.ratcurrency.inventory.CoinContainerMenu;
import com.solarbiscuit.ratcurrency.inventory.OpenContext;
import com.solarbiscuit.ratcurrency.inventory.RestoreTarget;
import com.solarbiscuit.ratcurrency.item.CoinContainerItem;
import com.solarbiscuit.ratcurrency.registry.ModCreativeTabs;
import com.solarbiscuit.ratcurrency.registry.ModItems;
import com.solarbiscuit.ratcurrency.registry.ModMenus;
import com.solarbiscuit.ratcurrency.registry.ModRecipeSerializers;
import com.solarbiscuit.ratlib.economy.CoinServices;
import com.solarbiscuit.ratlib.inventory.OpenFromSlot;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(RatCurrency.MOD_ID)
public class RatCurrency {
    public static final String MOD_ID = "ratcurrency";
    public static final Logger LOGGER = LogUtils.getLogger();

    @SuppressWarnings("removal")
    public RatCurrency() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.ITEMS.register(modBus);
        ModCreativeTabs.TABS.register(modBus);
        ModMenus.MENUS.register(modBus);
        ModRecipeSerializers.SERIALIZERS.register(modBus);

        ModCriteria.register();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, RatCurrencyConfig.SPEC);

        modBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            CoinServices.register(new RatCoinService());
            OpenFromSlot.register(
                    (stack, menu) -> stack.getItem() instanceof CoinContainerItem,
                    (player, menu, slot) -> {
                        OpenContext context = OpenContext.fromMenuSlot(player, menu, slot);
                        if (context == null) {
                            return false;
                        }
                        RestoreTarget restore = RestoreTarget.fromCurrentMenu(player);
                        if (menu instanceof CoinContainerMenu coinMenu) {
                            coinMenu.skipRestore();
                        }
                        CoinContainerMenu.open(player, context, restore);
                        return true;
                    }
            );
        });
    }
}
