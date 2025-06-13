package com.zetalasis.mooncomputers.radio;

import net.minecraft.util.math.BlockPos;

import java.util.function.Consumer;

public class RFTransceiver {
    private int frequency;
    public Consumer<RFPacket> runnable;

    public RFTransceiver(int frequency)
    {
        this.frequency = frequency;
        RFFrequencyManager.listeners.put(this.frequency, this);
    }

    /** Remove this listener from {@link RFFrequencyManager#listeners}
     * Call {@link RFTransceiver#setFrequency(int)} to link again */
    public void deregister()
    {
        RFFrequencyManager.listeners.remove(this.frequency, this);
    }

    public void setFrequency(int frequency)
    {
        deregister();
        this.frequency = frequency;
        RFFrequencyManager.listeners.put(frequency, this);
    }

    public int getFrequency()
    {
        return this.frequency;
    }

    public void listen(Consumer<RFPacket> runnable)
    {
        this.runnable = runnable;
    }

    public void send(byte[] data)
    {
        RFPacket packet = new RFPacket();
        packet.frequency = this.frequency;
        packet.data = data;
        packet.source = new BlockPos(0, 0, 0);
        packet.power = 100;
        packet.sender = this;
        RFFrequencyManager.airwaves.add(packet);
    }
}