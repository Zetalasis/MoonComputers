package com.zetalasis.mooncomputers.networking.packet.s2c;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ChunkedAudioBuffer {
    private final byte[][] chunks;
    private final int expectedChunks;
    private final int totalSize;
    private int sampleRate;
    private int format;

    public ChunkedAudioBuffer(int expectedChunks, int totalSize, int sampleRate, int format) {
        this.expectedChunks = expectedChunks;
        this.totalSize = totalSize;
        this.chunks = new byte[expectedChunks][];
        this.sampleRate = sampleRate;
        this.format = format;
    }

    public void acceptChunk(int index, byte[] data) {
        chunks[index] = data;
    }

    public boolean isComplete() {
        for (byte[] chunk : chunks)
            if (chunk == null) return false;
        return true;
    }

    public byte[] assemble() {
        ByteArrayOutputStream out = new ByteArrayOutputStream(totalSize);
        for (byte[] chunk : chunks) {
            try {
                out.write(chunk);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return out.toByteArray();
    }

    public int getSampleRate() { return sampleRate; }
    public int getFormat() { return format; }
}