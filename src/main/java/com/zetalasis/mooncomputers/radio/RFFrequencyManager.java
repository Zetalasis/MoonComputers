package com.zetalasis.mooncomputers.radio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RFFrequencyManager {
    public static HashMap<Integer, RFTransceiver> listeners = new HashMap<>();
    public static List<RFPacket> airwaves = new ArrayList<>();

    public static void tick()
    {
        for (RFPacket packet : airwaves)
        {
            for (RFTransceiver listener : listeners.values())
            {
                if (listener.getFrequency() == packet.frequency)
                {
                    listener.runnable.accept(packet);
                }
            }
        }
        airwaves.clear();
    }
}