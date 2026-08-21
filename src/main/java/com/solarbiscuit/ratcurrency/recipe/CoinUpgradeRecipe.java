package com.solarbiscuit.ratcurrency.recipe;

import com.google.gson.JsonObject;
import com.solarbiscuit.ratcurrency.registry.ModRecipeSerializers;
import com.solarbiscuit.ratcurrency.util.CoinHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public class CoinUpgradeRecipe extends CustomRecipe {
    private final Item ingredient;
    private final Item result;

    public CoinUpgradeRecipe(ResourceLocation id, Item ingredient, Item result) {
        super(id, CraftingBookCategory.MISC);
        this.ingredient = ingredient;
        this.result = result;
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        int count = 0;
        boolean found = false;
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.getItem() != this.ingredient) {
                return false;
            }
            found = true;
            count += stack.getCount();
        }
        return found && count >= CoinHelper.COINS_PER_TIER;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        return new ItemStack(this.result);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return new ItemStack(this.result);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
        int nonEmpty = 0;
        for (int i = 0; i < container.getContainerSize(); i++) {
            if (!container.getItem(i).isEmpty()) {
                nonEmpty++;
            }
        }
        int extraToRemove = CoinHelper.COINS_PER_TIER - nonEmpty;
        int remaining = extraToRemove;
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            int take = Math.min(remaining, Math.max(0, stack.getCount() - 1));
            if (take > 0) {
                stack.shrink(take);
                remaining -= take;
            }
        }
        return super.getRemainingItems(container);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.COIN_UPGRADE.get();
    }

    public static class Serializer implements RecipeSerializer<CoinUpgradeRecipe> {
        @Override
        public CoinUpgradeRecipe fromJson(ResourceLocation id, JsonObject json) {
            ResourceLocation ingredientId = ResourceLocation.parse(GsonHelper.getAsString(json, "ingredient"));
            ResourceLocation resultId = ResourceLocation.parse(GsonHelper.getAsString(json, "result"));
            Item ingredient = ForgeRegistries.ITEMS.getValue(ingredientId);
            Item result = ForgeRegistries.ITEMS.getValue(resultId);
            if (ingredient == null || result == null) {
                throw new IllegalArgumentException("Unknown item in coin upgrade recipe " + id);
            }
            return new CoinUpgradeRecipe(id, ingredient, result);
        }

        @Override
        public CoinUpgradeRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            Item ingredient = ForgeRegistries.ITEMS.getValue(buf.readResourceLocation());
            Item result = ForgeRegistries.ITEMS.getValue(buf.readResourceLocation());
            return new CoinUpgradeRecipe(id, ingredient, result);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, CoinUpgradeRecipe recipe) {
            buf.writeResourceLocation(ForgeRegistries.ITEMS.getKey(recipe.ingredient));
            buf.writeResourceLocation(ForgeRegistries.ITEMS.getKey(recipe.result));
        }
    }
}
