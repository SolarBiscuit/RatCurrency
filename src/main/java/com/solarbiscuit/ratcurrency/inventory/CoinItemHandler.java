package com.solarbiscuit.ratcurrency.inventory;

import com.solarbiscuit.ratcurrency.item.CoinContainerItem;
import com.solarbiscuit.ratcurrency.util.CoinHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import java.util.UUID;

public class CoinItemHandler extends ItemStackHandler {
    private final ItemStack containerStack;
    private final Runnable afterSave;
    private boolean silent;
    private boolean compressing;

    public CoinItemHandler(int size) {
        super(size);
        this.containerStack = ItemStack.EMPTY;
        this.afterSave = null;
    }

    public CoinItemHandler(ItemStack containerStack, int size) {
        this(containerStack, size, null);
    }

    public CoinItemHandler(ItemStack containerStack, int size, Runnable afterSave) {
        super(size);
        this.containerStack = containerStack;
        this.afterSave = afterSave;
        CompoundTag tag = containerStack.getTag();
        if (tag != null && tag.contains(CoinContainerItem.TAG_ITEMS)) {
            deserializeNBT(tag.getCompound(CoinContainerItem.TAG_ITEMS));
        }
        CoinContainerItem.getOrCreateId(containerStack);
    }

    public ItemStack getContainerStack() {
        return this.containerStack;
    }

    public void runSilent(Runnable runnable) {
        boolean previous = this.silent;
        this.silent = true;
        try {
            runnable.run();
        } finally {
            this.silent = previous;
        }
        save();
    }

    public ItemStack insertItemStacked(ItemStack stack) {
        if (stack.isEmpty() || !isItemValid(0, stack)) {
            return stack;
        }
        ItemStack remaining = ItemHandlerHelper.insertItemStacked(this, stack, false);
        compressAndSave();
        return remaining;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        if (!CoinHelper.isAllowedInContainer(stack)) {
            return false;
        }
        if (CoinHelper.isCoinContainer(stack) && !this.containerStack.isEmpty()) {
            UUID destId = CoinContainerItem.getId(this.containerStack);
            if (destId != null && CoinHelper.containsId(stack, destId)) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onContentsChanged(int slot) {
        if (this.silent) {
            return;
        }
        compressAndSave();
    }

    private void compressAndSave() {
        if (this.compressing || this.containerStack.isEmpty()) {
            save();
            return;
        }
        this.compressing = true;
        this.silent = true;
        try {
            CoinHelper.compress(this);
        } finally {
            this.silent = false;
            this.compressing = false;
        }
        save();
    }

    public void forceSave() {
        save();
    }

    private void save() {
        if (this.containerStack.isEmpty()) {
            return;
        }
        this.containerStack.getOrCreateTag().put(CoinContainerItem.TAG_ITEMS, serializeNBT());
        if (this.afterSave != null) {
            this.afterSave.run();
        }
    }
}
