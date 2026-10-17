package com.solarbiscuit.ratcurrency.registry;

import com.solarbiscuit.ratcurrency.RatCurrency;
import com.solarbiscuit.ratcurrency.recipe.CoinDowngradeRecipe;
import com.solarbiscuit.ratcurrency.recipe.CoinUpgradeRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, RatCurrency.MOD_ID);

    public static final RegistryObject<RecipeSerializer<CoinUpgradeRecipe>> COIN_UPGRADE =
            SERIALIZERS.register("coin_upgrade", CoinUpgradeRecipe.Serializer::new);
    public static final RegistryObject<RecipeSerializer<CoinDowngradeRecipe>> COIN_DOWNGRADE =
            SERIALIZERS.register("coin_downgrade", CoinDowngradeRecipe.Serializer::new);
}
