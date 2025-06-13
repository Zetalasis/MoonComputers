package com.zetalasis.mooncomputers.computer.device;

import com.zetalasis.mooncomputers.computer.VirtualizedComputer;
import com.zetalasis.mooncomputers.computer.memory.MemoryPage;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.Arrays;

public class GraphicsCard implements IMemoryMappedIO {
    private static final int FRAMEBUFFER_PAGE_COUNT = 4; // Use 4 pages (16KB total)
    private final VirtualizedComputer computer;
    private final MemoryPage[] framebufferPages = new MemoryPage[FRAMEBUFFER_PAGE_COUNT];

    public int mode = 0;

    public GraphicsCard(VirtualizedComputer host)
    {
        this.computer = host;
    }

    public void mapFramebufferPages(int startPage)
    {
        for (int i = 0; i < FRAMEBUFFER_PAGE_COUNT; i++) {
            framebufferPages[i] = computer.PAGE_TABLE.get(startPage + i);
        }
    }

    @Override
    public int getBaseAddress() {
        return framebufferPages[0].getBaseAddress(); // First page's base address
    }

    @Override
    public int getId() {
        return 0x0A0A;
    }

    @Override
    public String getHID() {
        return "Generic Graphics Card";
    }

    @Override
    public void deregister() {

    }

    public void render() {
        int width = 64;
        int height = 64;
        int totalPixels = width * height;
        int framebufferSize = 0;

        if (mode == 0)
        {
            return;
        }
        else if (mode == 1)
        {
            // add 3 for header info
            framebufferSize = 3 + totalPixels; // 1bpp (monochrome)
        }
        else if (mode == 2)
        {
            // add 3 for header info
            framebufferSize = 3 + totalPixels * 3; // 3 bpp (R, G, B)
        }

        byte[] framebuffer = new byte[framebufferSize];
        /* Mode:
         *  0: Text mode
         *  1: 128x128 Monochrome
         *  2: 64x64 Color */
        framebuffer[0] = (byte) mode;
        framebuffer[1] = (byte) width;
        framebuffer[2] = (byte) height;

        int remaining = framebuffer.length;
        int offset = 0;

        for (MemoryPage page : framebufferPages) {
            int toWrite = Math.min(4096, remaining);
            byte[] chunk = Arrays.copyOfRange(framebuffer, offset, offset + toWrite);
            page.write(chunk, 0);
            offset += toWrite;
            remaining -= toWrite;

            if (remaining <= 0) break;
        }
    }

    class BitmappedTexture {
        private final byte[] rgbPixels;
        public final int width;
        public final int height;

        public BitmappedTexture(String name, FileIO fileIO) {
            byte[] bmp = fileIO.read(fileIO.resolve(name));
            if (bmp == null || bmp.length < 54) {
                throw new IllegalArgumentException("Invalid BMP file: too short or null");
            }

            ByteBuffer buffer = ByteBuffer.wrap(bmp).order(ByteOrder.LITTLE_ENDIAN);
            if (buffer.get() != 'B' || buffer.get() != 'M') {
                throw new IllegalArgumentException("Invalid BMP signature");
            }

            buffer.position(10);
            int pixelArrayOffset = buffer.getInt();

            buffer.position(18);
            width = buffer.getInt();
            height = buffer.getInt();

            buffer.position(28);
            short bitsPerPixel = buffer.getShort();

            if (bitsPerPixel != 24) {
                throw new UnsupportedOperationException("Only 24-bit BMPs are supported.");
            }

            int rowSize = ((bitsPerPixel * width + 31) / 32) * 4;
            int dataSize = rowSize * height;

            if (pixelArrayOffset + dataSize > bmp.length) {
                throw new IllegalArgumentException("BMP file is truncated.");
            }

            rgbPixels = new byte[width * height * 3];

            for (int y = 0; y < height; y++) {
                int bmpRowStart = pixelArrayOffset + (height - 1 - y) * rowSize;
                for (int x = 0; x < width; x++) {
                    int bmpPixel = bmpRowStart + x * 3;
                    int texPixel = ((height - 1 - y) * width + x) * 3;

                    byte blue = bmp[bmpPixel];
                    byte green = bmp[bmpPixel + 1];
                    byte red = bmp[bmpPixel + 2];

                    rgbPixels[texPixel] = red;
                    rgbPixels[texPixel + 1] = green;
                    rgbPixels[texPixel + 2] = blue;
                }
            }
        }

        /**
         * Returns the raw RGB data (3 bytes per pixel)
         */
        public byte[] getRGBPixels() {
            return rgbPixels;
        }

        /**
         * Copies this texture into the framebuffer format used by the GraphicsCard at the given x/y offset
         */
        public void blitToFramebuffer(byte[] framebuffer, int fbWidth, int fbHeight, int destX, int destY) {
            for (int y = 0; y < height; y++) {
                if (y + destY >= fbHeight) break;

                for (int x = 0; x < width; x++) {
                    if (x + destX >= fbWidth) break;

                    int texIndex = (y * width + x) * 3;
                    int fbIndex = 3 + ((y + destY) * fbWidth + (x + destX)) * 3;

                    if (fbIndex + 2 >= framebuffer.length) continue;

                    framebuffer[fbIndex] = rgbPixels[texIndex];
                    framebuffer[fbIndex + 1] = rgbPixels[texIndex + 1];
                    framebuffer[fbIndex + 2] = rgbPixels[texIndex + 2];
                }
            }
        }
    }
}