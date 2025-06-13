package com.zetalasis.mooncomputers.computer.device.lua;

import com.zetalasis.mooncomputers.MoonComputers;
import com.zetalasis.mooncomputers.computer.device.GraphicsCard;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

public class LuaGraphicsIO extends LuaTable {
    private final GraphicsCard graphicsCard;
    private final Runnable onPaint = this::onRender;

    public LuaGraphicsIO(GraphicsCard graphicsCard)
    {
        this.graphicsCard = graphicsCard;

        set("mode", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue) {
                if (!luaValue.isint())
                    throw new LuaError("Graphics.mode was called yet was passed a non-number argument!");

                int mode = luaValue.toint();
                if (mode < 0 || mode > 2)
                    throw new LuaError("Graphics.mode IndexOutOfRange");

                graphicsCard.mode = mode;
                return LuaValue.NIL;
            }
        });

        set("render", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                graphicsCard.render(onPaint);

                return LuaValue.NIL;
            }
        });

        set("fill", new VarArgFunction() {
            @Override
            /* Lua args look like: 0, 0, 220, 220, 0xFFFFFF */
            public Varargs invoke(Varargs varargs) {
                // TODO: should check the type of these and error if they aren't correct
                int x1 = varargs.arg(1).toint();
                int y1 = varargs.arg(2).toint();
                int x2 = varargs.arg(3).toint();
                int y2 = varargs.arg(4).toint();
                int color = varargs.arg(5).toint();

                graphicsCard.fill(x1, y1, x2, y2, color);

                return LuaValue.NIL;
            }
        });

        set("finish", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                if (graphicsCard.computer.computerBlock.screen == null)
                    throw new LuaError("Attempted to fill while \"screen\" is null!");

                graphicsCard.finish();
                graphicsCard.computer.computerBlock.screen.flush();

                return LuaValue.NIL;
            }
        });
    }

    /** Calls the `paint` method in the loaded Lua script.
     * Graphics calls are required to be made in the context of this `paint` method. */
    public void onRender()
    {
        LuaValue paintMethod = graphicsCard.computer.getLuaMethod("paint");
        if (paintMethod != null)
        {
            paintMethod.call();
        }
    }
}