package com.zetalasis.mooncomputers.computer.device.lua;

import com.zetalasis.mooncomputers.MoonComputers;
import com.zetalasis.mooncomputers.computer.device.NetworkIO;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;

import java.nio.charset.StandardCharsets;

public class LuaNetworkIO extends LuaTable {
    private final NetworkIO network;

    public LuaNetworkIO(NetworkIO network) {
        this.network = network;

        set("set_freq", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                if (!arg.isint()) return LuaValue.NIL;
                network.setFrequency(arg.toint());
                return LuaValue.NIL;
            }
        });

        set("send", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                if (!arg.isstring()) return LuaValue.NIL;
                byte[] data = arg.checkstring().m_bytes;
                network.send(data);
                return LuaValue.NIL;
            }
        });

        set("listen", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaFunc) {
                if (!luaFunc.isfunction()) return LuaValue.NIL;

                network.listen(packet -> {
                    LuaValue func = luaFunc.checkfunction();
                    LuaTable pkt = new LuaTable();
                    pkt.set("data", LuaValue.valueOf(new String(packet.data, StandardCharsets.UTF_8)));
                    pkt.set("power", LuaValue.valueOf(packet.power));
                    pkt.set("sender_id", LuaValue.valueOf(packet.sender.hashCode()));

                    try {
                        func.call(pkt);
                    } catch (LuaError e) {
                        MoonComputers.LOGGER.error("Lua error in RF listener: {}", e.getMessage());
                    }
                });

                return LuaValue.NIL;
            }
        });
    }
}