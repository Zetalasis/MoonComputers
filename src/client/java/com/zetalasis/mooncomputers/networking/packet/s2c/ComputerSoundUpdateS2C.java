package com.zetalasis.mooncomputers.networking.packet.s2c;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class ComputerSoundUpdateS2C {
    private static final Map<BlockPos, ChunkedAudioBuffer> activeAudioBuffers = new HashMap<>();

    public static void handle(MinecraftClient client, ClientPlayNetworkHandler handler,
                              PacketByteBuf buf, PacketSender responseSender) {
        BlockPos pos = buf.readBlockPos();
        int totalChunks = buf.readInt();
        int chunkIndex = buf.readInt();
        int fullSize = buf.readInt();
        int sampleRate = buf.readInt();
        /* Formats:
        *   AL10.AL_FORMAT_MONO8
        *   AL10.AL_FORMAT_MONO16
        *   AL10.AL_FORMAT_STEREO8
        *   AL10.AL_FORMAT_STEREO16*/
        int format = buf.readInt();

        int chunkLength = buf.readInt();
        byte[] chunkData = new byte[chunkLength];
        buf.readBytes(chunkData);

        client.execute(() -> {
            if (client.world == null) return;

            activeAudioBuffers.computeIfAbsent(pos, p ->
                    new ChunkedAudioBuffer(totalChunks, fullSize, sampleRate, format)
            ).acceptChunk(chunkIndex, chunkData);

            ChunkedAudioBuffer buffer = activeAudioBuffers.get(pos);
            if (buffer.isComplete()) {
                byte[] fullAudio = buffer.assemble();
                playPCM(fullAudio, buffer.getSampleRate(), buffer.getFormat());
                activeAudioBuffers.remove(pos);
            }
        });
    }

    private static void playPCM(byte[] data, int sampleRate, int format) {
        int buffer = AL10.alGenBuffers();
        int source = AL10.alGenSources();

        ByteBuffer pcm = BufferUtils.createByteBuffer(data.length);
        pcm.put(data).flip();

        AL10.alBufferData(buffer, format, pcm, sampleRate);
        AL10.alSourcei(source, AL10.AL_BUFFER, buffer);
        AL10.alSourcePlay(source);
    }
}