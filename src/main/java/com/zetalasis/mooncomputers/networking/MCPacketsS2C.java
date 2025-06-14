package com.zetalasis.mooncomputers.networking;

import com.zetalasis.mooncomputers.MoonComputers;
import com.zetalasis.mooncomputers.networking.packet.c2s.ComputerInputEventC2S;
import com.zetalasis.mooncomputers.networking.packet.c2s.UpdateComputerStateC2S;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.openal.AL10;

import java.util.Arrays;
import java.util.List;

public class MCPacketsS2C {
    public static final int MAX_CHUNK_SIZE = 16 * 1024;

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

    public static void updateScreenState(ServerPlayerEntity sp, BlockPos pos, List<String> screenLines, byte[] vram) {
        if (vram == null) throw new IllegalStateException("VRAM is null!");

        StringBuilder formatted = new StringBuilder();
        for (String line : screenLines) {
            formatted.append(line).append("\n");
        }

        String text = formatted.toString();
        int totalChunks = (int) Math.ceil((double) vram.length / MAX_CHUNK_SIZE);

        for (int chunkIndex = 0; chunkIndex < totalChunks; chunkIndex++) {
            int start = chunkIndex * MAX_CHUNK_SIZE;
            int end = Math.min(vram.length, (chunkIndex + 1) * MAX_CHUNK_SIZE);
            byte[] chunk = Arrays.copyOfRange(vram, start, end);

            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeBlockPos(pos);
            buf.writeInt(totalChunks);
            buf.writeInt(chunkIndex);

            if (chunkIndex == 0) {
                buf.writeBoolean(true);
                buf.writeString(text);
            } else {
                buf.writeBoolean(false);
            }

            buf.writeInt(vram.length);
            buf.writeInt(chunk.length);
            buf.writeBytes(chunk);

            ServerPlayNetworking.send(sp, MCMessages.UPDATE_SCREEN_STATE_S2C, buf);
        }

        MoonComputers.LOGGER.info("Sent screen update in {} chunks, total size: {} bytes", totalChunks, vram.length);
    }

    public static void soundEvent(ServerPlayerEntity sp, BlockPos pos, int sampleRate , byte[] data)
    {
        int totalChunks = (int) Math.ceil((double) data.length / MAX_CHUNK_SIZE);

        for (int chunkIndex = 0; chunkIndex < totalChunks; chunkIndex++)
        {
            int start = chunkIndex * MAX_CHUNK_SIZE;
            int end = Math.min(data.length, (chunkIndex + 1) * MAX_CHUNK_SIZE);
            byte[] chunk = Arrays.copyOfRange(data, start, end);

            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

            buf.writeBlockPos(pos);
            buf.writeInt(totalChunks);
            buf.writeInt(chunkIndex);
            buf.writeInt(data.length);
            buf.writeInt(sampleRate);
            buf.writeInt(AL10.AL_FORMAT_STEREO8);

            buf.writeInt(chunk.length);
            buf.writeBytes(chunk);

            ServerPlayNetworking.send(sp, MCMessages.COMPUTER_SOUND_UPDATE_S2C, buf);
        }
    }
}