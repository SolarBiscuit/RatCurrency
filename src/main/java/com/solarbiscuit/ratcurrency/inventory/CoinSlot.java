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

    // SlotItemHandler inherits Slot.setChanged(), which never reaches the handler, but vanilla's shift-click
    // merge edits the stack in place and only calls setChanged(). Save without compressing: callers may
    // still hold the stack being edited.
    @Override
    public void setChanged() {
        super.setChanged();
        if (getItemHandler() instanceof CoinItemHandler handler) {
            handler.forceSave();
        }
    }
}
