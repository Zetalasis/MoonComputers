package com.zetalasis.mooncomputers.computer;

import com.zetalasis.mooncomputers.MoonComputers;
import com.zetalasis.mooncomputers.block.entity.ComputerCaseEntity;
import com.zetalasis.mooncomputers.computer.device.FileIO;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class LuaMethods {
    public static void bootstrap(Globals globals, ComputerCaseEntity computer)
    {
        globals.set("scr_clear", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                if (computer.screen == null)
                {
                    MoonComputers.LOGGER.info("Attempted to clear screen but screen was nil!");
                    return LuaValue.NIL;
                }

                computer.screen.clear();
                return LuaValue.NIL;
            }
        });

        globals.set("scr_get_line", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue) {
                if (!luaValue.isint())
                    return LuaValue.NIL;

                if (computer.screen == null)
                    return LuaValue.NIL;

                List<String> lines = computer.screen.getScreenLines();
                if (lines.isEmpty())
                    return LuaValue.valueOf("");

                int lineNumber = luaValue.toint();
                if (lineNumber == -1)
                    return LuaValue.valueOf(lines.get(lines.size() - 1));
                else {
                    if (lineNumber <= 0 || lineNumber > lines.size())
                        return LuaValue.NIL;

                    return LuaValue.valueOf(lines.get(lineNumber - 1));
                }
            }
        });

        globals.set("scr_edit_line", new VarArgFunction() {
            @Override
            public LuaValue call(LuaValue index, LuaValue str) {
                if (!index.isint() || !str.isstring())
                    return LuaValue.NIL;

                if (computer.screen == null)
                    return LuaValue.NIL;

                int lineNumber = index.toint();
                if (lineNumber == -1)
                    lineNumber = computer.screen.getScreenLines().size();

                String lineContent = str.tojstring();

                if (lineNumber <= 0 || lineNumber > computer.screen.getScreenLines().size())
                    return LuaValue.NIL;

                computer.screen.getScreenLines().set(lineNumber - 1, lineContent);
                computer.screen.flush();
                return LuaValue.NIL;
            }
        });

        globals.set("readfile", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue) {
                return LuaValue.NIL;
            }
        });

        globals.set("executefile", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue) {
                if (!luaValue.isstring())
                {
                    error("Passed value was not a string");
                    return LuaValue.NIL;
                }

                String filename = luaValue.tojstring();

                FileIO fileIO = computer.computer.getDevice(FileIO.class);
                byte[] rawShellCode = fileIO.read(fileIO.resolve(filename));
                if (rawShellCode == null) {
                    error("FileNotFound error while attempting to load \"" + filename + "\"");
                    return LuaValue.NIL;
                }

                computer.computer.loadScript(new String(rawShellCode, StandardCharsets.UTF_8));

                return LuaValue.NIL;
            }
        });
    }
}
