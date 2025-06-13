package com.zetalasis.mooncomputers.computer.device;

import com.zetalasis.mooncomputers.computer.VirtualizedComputer;
import com.zetalasis.mooncomputers.radio.RFPacket;
import com.zetalasis.mooncomputers.radio.RFTransceiver;

import java.util.function.Consumer;

public class NetworkIO implements IMemoryMappedIO {
    private final VirtualizedComputer computer;
    private final RFTransceiver transceiver;

    public NetworkIO(VirtualizedComputer host)
    {
        this.computer = host;
        this.transceiver = new RFTransceiver(500);
    }

    @Override
    public int getBaseAddress() {
        /** 0x0000 - 0x4000 reserved for VRAM {@link GraphicsCard}
         * 0x5000 reserved for {@link FileIO} */
        return 0x6000;
    }

    @Override
    public int getId() {
        return 0x3A8F;
    }

    @Override
    public String getHID() {
        return "Generic Network Card";
    }

    @Override
    public void deregister() {
        transceiver.deregister();
    }

    public void setFrequency(int frequency)
    {
        this.transceiver.setFrequency(frequency);
    }

    public void send(byte[] data)
    {
        this.transceiver.send(data);
    }

    public void listen(Consumer<RFPacket> runnable)
    {
        this.transceiver.listen(runnable);
    }

    /** Did this transceiver send this packet? */
    public boolean owns(RFPacket packet)
    {
        return (packet.sender == transceiver);
    }
}