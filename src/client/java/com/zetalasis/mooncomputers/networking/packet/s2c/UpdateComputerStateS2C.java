package com.zetalasis.mooncomputers.networking.packet.s2c;

import com.zetalasis.mooncomputers.MoonComputers;
import com.zetalasis.mooncomputers.block.entity.ComputerCaseEntity;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;

public class UpdateComputerStateS2C {
    public static void handle(MinecraftClient client, ClientPlayNetworkHandler handler,
                              PacketByteBuf buf, PacketSender responseSender)
    {
        if (client.world == null)
            return;

        BlockPos pos = buf.readBlockPos();
        if (client.world.getBlockEntity(pos) instanceof ComputerCaseEntity computer)
        {
            MoonComputers.LOGGER.info("[CLIENT] Updating computer state for computer at \"{}\"", pos);
        }
    }
}
