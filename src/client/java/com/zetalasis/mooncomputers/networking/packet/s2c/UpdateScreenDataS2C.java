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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateScreenDataS2C {
    // Per-position tracking (in case multiple screens update concurrently)
    private static final Map<BlockPos, ScreenChunkBuffer> activeBuffers = new HashMap<>();

    public static void handle(MinecraftClient client, ClientPlayNetworkHandler handler,
                              PacketByteBuf buf, PacketSender responseSender) {
        BlockPos pos = buf.readBlockPos();
        int totalChunks = buf.readInt();
        int chunkIndex = buf.readInt();
        boolean hasText = buf.readBoolean();

        String rawText = hasText ? buf.readString() : null;
        int fullSize = buf.readInt();
        int chunkSize = buf.readInt();
        byte[] chunkData = new byte[chunkSize];
        buf.readBytes(chunkData);

        client.execute(() -> {
            if (client.world == null) return;

            activeBuffers.computeIfAbsent(pos, p -> new ScreenChunkBuffer(totalChunks, fullSize)).acceptChunk(chunkIndex, chunkData);

            if (hasText) {
                activeBuffers.get(pos).setText(rawText);
            }

            if (activeBuffers.get(pos).isComplete()) {
                ScreenChunkBuffer buffer = activeBuffers.remove(pos);
                applyUpdate(client, pos, buffer);
            }
        });
    }

    private static void applyUpdate(MinecraftClient client, BlockPos pos, ScreenChunkBuffer buffer) {
        BlockState state = client.world.getBlockState(pos);
        if (!state.isOf(MCBlocks.SCREEN)) return;

        ScreenBlockEntity entity = (ScreenBlockEntity) client.world.getBlockEntity(pos);
        if (entity == null) return;

        MoonComputers.LOGGER.info("[CLIENT] Screen at {} fully updated", pos);

        if (buffer.getRawText() != null) {
            List<String> lines = Arrays.stream(buffer.getRawText().split("\n")).toList();
            entity.setScreenLines(lines);
        }

        entity.vram = buffer.getData();
    }

    private static class ScreenChunkBuffer {
        private final byte[][] chunks;
        private final int expectedChunks;
        private final int totalSize;
        private String rawText;

        public ScreenChunkBuffer(int expectedChunks, int totalSize) {
            this.expectedChunks = expectedChunks;
            this.totalSize = totalSize;
            this.chunks = new byte[expectedChunks][];
        }

        public void acceptChunk(int index, byte[] data) {
            chunks[index] = data;
        }

        public boolean isComplete() {
            for (int i = 0; i < expectedChunks; i++) {
                if (chunks[i] == null) return false;
            }
            return true;
        }

        public byte[] getData() {
            ByteArrayOutputStream out = new ByteArrayOutputStream(totalSize);
            for (byte[] part : chunks) {
                try {
                    out.write(part);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return out.toByteArray();
        }

        public void setText(String text) {
            this.rawText = text;
        }

        public String getRawText() {
            return rawText;
        }
    }
}