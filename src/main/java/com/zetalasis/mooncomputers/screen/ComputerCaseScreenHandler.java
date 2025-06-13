package com.zetalasis.mooncomputers.screen;

import com.zetalasis.mooncomputers.block.entity.ComputerCaseEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;

public class ComputerCaseScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    private final ComputerCaseEntity blockEntity;
    public BlockPos pos;

    public ComputerCaseScreenHandler(int syncId, PlayerInventory inventory, PacketByteBuf buf)
    {
        this(syncId, inventory, inventory.player.getWorld().getBlockEntity(buf.readBlockPos()));
    }

    public ComputerCaseScreenHandler(int syncId, PlayerInventory inventory, BlockEntity blockEntity) {
        super(MCScreenHandlers.COMPUTER_CASE_SCREEN_HANDLER, syncId);
        checkSize((Inventory) blockEntity, 1);
        this.inventory = ((Inventory) blockEntity);
        this.blockEntity = (ComputerCaseEntity) blockEntity;
        this.pos = blockEntity.getPos();

        this.addSlot(new Slot(inventory, 0, 79, 33));

        addPlayerInventory(inventory);
        addPlayerHotbar(inventory);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            if (invSlot < this.inventory.size()) {
                if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(originalStack, 0, this.inventory.size(), false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return newStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    private void addPlayerInventory(PlayerInventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(PlayerInventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    public ComputerCaseEntity getEntity()
    {
        return this.blockEntity;
    }
}