package com.zetalasis.mooncomputers.screen;

import com.zetalasis.mooncomputers.block.entity.ScreenBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;

public class ScreenBlockScreenHandler extends ScreenHandler {
    private final ScreenBlockEntity screenEntity;

    public ScreenBlockScreenHandler(int syncId, PlayerInventory inv, PacketByteBuf buf) {
        this(syncId, inv, (ScreenBlockEntity) inv.player.getWorld().getBlockEntity(buf.readBlockPos()));
    }

    public ScreenBlockScreenHandler(int syncId, PlayerInventory inv, ScreenBlockEntity screenEntity) {
        super(MCScreenHandlers.SCREEN_BLOCK_SCREEN_HANDLER, syncId);
        this.screenEntity = screenEntity;
    }

    public ScreenBlockEntity getScreenEntity() {
        return screenEntity;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return null;
    }
}