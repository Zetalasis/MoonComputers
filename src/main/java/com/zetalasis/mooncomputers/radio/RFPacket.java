package com.zetalasis.mooncomputers.radio;

import net.minecraft.util.math.BlockPos;

public class RFPacket {
    public int frequency;
    public byte[] data;
    public BlockPos source;
    public int power;
    public RFTransceiver sender;
}
