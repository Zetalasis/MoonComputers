package com.zetalasis.mooncomputers.computer.device.lua;

import com.zetalasis.mooncomputers.MoonComputers;
import com.zetalasis.mooncomputers.computer.device.FileIO;
import com.zetalasis.mooncomputers.computer.device.GraphicsCard;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

import java.util.HashMap;

public class LuaGraphicsIO extends LuaTable {
    private final GraphicsCard graphicsCard;
    private final Runnable onPaint = this::onRender;
    private final HashMap<String, GraphicsCard.BitmappedTexture> bitmapCache = new HashMap<>();

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

        set("renderBMP", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs varargs) {
                String bmpName = varargs.arg1().tojstring();

                int x = varargs.arg(2).toint();
                int y = varargs.arg(3).toint();

//                int width = varargs.arg(4).toint();
//                int height = varargs.arg(5).toint();

                if (!bitmapCache.containsKey(bmpName))
                    throw new LuaError("bitmapCache does not contain \"" + bmpName + "\"");

                GraphicsCard.BitmappedTexture texture = bitmapCache.get(bmpName);

                texture.blitToFramebuffer(graphicsCard.framebuffer, graphicsCard.fbWidth, graphicsCard.fbHeight, x, y);

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

        set("loadBMP", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue luaValue) {
                String name = luaValue.tojstring();

                try {
                    GraphicsCard.BitmappedTexture texture = new GraphicsCard.BitmappedTexture(name, graphicsCard.computer.getDevice(FileIO.class));

                    bitmapCache.put(name, texture);
                    return LuaValue.NIL;
                }
                catch (Exception e)
                {
                    throw new LuaError("Failed to load BMP: \"" + e + "\"");
                }
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