package com.solarbiscuit.ratcurrency.compat.curios;

import com.solarbiscuit.ratcurrency.inventory.OpenContext;
import com.solarbiscuit.ratcurrency.item.CoinTier;
import com.solarbiscuit.ratcurrency.util.CoinHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import javax.annotation.Nullable;

public final class CuriosCompat {
    public static final String MOD_ID = "curios";
    public static final String CURRENCY = "currency";
    public static final String BELT = "belt";
    public static final String HANDS = "hands";

    private CuriosCompat() {
    }

    public static boolean isLoaded() {
        return com.solarbiscuit.ratlib.compat.curios.CuriosCompat.isLoaded();
    }

    public static ItemStack insertIntoBelt(Player player, ItemStack coins) {
        if (!isLoaded() || coins.isEmpty()) {
            return coins;
        }
        ItemStack remaining = insertIntoSlot(player, CURRENCY, coins);
        if (remaining.isEmpty()) {
            return remaining;
        }
        remaining = insertIntoSlot(player, BELT, remaining);
        if (remaining.isEmpty()) {
            return remaining;
        }
        return insertIntoSlot(player, HANDS, remaining);
    }

    private static ItemStack insertIntoSlot(Player player, String slotId, ItemStack coins) {
        return com.solarbiscuit.ratlib.compat.curios.CuriosCompat.insertIntoSlot(player, slotId, coins,
                (handler, slot, stored, remaining) -> CoinHelper.insertInto(stored, remaining));
    }

    public static ItemStack getStack(Player player, String identifier, int slot) {
        return com.solarbiscuit.ratlib.compat.curios.CuriosCompat.getStack(player, identifier, slot);
    }

    @Nullable
    public static OpenContext fromSlot(Player player, Slot slot) {
        if (!isLoaded()) {
            return null;
        }
        String identifier = com.solarbiscuit.ratlib.compat.curios.CuriosCompat.identifierMatching(player, slot);
        if (identifier == null) {
            return null;
        }
        return OpenContext.curios(identifier, slot.getContainerSlot());
    }

    public static int countCoins(Player player, CoinTier tier) {
        return com.solarbiscuit.ratlib.compat.curios.CuriosCompat.countInAllSlots(player,
                stack -> CoinHelper.countInStack(stack, tier));
    }

    public static int takeCoins(Player player, CoinTier tier, int remaining) {
        if (!isLoaded() || remaining <= 0) {
            return remaining;
        }
        return takeCoinsLoaded(player, tier, remaining);
    }

    private static int takeCoinsLoaded(Player player, CoinTier tier, int remaining) {
        var curios = CuriosApi.getCuriosInventory(player).orElse(null);
        if (curios == null) {
            return remaining;
        }
        for (var entry : curios.getCurios().entrySet()) {
            IDynamicStackHandler stacks = entry.getValue().getStacks();
            for (int i = 0; i < stacks.getSlots() && remaining > 0; i++) {
                ItemStack stored = stacks.getStackInSlot(i);
                remaining = CoinHelper.takeFromStack(stored, tier, remaining);
                if (stored.isEmpty()) {
                    stacks.setStackInSlot(i, ItemStack.EMPTY);
                }
            }
        }
        return remaining;
    }
}
