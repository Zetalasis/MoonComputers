package com.zetalasis.mooncomputers.computer.device.lua;

import com.zetalasis.mooncomputers.computer.device.FileIO;
import org.luaj.vm2.LuaTable;

public class LuaFileIO extends LuaTable {
    private final FileIO fileIO;

    public LuaFileIO(FileIO fileIO)
    {
        this.fileIO = fileIO;
    }
}