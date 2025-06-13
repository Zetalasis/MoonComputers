package com.zetalasis.mooncomputers.networking.packet.c2s;

import com.zetalasis.mooncomputers.MoonComputers;
import com.zetalasis.mooncomputers.block.ComputerCase;
import com.zetalasis.mooncomputers.block.MCBlocks;
import com.zetalasis.mooncomputers.block.entity.ComputerCaseEntity;
import com.zetalasis.mooncomputers.networking.MCPacketsS2C;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public class UpdateComputerStateC2S {
    public static void handle(MinecraftServer server, ServerPlayerEntity player,
                              ServerPlayNetworkHandler handler,
                              PacketByteBuf buf, PacketSender responseSender) {
        BlockPos pos = buf.readBlockPos();
        byte context = buf.readByte();

        server.execute(() -> {
            BlockState state = player.getWorld().getBlockState(pos);

            if (state.isOf(MCBlocks.COMPUTER_CASE)) {
                MoonComputers.LOGGER.info("[SERVER] Updating computer state for computer at \"{}\"", pos);

                ComputerCaseEntity entity = (ComputerCaseEntity) player.getWorld().getBlockEntity(pos);
                assert entity != null;

                boolean powered = state.get(ComputerCase.POWERED);

                if (context == MCPacketsS2C.ComputerUpdateContext.TOGGLE_POWER.value)
                    powered = !powered;

                entity.update(player.getWorld(), pos, state, powered);
            }
        });
    }
}