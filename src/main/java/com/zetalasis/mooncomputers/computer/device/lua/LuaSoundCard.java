package com.zetalasis.mooncomputers.computer.device.lua;

import com.zetalasis.mooncomputers.computer.device.SoundCard;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;

public class LuaSoundCard extends LuaTable {
    public LuaSoundCard(SoundCard card)
    {
        set("play", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue) {
                card.play(luaValue.tojstring());

                return LuaValue.NIL;
            }
        });
    }
}