package com.solarbiscuit.ratcurrency;

import com.solarbiscuit.ratcurrency.advancement.ModCriteria;
import com.solarbiscuit.ratcurrency.config.RatCurrencyConfig;
import com.solarbiscuit.ratcurrency.economy.RatCoinService;
import com.solarbiscuit.ratcurrency.inventory.CoinContainerMenu;
import com.solarbiscuit.ratcurrency.inventory.OpenContext;
import com.solarbiscuit.ratcurrency.inventory.RestoreTarget;
import com.solarbiscuit.ratcurrency.item.CoinContainerItem;
import com.solarbiscuit.ratcurrency.network.ModNetwork;
import com.solarbiscuit.ratcurrency.recipe.CoinUpgradeRecipe;
import com.solarbiscuit.ratcurrency.registry.ModCreativeTabs;
import com.solarbiscuit.ratcurrency.registry.ModEnchantments;
import com.solarbiscuit.ratcurrency.registry.ModItems;
import com.solarbiscuit.ratcurrency.registry.ModLootModifiers;
import com.solarbiscuit.ratcurrency.registry.ModMenus;
import com.solarbiscuit.ratcurrency.registry.ModRecipeSerializers;
import com.solarbiscuit.ratlib.config.SolConfigPaths;
import com.solarbiscuit.ratlib.economy.CoinServices;
import com.solarbiscuit.ratlib.inventory.OpenFromSlot;
import com.solarbiscuit.ratlib.recipe.RecipePreference;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
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
        ModEnchantments.ENCHANTMENTS.register(modBus);
        ModCreativeTabs.TABS.register(modBus);
        ModMenus.MENUS.register(modBus);
        ModRecipeSerializers.SERIALIZERS.register(modBus);
        ModLootModifiers.SERIALIZERS.register(modBus);

        SolConfigPaths.migrateFile("ratcurrency-common.toml", SolConfigPaths.file(MOD_ID, "ratcurrency-common.toml"));
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, RatCurrencyConfig.SPEC,
                SolConfigPaths.file(MOD_ID, "ratcurrency-common.toml"));

        modBus.addListener(this::commonSetup);

        // Clients use the server's config values while connected (stack sizes, slot limits, enchanting checks).
        ModNetwork.register();
        modBus.addListener((ModConfigEvent.Reloading event) -> {
            if (event.getConfig().getSpec() == RatCurrencyConfig.SPEC) {
                ModNetwork.sendConfigToAll();
            }
        });
        MinecraftForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                ModNetwork.sendConfig(player);
            }
        });
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // CriteriaTriggers.register writes a plain HashMap; mod constructors run in parallel.
            ModCriteria.register();
            CoinServices.register(new RatCoinService());
            RecipePreference.register(recipe -> recipe instanceof CoinUpgradeRecipe);
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
