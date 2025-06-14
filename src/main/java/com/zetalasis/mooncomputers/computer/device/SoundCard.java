package com.zetalasis.mooncomputers.computer.device;

import com.zetalasis.mooncomputers.MoonComputers;
import com.zetalasis.mooncomputers.computer.VirtualizedComputer;
import com.zetalasis.mooncomputers.networking.MCPacketsS2C;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import org.lwjgl.openal.AL10;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class SoundCard implements IMemoryMappedIO {
    private final VirtualizedComputer computer;

    public SoundCard(VirtualizedComputer host)
    {
        this.computer = host;
    }

    @Override
    public int getBaseAddress() {
        /** 0x0000 - 0x4000 reserved for VRAM {@link GraphicsCard}
         * 0x5000 reserved for {@link FileIO}
         * 0x6000 reserved for {@link NetworkIO} */
        return 0x7000;
    }

    @Override
    public int getId() {
        return 0xCCA7;
    }

    @Override
    public String getHID() {
        return "Generic Sound Card";
    }

    @Override
    public void deregister() {

    }

    public void play(String path) {
        FileIO fileIO = computer.getDevice(FileIO.class);
        byte[] fileContent = fileIO.read(fileIO.resolve(path));

        if (fileContent == null) {
            computer.error("Audio file not found: " + path);
            return;
        }

        WavData wav;
        try {
            wav = parse(fileContent);
        } catch (IOException e) {
            computer.error("Failed to parse WAV file: " + e.getMessage());
            return;
        }

        int chunkSize = 8192;
        int totalChunks = (int) Math.ceil(wav.pcmData.length / (double) chunkSize);

//        for (PlayerEntity sp : computer.computerBlock.getWorld().getPlayers()) {
//            for (int i = 0; i < totalChunks; i++) {
//                int start = i * chunkSize;
//                int end = Math.min(wav.pcmData.length, start + chunkSize);
//                byte[] chunk = Arrays.copyOfRange(wav.pcmData, start, end);
//
//                PacketByteBuf buf = PacketByteBufs.create();
//                buf.writeBlockPos(computer.computerBlock.getPos());
//                buf.writeInt(totalChunks);
//                buf.writeInt(i);
//                buf.writeInt(wav.pcmData.length);
//                buf.writeInt(wav.sampleRate);
//                buf.writeInt(wav.format);
//                buf.writeInt(chunk.length);
//                buf.writeBytes(chunk);
//
//                ServerPlayNetworking.send((ServerPlayerEntity) sp, MCPacketsS2C.SEND_AUDIO_CHUNK, buf);
//            }
//        }

        for (PlayerEntity sp : computer.computerBlock.getWorld().getPlayers())
            MCPacketsS2C.soundEvent((ServerPlayerEntity) sp, computer.computerBlock.getPos(), wav.sampleRate, wav.pcmData);
    }

    public WavData parse(byte[] wavFile) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(wavFile).order(ByteOrder.LITTLE_ENDIAN);

        if (!(buffer.get() == 'R' && buffer.get() == 'I' && buffer.get() == 'F' && buffer.get() == 'F')) {
            throw new IllegalArgumentException("Invalid WAV: Missing RIFF");
        }

        buffer.getInt();

        if (!(buffer.get() == 'W' && buffer.get() == 'A' && buffer.get() == 'V' && buffer.get() == 'E')) {
            throw new IOException("Invalid WAV: Missing WAVE");
        }

        boolean foundFmt = false;
        int audioFormat = 0;
        int numChannels = 0;
        int sampleRate = 0;
        int bitsPerSample = 0;

        while (buffer.remaining() >= 8) {
            byte[] chunkId = new byte[4];
            buffer.get(chunkId);
            int chunkSize = buffer.getInt();

            String chunkName = new String(chunkId, StandardCharsets.US_ASCII);
            if (chunkName.equals("fmt ")) {
                foundFmt = true;
                if (chunkSize < 16) throw new IOException("Invalid fmt chunk size");

                audioFormat = buffer.getShort();
                numChannels = buffer.getShort();
                sampleRate = buffer.getInt();
                buffer.getInt();
                buffer.getShort();
                bitsPerSample = buffer.getShort();

                if (chunkSize > 16) {
                    buffer.position(buffer.position() + (chunkSize - 16));
                }
                break;
            } else {
                buffer.position(buffer.position() + chunkSize);
            }
        }

        if (!foundFmt) throw new IOException("Missing fmt chunk");

        int format;
        if (numChannels == 1 && bitsPerSample == 8) format = AL10.AL_FORMAT_MONO8;
        else if (numChannels == 1 && bitsPerSample == 16) format = AL10.AL_FORMAT_MONO16;
        else if (numChannels == 2 && bitsPerSample == 8) format = AL10.AL_FORMAT_STEREO8;
        else if (numChannels == 2 && bitsPerSample == 16) format = AL10.AL_FORMAT_STEREO16;
        else throw new IOException("Unsupported format: " + numChannels + "ch, " + bitsPerSample + "bit");

        byte[] pcmData = null;
        while (buffer.remaining() >= 8) {
            byte[] chunkId = new byte[4];
            buffer.get(chunkId);
            int chunkSize = buffer.getInt();

            String chunkName = new String(chunkId, StandardCharsets.US_ASCII);
            if (chunkName.equals("data")) {
                pcmData = new byte[chunkSize];
                buffer.get(pcmData);
                break;
            } else {
                buffer.position(buffer.position() + chunkSize);
            }
        }

        if (pcmData == null) throw new IOException("Missing data chunk");

        return new WavData(sampleRate, format, pcmData);
    }


    public class WavData {
        public final int sampleRate;
        /* Formats:
         *   AL10.AL_FORMAT_MONO8
         *   AL10.AL_FORMAT_MONO16
         *   AL10.AL_FORMAT_STEREO8
         *   AL10.AL_FORMAT_STEREO16*/
        public final int format;
        public final byte[] pcmData;

        public WavData(int sampleRate, int format, byte[] pcmData) {
            this.sampleRate = sampleRate;
            this.format = format;
            this.pcmData = pcmData;
        }
    }
}