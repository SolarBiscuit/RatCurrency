package com.solarbiscuit.ratcurrency.registry;

import com.solarbiscuit.ratcurrency.RatCurrency;
import com.solarbiscuit.ratcurrency.util.CoinHelper;
import com.solarbiscuit.ratcurrency.item.CoinContainerItem;
import com.solarbiscuit.ratcurrency.item.CoinItem;
import com.solarbiscuit.ratcurrency.item.CoinTier;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, RatCurrency.MOD_ID);

    public static final RegistryObject<Item> COPPER_COIN = coin(CoinTier.COPPER);
    public static final RegistryObject<Item> SILVER_COIN = coin(CoinTier.SILVER);
    public static final RegistryObject<Item> GOLD_COIN = coin(CoinTier.GOLD);
    public static final RegistryObject<Item> PLATINUM_COIN = coin(CoinTier.PLATINUM);
    public static final RegistryObject<Item> SAPPHIRE_COIN = coin(CoinTier.SAPPHIRE);
    public static final RegistryObject<Item> EMERALD_COIN = coin(CoinTier.EMERALD);
    public static final RegistryObject<Item> RUBY_COIN = coin(CoinTier.RUBY);
    public static final RegistryObject<Item> DIAMOND_COIN = coin(CoinTier.DIAMOND);
    public static final RegistryObject<Item> NETHERITE_COIN = coin(CoinTier.NETHERITE);

    public static final RegistryObject<Item> COIN_POUCH = ITEMS.register("coin_pouch",
            () -> new CoinContainerItem(9, "container.ratcurrency.coin_pouch",
                    new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> COIN_CASE = ITEMS.register("coin_case",
            () -> new CoinContainerItem(27, "container.ratcurrency.coin_case",
                    new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> COIN_CRATE = ITEMS.register("coin_crate",
            () -> new CoinContainerItem(96, "container.ratcurrency.coin_crate",
                    new Item.Properties().stacksTo(1)));

    private static RegistryObject<Item> coin(CoinTier tier) {
        return ITEMS.register(tier.itemId(), () -> new CoinItem(tier, new Item.Properties().stacksTo(CoinHelper.COINS_PER_TIER)));
    }
}
