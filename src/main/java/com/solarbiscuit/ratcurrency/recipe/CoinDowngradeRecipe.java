package com.solarbiscuit.ratcurrency.recipe;

import com.google.gson.JsonObject;
import com.solarbiscuit.ratcurrency.registry.ModRecipeSerializers;
import com.solarbiscuit.ratcurrency.util.CoinHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;

/**
 * One coin -> 100 coins of the tier below. A full stack (100+) in the grid upgrades instead:
 * without Polymorph this recipe stops matching there; with Polymorph it stays selectable and
 * RatLib's RecipePreference makes the upgrade the default pick.
 */
public class CoinDowngradeRecipe extends ShapelessRecipe {
    public CoinDowngradeRecipe(ResourceLocation id, String group, CraftingBookCategory category, ItemStack result,
                               NonNullList<Ingredient> ingredients) {
        super(id, group, category, result, ingredients);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        if (!super.matches(container, level)) {
            return false;
        }
        if (ModList.get().isLoaded("polymorph")) {
            return true;
        }
        int count = 0;
        for (int i = 0; i < container.getContainerSize(); i++) {
            count += container.getItem(i).getCount();
        }
        return count < CoinHelper.COINS_PER_TIER;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.COIN_DOWNGRADE.get();
    }

    private static CoinDowngradeRecipe wrap(ShapelessRecipe recipe) {
        return new CoinDowngradeRecipe(recipe.getId(), recipe.getGroup(), recipe.category(),
                recipe.getResultItem(null), recipe.getIngredients());
    }

    public static class Serializer implements RecipeSerializer<CoinDowngradeRecipe> {
        @Override
        public CoinDowngradeRecipe fromJson(ResourceLocation id, JsonObject json) {
            return wrap(RecipeSerializer.SHAPELESS_RECIPE.fromJson(id, json));
        }

        @Override
        public CoinDowngradeRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            return wrap(RecipeSerializer.SHAPELESS_RECIPE.fromNetwork(id, buf));
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, CoinDowngradeRecipe recipe) {
            RecipeSerializer.SHAPELESS_RECIPE.toNetwork(buf, recipe);
        }
    }
}
