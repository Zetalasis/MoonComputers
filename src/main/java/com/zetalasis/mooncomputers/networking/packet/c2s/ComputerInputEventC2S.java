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

public class ComputerInputEventC2S {
    public static void handle(MinecraftServer server, ServerPlayerEntity player,
                              ServerPlayNetworkHandler handler,
                              PacketByteBuf buf, PacketSender responseSender)
    {
        BlockPos pos = buf.readBlockPos();
        String inputChar = buf.readString();

        server.execute(() -> {
            BlockState state = player.getWorld().getBlockState(pos);

            if (state.isOf(MCBlocks.COMPUTER_CASE)) {
                ComputerCaseEntity entity = (ComputerCaseEntity) player.getWorld().getBlockEntity(pos);
                assert entity != null;

                entity.handleInput(player.getWorld(), pos, state, inputChar);
            }
        });
    }
}