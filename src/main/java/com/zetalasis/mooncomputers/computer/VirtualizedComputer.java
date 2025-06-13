package com.zetalasis.mooncomputers.computer;

import com.zetalasis.mooncomputers.MoonComputers;
import com.zetalasis.mooncomputers.block.entity.ComputerCaseEntity;
import com.zetalasis.mooncomputers.computer.device.*;
import com.zetalasis.mooncomputers.computer.device.lua.LuaGraphicsIO;
import com.zetalasis.mooncomputers.computer.device.lua.LuaNetworkIO;
import com.zetalasis.mooncomputers.computer.memory.MemoryPage;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.util.HashMap;
import java.util.function.Consumer;

public class VirtualizedComputer {
    public final byte[] MEMORY_SPACE;
    public final HashMap<Integer, MemoryPage> PAGE_TABLE = new HashMap<>();
    public final HashMap<MemoryPage, IMemoryMappedIO> DEVICE_TREE = new HashMap<>();
    public final ComputerCaseEntity computerBlock;

    public final Globals luaGlobals;
    private LuaValue luaTickFunction;
    private LuaValue onInputFunction;
    public LuaValue paintFunction;
    public final Consumer<String> printMethod;

    public VirtualizedComputer(ComputerCaseEntity block, int pages, Consumer<String> printMethod)
    {
        this.computerBlock = block;
        MEMORY_SPACE = new byte[4096 * pages];
        this.printMethod = printMethod;
        this.luaGlobals = JsePlatform.standardGlobals();

        for (int page = 0; page < pages; page++)
        {
            PAGE_TABLE.put(
                    page,
                    new MemoryPage(
                        page * 4096,
                        MemoryPage.Flags.READ.value | MemoryPage.Flags.WRITE.value | MemoryPage.Flags.EXECUTE.value
            ));
        }

        NetworkIO networkIO = new NetworkIO(this);
        DEVICE_TREE.put(PAGE_TABLE.get(6), networkIO);

        FileIO fileIO = new FileIO();
        DEVICE_TREE.put(PAGE_TABLE.get(5), fileIO);

        GraphicsCard graphicsCard = new GraphicsCard(this);
        graphicsCard.mapFramebufferPages(0);
        DEVICE_TREE.put(PAGE_TABLE.get(0), graphicsCard);
        graphicsCard.render();

        MoonComputers.LOGGER.info("Loaded computer | {} Pages | {}kb Hardware Memory | Device Tree: ", pages, pages*4096);
        for (IMemoryMappedIO device : DEVICE_TREE.values())
        {
            MoonComputers.LOGGER.info("{} | at: 0x{} | ID: 0x{}", device.getHID(), Integer.toHexString(device.getBaseAddress()), Integer.toHexString(device.getId()));
        }

        luaGlobals.set("print", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i <= args.narg(); i++) {
                    sb.append(args.arg(i).tojstring()).append(" ");
                }
                print(sb.toString());

                return LuaValue.NIL;
            }
        });

        luaGlobals.set("warn", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i <= args.narg(); i++) {
                    sb.append(args.arg(i).tojstring()).append(" ");
                }
                warn(sb.toString());

                return LuaValue.NIL;
            }
        });

        luaGlobals.set("error", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i <= args.narg(); i++) {
                    sb.append(args.arg(i).tojstring()).append(" ");
                }
                error(sb.toString());

                return LuaValue.NIL;
            }
        });

        luaGlobals.set("net", new LuaNetworkIO(networkIO));
        luaGlobals.set("graphics", new LuaGraphicsIO(graphicsCard));
    }

    @SuppressWarnings("unchecked")
    public <T extends IMemoryMappedIO> T getDevice(Class<T> deviceClass)
    {
        for (IMemoryMappedIO device : DEVICE_TREE.values())
        {
            if (device.getClass().equals(deviceClass))
            {
                return (T) device;
            }
        }

        return null;
    }

    /** Deregister all devices in the device tree and prepare the computer to be removed */
    public void shutdown()
    {
        for (IMemoryMappedIO device : DEVICE_TREE.values())
        {
            device.deregister();
        }
    }

    public LuaValue getLuaMethod(String name)
    {
        LuaValue method = luaGlobals.get(name);
        if (!method.isnil() && method.isfunction())
        {
            MoonComputers.LOGGER.info("Found method \"{}\"", name);
            return method;
        }

        MoonComputers.LOGGER.info("Failed to find method \"{}\"", name);
        return null;
    }

    public void loadScript(String script) {
        try {
            LuaValue chunk = luaGlobals.load(script, "user_script");
            chunk.call();
            luaTickFunction = getLuaMethod("tick");
            onInputFunction = getLuaMethod("onInput");
            paintFunction = getLuaMethod("paint");
        } catch (LuaError e) {
            MoonComputers.LOGGER.error("Lua error: {}", e.getMessage());
            printMethod.accept("§cError caught while running script:\n" + e.getMessage() + "§r");
        }
    }

    public void tick() {
        if (luaTickFunction != null) {
            try {
                luaTickFunction.call();
            } catch (LuaError e) {
                MoonComputers.LOGGER.error("Lua tick() error: {}", e.getMessage());
                printMethod.accept("§cError caught in tick:\n" + e.getMessage() + "§r");
            }
        }
    }

    public void handleInput(String inputStr)
    {
        if (onInputFunction != null) {
            try {
                onInputFunction.call(inputStr);
            } catch (LuaError e) {
                MoonComputers.LOGGER.error("Lua onInput() error: {}", e.getMessage());
                printMethod.accept("§cError caught in onInput:\n" + e.getMessage() + "§r");
            }
        }
    }

    public void print(String s)
    {
        printMethod.accept(s.trim());
    }

    public void warn(String s)
    {
        printMethod.accept("§6" + s.trim() + "§r");
    }

    public void error(String s)
    {
        printMethod.accept("§c" + s.trim() + "§r");
    }
}