package com.solarbiscuit.ratcurrency.inventory;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class CoinSlot extends SlotItemHandler {
    public CoinSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        if (stack.isEmpty()) {
            return getMaxStackSize();
        }
        return Math.min(getItemHandler().getSlotLimit(getSlotIndex()), stack.getMaxStackSize());
    }
}
