package com.zetalasis.mooncomputers.networking;

import com.zetalasis.mooncomputers.MoonComputers;
import com.zetalasis.mooncomputers.networking.packet.c2s.ComputerInputEventC2S;
import com.zetalasis.mooncomputers.networking.packet.c2s.UpdateComputerStateC2S;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class MCPacketsS2C {
    public static void register()
    {
        ServerPlayNetworking.registerGlobalReceiver(MCMessages.UPDATE_COMPUTER_STATE_C2S, UpdateComputerStateC2S::handle);
        ServerPlayNetworking.registerGlobalReceiver(MCMessages.COMPUTER_INPUT_EVENT_C2S, ComputerInputEventC2S::handle);
    }

    public enum ComputerUpdateContext {
        TOGGLE_POWER(0);

        public final byte value;

        ComputerUpdateContext(int b)
        {
            this.value = (byte)b;
        }
    }

    public static void updateComputerState(ServerPlayerEntity sp, BlockPos pos)
    {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

        buf.writeBlockPos(pos);

        ServerPlayNetworking.send(sp, MCMessages.UPDATE_COMPUTER_STATE_S2C, buf);
    }

    public static void updateScreenState(ServerPlayerEntity sp, BlockPos pos, List<String> screenLines, byte[] vram)
    {
        if (vram == null) throw new IllegalStateException("VRAM is null!");

        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

        buf.writeBlockPos(pos);
        StringBuilder formatted = new StringBuilder();

        for (String line : screenLines)
        {
            formatted.append(line).append("\n");
        }

        buf.writeString(formatted.toString());
        buf.writeByteArray(vram);

        MoonComputers.LOGGER.info("Sending Screen Update Packet | Buffer Size: {}", buf.readableBytes());
        ServerPlayNetworking.send(sp, MCMessages.UPDATE_SCREEN_STATE_S2C, buf);
    }
}