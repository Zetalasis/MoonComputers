package com.zetalasis.mooncomputers.networking;

import com.zetalasis.mooncomputers.networking.packet.s2c.UpdateComputerStateS2C;
import com.zetalasis.mooncomputers.networking.packet.s2c.UpdateScreenDataS2C;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;

public class MCPacketsC2S {
    public static void register()
    {
        ClientPlayNetworking.registerGlobalReceiver(MCMessages.UPDATE_COMPUTER_STATE_S2C, UpdateComputerStateS2C::handle);
        ClientPlayNetworking.registerGlobalReceiver(MCMessages.UPDATE_SCREEN_STATE_S2C, UpdateScreenDataS2C::handle);
    }

    public static void updateComputerState(BlockPos pos, MCPacketsS2C.ComputerUpdateContext context)
    {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

        buf.writeBlockPos(pos);
        buf.writeByte(context.value);

        ClientPlayNetworking.send(MCMessages.UPDATE_COMPUTER_STATE_C2S, buf);
    }

    public static void computerInputEvent(BlockPos pos, String input)
    {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

        buf.writeBlockPos(pos);
        buf.writeString(input);

        ClientPlayNetworking.send(MCMessages.COMPUTER_INPUT_EVENT_C2S, buf);
    }
}