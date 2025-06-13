package com.zetalasis.mooncomputers.computer.device;

import com.zetalasis.mooncomputers.MoonComputers;
import com.zetalasis.mooncomputers.computer.VirtualizedComputer;
import com.zetalasis.mooncomputers.computer.memory.MemoryPage;

import java.awt.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.Arrays;

public class GraphicsCard implements IMemoryMappedIO {
    public static final int FRAMEBUFFER_PAGE_COUNT = 169;
    public final VirtualizedComputer computer;
    private final MemoryPage[] framebufferPages = new MemoryPage[FRAMEBUFFER_PAGE_COUNT];

    private byte[] framebuffer;
    private final int fbWidth  = 220;
    private final int fbHeight = 220;

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
        render(null);
        finish();
    }

    public void render(Runnable onPaint) {
        int totalPixels = fbWidth * fbHeight;
        int framebufferSize = 0;

        if (mode == 0)
        {
            return;
        }
        else if (mode == 1)
        {
            framebufferSize = 3 + totalPixels; // 1bpp
        }
        else if (mode == 2)
        {
            framebufferSize = 3 + totalPixels * 3; // 3bpp
        }

        framebuffer = new byte[framebufferSize];
        framebuffer[0] = (byte) mode;
        framebuffer[1] = (byte) fbWidth;
        framebuffer[2] = (byte) fbHeight;

        //fill(0, 0, fbWidth, fbHeight, 0xFFFFFF);

        if (onPaint != null)
        {
            onPaint.run();
        }

        MoonComputers.LOGGER.info("Created framebuffer!");
    }

    public void finish()
    {
        if (framebuffer == null)
            return;

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

    public void fill(int x1, int y1, int x2, int y2, int color) {

        if (mode != 2 || framebuffer == null) {
            MoonComputers.LOGGER.info("Attempted to fill but failed | mode {} | framebuffer {}", mode, framebuffer == null ? "null" : "OK");
            return;
        }

        if (x1 > x2) { int t = x1; x1 = x2; x2 = t; }
        if (y1 > y2) { int t = y1; y1 = y2; y2 = t; }

        x1 = Math.max(0, x1);   y1 = Math.max(0, y1);
        x2 = Math.min(fbWidth  - 1, x2);
        y2 = Math.min(fbHeight - 1, y2);

        if (x1 > x2 || y1 > y2) {
            return;
        }

        final int rectW  = x2 - x1 + 1;
        final int rowLen = rectW * 3;

        byte r = (byte) ((color >> 16) & 0xFF);
        byte g = (byte) ((color >> 8)  & 0xFF);
        byte b = (byte) ( color        & 0xFF);

        byte[] rowBuf = new byte[rowLen];
        rowBuf[0] = r; rowBuf[1] = g; rowBuf[2] = b;

        for (int filled = 3; filled < rowLen; ) {
            int copy = Math.min(filled, rowLen - filled);
            System.arraycopy(rowBuf, 0, rowBuf, filled, copy);
            filled += copy;
        }

        for (int y = y1; y <= y2; y++) {
            int dst = 3 + (y * fbWidth + x1) * 3;
            System.arraycopy(rowBuf, 0, framebuffer, dst, rowLen);
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
                    int texPixel = (y * width + x) * 3;

                    if (bmpPixel + 2 >= bmp.length) continue;

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