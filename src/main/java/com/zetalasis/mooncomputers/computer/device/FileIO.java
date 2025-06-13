package com.zetalasis.mooncomputers.computer.device;

import com.zetalasis.mooncomputers.MoonComputers;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileIO implements IMemoryMappedIO {
    @Override
    public int getBaseAddress() {
        /* 0x0000 - 0x4000 is reserved for VRAM */
        return 0x5000;
    }

    @Override
    public int getId() {
        return 0x70BA;
    }

    @Override
    public String getHID() {
        return "File IO";
    }

    @Override
    public void deregister() {

    }

    public Path resolve(String name)
    {
        return FabricLoader.getInstance().getGameDir().resolve("mooncomputers/" + name);
    }

    public byte @Nullable [] read(Path path) {
        if (Files.exists(path) && Files.isRegularFile(path)) {
            try {
                return Files.readAllBytes(path);
            } catch (IOException e) {
                MoonComputers.LOGGER.info("Failed to read file!\n{}", e.toString());
                e.printStackTrace();
            }
        }
        return null;
    }
}
