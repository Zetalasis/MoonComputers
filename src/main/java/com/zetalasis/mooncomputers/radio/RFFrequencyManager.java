package com.zetalasis.mooncomputers.radio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RFFrequencyManager {
    public static final Map<Integer, List<RFTransceiver>> listeners = new HashMap<>();
    public static final List<RFPacket> airwaves = new ArrayList<>();

    public static void tick() {
        for (RFPacket packet : airwaves) {
            List<RFTransceiver> freqListeners = listeners.get(packet.frequency);
            if (freqListeners != null) {
                for (RFTransceiver listener : freqListeners) {
                    listener.runnable.accept(packet);
                }
            }
        }
        airwaves.clear();
    }

    public static void register(int frequency, RFTransceiver transceiver) {
        listeners.computeIfAbsent(frequency, k -> new ArrayList<>()).add(transceiver);
    }

    public static void deregister(int frequency, RFTransceiver transceiver) {
        List<RFTransceiver> freqListeners = listeners.get(frequency);
        if (freqListeners != null) {
            freqListeners.remove(transceiver);
            if (freqListeners.isEmpty()) {
                listeners.remove(frequency);
            }
        }
    }
}