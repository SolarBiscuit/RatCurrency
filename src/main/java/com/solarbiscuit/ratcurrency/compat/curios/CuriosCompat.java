package com.solarbiscuit.ratcurrency.compat.curios;

import com.solarbiscuit.ratcurrency.inventory.OpenContext;
import com.solarbiscuit.ratcurrency.item.CoinContainerItem;
import com.solarbiscuit.ratcurrency.item.CoinTier;
import com.solarbiscuit.ratcurrency.util.CoinHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import javax.annotation.Nullable;

public final class CuriosCompat {
    public static final String MOD_ID = "curios";
    public static final String BELT = "belt";

    private CuriosCompat() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    public static ItemStack insertIntoBelt(Player player, ItemStack coins) {
        if (!isLoaded() || coins.isEmpty()) {
            return coins;
        }
        return insertIntoBeltLoaded(player, coins);
    }

    private static ItemStack insertIntoBeltLoaded(Player player, ItemStack coins) {
        ItemStack remaining = coins;
        var curios = CuriosApi.getCuriosInventory(player).orElse(null);
        if (curios == null) {
            return remaining;
        }
        var belt = curios.getStacksHandler(BELT).orElse(null);
        if (belt == null) {
            return remaining;
        }
        IDynamicStackHandler stacks = belt.getStacks();
        for (int i = 0; i < stacks.getSlots() && !remaining.isEmpty(); i++) {
            ItemStack stored = stacks.getStackInSlot(i);
            if (stored.getItem() instanceof CoinContainerItem container) {
                remaining = container.createHandler(stored).insertItemStacked(remaining);
            }
        }
        return remaining;
    }

    public static ItemStack getStack(Player player, String identifier, int slot) {
        if (!isLoaded()) {
            return ItemStack.EMPTY;
        }
        return getStackLoaded(player, identifier, slot);
    }

    private static ItemStack getStackLoaded(Player player, String identifier, int slot) {
        return CuriosApi.getCuriosInventory(player)
                .map(handler -> handler.getStacksHandler(identifier)
                        .map(stacks -> {
                            IDynamicStackHandler dynamic = stacks.getStacks();
                            if (slot < 0 || slot >= dynamic.getSlots()) {
                                return ItemStack.EMPTY;
                            }
                            return dynamic.getStackInSlot(slot);
                        })
                        .orElse(ItemStack.EMPTY))
                .orElse(ItemStack.EMPTY);
    }

    @Nullable
    public static OpenContext fromSlot(Player player, Slot slot) {
        if (!isLoaded()) {
            return null;
        }
        return fromSlotLoaded(player, slot);
    }

    @Nullable
    private static OpenContext fromSlotLoaded(Player player, Slot slot) {
        if (!(slot instanceof SlotItemHandler handlerSlot)) {
            return null;
        }
        IItemHandler handler = handlerSlot.getItemHandler();
        return CuriosApi.getCuriosInventory(player).map(inv -> {
            for (var entry : inv.getCurios().entrySet()) {
                if (entry.getValue().getStacks() == handler) {
                    return OpenContext.curios(entry.getKey(), slot.getContainerSlot());
                }
            }
            return null;
        }).orElse(null);
    }

    public static int countCoins(Player player, CoinTier tier) {
        if (!isLoaded()) {
            return 0;
        }
        return countCoinsLoaded(player, tier);
    }

    private static int countCoinsLoaded(Player player, CoinTier tier) {
        return CuriosApi.getCuriosInventory(player).map(inv -> {
            int total = 0;
            for (var entry : inv.getCurios().entrySet()) {
                IDynamicStackHandler stacks = entry.getValue().getStacks();
                for (int i = 0; i < stacks.getSlots(); i++) {
                    total += CoinHelper.countInStack(stacks.getStackInSlot(i), tier);
                }
            }
            return total;
        }).orElse(0);
    }
}
