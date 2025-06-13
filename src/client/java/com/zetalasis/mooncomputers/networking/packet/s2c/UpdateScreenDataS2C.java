package com.zetalasis.mooncomputers.networking.packet.s2c;

import com.zetalasis.mooncomputers.MoonComputers;
import com.zetalasis.mooncomputers.block.MCBlocks;
import com.zetalasis.mooncomputers.block.entity.ScreenBlockEntity;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.List;

public class UpdateScreenDataS2C {
    public static void handle(MinecraftClient client, ClientPlayNetworkHandler handler,
                              PacketByteBuf buf, PacketSender responseSender)
    {
        MoonComputers.LOGGER.info("[CLIENT] HELLO");
        BlockPos pos = buf.readBlockPos();
        String rawText = buf.readString();
        byte[] vram = buf.readByteArray(4096*4);

        client.execute(()->{
            if (client.world == null)
                return;

            BlockState state = client.world.getBlockState(pos);
            assert state.isOf(MCBlocks.SCREEN);

            ScreenBlockEntity entity = (ScreenBlockEntity) client.world.getBlockEntity(pos);
            assert entity != null;

            MoonComputers.LOGGER.info("[CLIENT] Updating screen state for screen at \"{}\"", pos);
            List<String> lines = Arrays.stream(rawText.split("\n")).toList();

            entity.setScreenLines(lines);
            entity.vram = vram;
        });
    }
}
