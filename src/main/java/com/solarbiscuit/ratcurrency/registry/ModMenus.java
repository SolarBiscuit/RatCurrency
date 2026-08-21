package com.solarbiscuit.ratcurrency.registry;

import com.solarbiscuit.ratcurrency.RatCurrency;
import com.solarbiscuit.ratcurrency.inventory.CoinContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, RatCurrency.MOD_ID);

    public static final RegistryObject<MenuType<CoinContainerMenu>> COIN_CONTAINER =
            MENUS.register("coin_container", () -> IForgeMenuType.create(CoinContainerMenu::fromNetwork));
}
