package com.zetalasis.mooncomputers.radio;

import net.minecraft.util.math.BlockPos;

import java.util.function.Consumer;

public class RFTransceiver {
    private int frequency;
    public Consumer<RFPacket> runnable;

    public RFTransceiver(int frequency) {
        this.frequency = frequency;
        RFFrequencyManager.register(frequency, this);
    }

    public void deregister() {
        RFFrequencyManager.deregister(this.frequency, this);
    }

    public void setFrequency(int frequency) {
        deregister();
        this.frequency = frequency;
        RFFrequencyManager.register(frequency, this);
    }

    public int getFrequency() {
        return frequency;
    }

    public void listen(Consumer<RFPacket> runnable) {
        this.runnable = runnable;
    }

    public void send(byte[] data) {
        RFPacket packet = new RFPacket();
        packet.frequency = frequency;
        packet.data = data;
        packet.source = new BlockPos(0, 0, 0);
        packet.power = 100;
        packet.sender = this;
        RFFrequencyManager.airwaves.add(packet);
    }
}