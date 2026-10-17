package com.solarbiscuit.ratcurrency.inventory;

import com.solarbiscuit.ratcurrency.compat.curios.CuriosCompat;
import com.solarbiscuit.ratcurrency.item.CoinContainerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OpenContext {
    public enum Type {
        HAND,
        PLAYER,
        BLOCK,
        ENDER,
        NESTED,
        CURIOS
    }

    private final Type type;
    @Nullable
    private final InteractionHand hand;
    private final int slotIndex;
    @Nullable
    private final BlockPos pos;
    @Nullable
    private final OpenContext parent;
    @Nullable
    private final String curiosId;

    private OpenContext(Type type, @Nullable InteractionHand hand, int slotIndex, @Nullable BlockPos pos,
                        @Nullable OpenContext parent, @Nullable String curiosId) {
        this.type = type;
        this.hand = hand;
        this.slotIndex = slotIndex;
        this.pos = pos;
        this.parent = parent;
        this.curiosId = curiosId;
    }

    public static OpenContext hand(InteractionHand hand) {
        return new OpenContext(Type.HAND, hand, -1, null, null, null);
    }

    public static OpenContext player(int slot) {
        return new OpenContext(Type.PLAYER, null, slot, null, null, null);
    }

    public static OpenContext block(BlockPos pos, int slot) {
        return new OpenContext(Type.BLOCK, null, slot, pos, null, null);
    }

    public static OpenContext ender(int slot) {
        return new OpenContext(Type.ENDER, null, slot, null, null, null);
    }

    public static OpenContext nested(OpenContext parent, int slot) {
        return new OpenContext(Type.NESTED, null, slot, null, parent, null);
    }

    public static OpenContext curios(String identifier, int slot) {
        return new OpenContext(Type.CURIOS, null, slot, null, null, identifier);
    }

    public Type getType() {
        return this.type;
    }

    public int getSlotIndex() {
        return this.slotIndex;
    }

    @Nullable
    public OpenContext getParent() {
        return this.parent;
    }

    public ItemStack getStack(Player player) {
        return switch (this.type) {
            case HAND -> player.getItemInHand(this.hand);
            case PLAYER -> player.getInventory().getItem(this.slotIndex);
            case BLOCK -> getBlockStack(player);
            case ENDER -> player.getEnderChestInventory().getItem(this.slotIndex);
            case NESTED -> getNestedStack(player);
            case CURIOS -> CuriosCompat.getStack(player, this.curiosId, this.slotIndex);
        };
    }

    public CoinItemHandler createHandler(Player player) {
        if (this.type == Type.NESTED && this.parent != null) {
            CoinItemHandler parentHandler = this.parent.createHandler(player);
            ItemStack child = this.slotIndex >= 0 && this.slotIndex < parentHandler.getSlots()
                    ? parentHandler.getStackInSlot(this.slotIndex)
                    : ItemStack.EMPTY;
            if (!(child.getItem() instanceof CoinContainerItem item)) {
                return new CoinItemHandler(9);
            }
            CoinItemHandler handler = new CoinItemHandler(child, item.getSlotCount(), parentHandler::forceSave);
            handler.setAncestors(parentHandler.getAncestors() + 1);
            return handler;
        }
        ItemStack stack = getStack(player);
        if (!(stack.getItem() instanceof CoinContainerItem item)) {
            return new CoinItemHandler(9);
        }
        if (this.type == Type.BLOCK && this.pos != null) {
            // Saves edit the item's NBT in place, which the block entity never hears about. Mark it changed so
            // the chunk is saved; otherwise coins taken out of a pouch in a chest could reappear after a reload.
            Level level = player.level();
            BlockPos blockPos = this.pos;
            return new CoinItemHandler(stack, item.getSlotCount(), () -> {
                BlockEntity be = level.getBlockEntity(blockPos);
                if (be != null) {
                    be.setChanged();
                }
            });
        }
        return item.createHandler(stack);
    }

    /**
     * False when the block's inventory hands out copies of its stacks: edits would then never reach the real item,
     * and taking coins out would duplicate them.
     */
    public boolean editsReachItem(Player player) {
        if (this.type == Type.NESTED) {
            return this.parent == null || this.parent.editsReachItem(player);
        }
        if (this.type != Type.BLOCK || this.pos == null) {
            return true;
        }
        BlockEntity be = player.level().getBlockEntity(this.pos);
        if (be == null) {
            return false;
        }
        IItemHandler handler = be.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
        if (handler == null || this.slotIndex < 0 || this.slotIndex >= handler.getSlots()) {
            return false;
        }
        return handler.getStackInSlot(this.slotIndex) == handler.getStackInSlot(this.slotIndex);
    }

    /** Ids of the coin containers this context sits inside, innermost first. */
    public List<UUID> ancestorIds(Player player) {
        List<UUID> ids = new ArrayList<>();
        for (OpenContext context = this.parent; context != null; context = context.parent) {
            UUID id = context.expectedId(player);
            if (id != null) {
                ids.add(id);
            }
        }
        return ids;
    }

    /** {@code stack} is this context's current stack, already resolved by the caller. */
    public boolean stillValid(Player player, ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof CoinContainerItem)) {
            return false;
        }
        return switch (this.type) {
            case HAND, PLAYER, ENDER, NESTED, CURIOS -> true;
            case BLOCK -> this.pos != null
                    && player.level().getBlockEntity(this.pos) != null
                    && player.distanceToSqr(this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5) <= 64.0;
        };
    }

    public int lockedPlayerSlot(Player player) {
        return switch (this.type) {
            case PLAYER -> this.slotIndex;
            case HAND -> this.hand == InteractionHand.MAIN_HAND
                    ? player.getInventory().selected
                    : Inventory.SLOT_OFFHAND;
            default -> -1;
        };
    }

    private ItemStack getBlockStack(Player player) {
        if (this.pos == null) {
            return ItemStack.EMPTY;
        }
        BlockEntity be = player.level().getBlockEntity(this.pos);
        if (be == null) {
            return ItemStack.EMPTY;
        }
        return be.getCapability(ForgeCapabilities.ITEM_HANDLER)
                .map(handler -> this.slotIndex >= 0 && this.slotIndex < handler.getSlots()
                        ? handler.getStackInSlot(this.slotIndex)
                        : ItemStack.EMPTY)
                .orElse(ItemStack.EMPTY);
    }

    private ItemStack getNestedStack(Player player) {
        if (this.parent == null) {
            return ItemStack.EMPTY;
        }
        ItemStack parentStack = this.parent.getStack(player);
        if (!(parentStack.getItem() instanceof CoinContainerItem)) {
            return ItemStack.EMPTY;
        }
        // Read-only copy: going through a handler would stamp Ids onto stacks as a side effect.
        List<ItemStack> contents = CoinContainerItem.readContents(parentStack);
        if (this.slotIndex < 0 || this.slotIndex >= contents.size()) {
            return ItemStack.EMPTY;
        }
        return contents.get(this.slotIndex);
    }

    @Nullable
    public static OpenContext fromMenuSlot(Player player, AbstractContainerMenu menu, Slot slot) {
        if (!(slot.getItem().getItem() instanceof CoinContainerItem)) {
            return null;
        }
        if (menu instanceof CoinContainerMenu coinMenu) {
            if (slot.index < coinMenu.getContainerSlotCount()) {
                return nested(coinMenu.getOpenContext(), slot.getContainerSlot());
            }
            if (slot.container == player.getInventory()) {
                return player(slot.getContainerSlot());
            }
        }
        if (slot.container == player.getInventory()) {
            return player(slot.getContainerSlot());
        }
        if (slot.container == player.getEnderChestInventory()) {
            return ender(slot.getContainerSlot());
        }
        OpenContext curios = CuriosCompat.fromSlot(player, slot);
        if (curios != null) {
            return curios;
        }
        BlockPos pos = findBlockPos(player, slot);
        if (pos != null) {
            return block(pos, slot.getContainerSlot());
        }
        return null;
    }

    @Nullable
    static BlockPos findBlockPos(Player player, Slot slot) {
        if (slot.container instanceof BlockEntity be) {
            return be.getBlockPos();
        }

        IItemHandler slotHandler = slot instanceof SlotItemHandler handlerSlot ? handlerSlot.getItemHandler() : null;
        Level level = player.level();
        BlockPos origin = player.blockPosition();
        int range = 10;
        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos pos = origin.offset(x, y, z);
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be == null) {
                        continue;
                    }
                    if (be == slot.container) {
                        return pos;
                    }
                    if (slot.container instanceof CompoundContainer compound && be instanceof ChestBlockEntity chest
                            && compound.contains(chest)) {
                        return pos;
                    }
                    if (slotHandler != null && be.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null) == slotHandler) {
                        return pos;
                    }
                }
            }
        }
        return null;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeEnum(this.type);
        switch (this.type) {
            case HAND -> buf.writeEnum(this.hand);
            case PLAYER, ENDER -> buf.writeVarInt(this.slotIndex);
            case BLOCK -> {
                buf.writeBlockPos(this.pos);
                buf.writeVarInt(this.slotIndex);
            }
            case NESTED -> {
                buf.writeVarInt(this.slotIndex);
                this.parent.write(buf);
            }
            case CURIOS -> {
                buf.writeUtf(this.curiosId);
                buf.writeVarInt(this.slotIndex);
            }
        }
    }

    public static OpenContext read(FriendlyByteBuf buf) {
        Type type = buf.readEnum(Type.class);
        return switch (type) {
            case HAND -> hand(buf.readEnum(InteractionHand.class));
            case PLAYER -> player(buf.readVarInt());
            case BLOCK -> block(buf.readBlockPos(), buf.readVarInt());
            case ENDER -> ender(buf.readVarInt());
            case NESTED -> {
                int slot = buf.readVarInt();
                OpenContext parent = read(buf);
                yield nested(parent, slot);
            }
            case CURIOS -> curios(buf.readUtf(), buf.readVarInt());
        };
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Type", this.type.name());
        if (this.hand != null) {
            tag.putString("Hand", this.hand.name());
        }
        tag.putInt("Slot", this.slotIndex);
        if (this.pos != null) {
            tag.putLong("Pos", this.pos.asLong());
        }
        if (this.parent != null) {
            tag.put("Parent", this.parent.save());
        }
        if (this.curiosId != null) {
            tag.putString("CuriosId", this.curiosId);
        }
        return tag;
    }

    public static OpenContext load(CompoundTag tag) {
        Type type = Type.valueOf(tag.getString("Type"));
        return switch (type) {
            case HAND -> hand(InteractionHand.valueOf(tag.getString("Hand")));
            case PLAYER -> player(tag.getInt("Slot"));
            case BLOCK -> block(BlockPos.of(tag.getLong("Pos")), tag.getInt("Slot"));
            case ENDER -> ender(tag.getInt("Slot"));
            case NESTED -> nested(load(tag.getCompound("Parent")), tag.getInt("Slot"));
            case CURIOS -> curios(tag.getString("CuriosId"), tag.getInt("Slot"));
        };
    }

    @Nullable
    public UUID expectedId(Player player) {
        return CoinContainerItem.getId(getStack(player));
    }
}
