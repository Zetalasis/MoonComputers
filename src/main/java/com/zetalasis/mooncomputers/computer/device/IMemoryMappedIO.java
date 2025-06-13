package com.zetalasis.mooncomputers.computer.device;

public interface IMemoryMappedIO {
    int getBaseAddress();

    /** Individual device-tree ID */
    int getId();
    /** Human-readable display name */
    String getHID();
    /** De-initialize everything */
    void deregister();
}
