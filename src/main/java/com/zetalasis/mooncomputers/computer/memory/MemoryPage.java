package com.zetalasis.mooncomputers.computer.memory;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class MemoryPage {
    private final byte[] data = new byte[4096];
    private final int baseAddress;
    private final int flags;

    public MemoryPage(int baseAddress, int flags)
    {
        this.baseAddress = baseAddress;
        this.flags = flags;
    }

    public boolean write(byte[] data, int baseAddress)
    {
        if (!hasPermission(Flags.WRITE))
            return false;

        if (baseAddress < 0 || baseAddress + data.length > this.data.length)
            return false;

        System.arraycopy(data, 0, this.data, baseAddress, data.length);

        return true;
    }

    public byte @Nullable [] read(int address, int length) {
        if ((flags & Flags.READ.value) == 0)
            return null;

        int localAddress = address - baseAddress;
        if (localAddress < 0 || localAddress + length > data.length)
            return null;

        return Arrays.copyOfRange(data, localAddress, localAddress + length);
    }

    public int getBaseAddress()
    {
        return this.baseAddress;
    }

    public boolean hasPermission(Flags flag)
    {
        return ((this.flags & flag.value) != 0);
    }

    public enum Flags {
        READ(1),
        WRITE(2),
        EXECUTE(4);

        public final int value;

        Flags(int value)
        {
            this.value = value;
        }
    }
}