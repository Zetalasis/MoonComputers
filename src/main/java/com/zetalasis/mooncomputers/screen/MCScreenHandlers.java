package com.zetalasis.mooncomputers.screen;

import com.zetalasis.mooncomputers.MoonComputers;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;

public class MCScreenHandlers {
    public static final ScreenHandlerType<ComputerCaseScreenHandler> COMPUTER_CASE_SCREEN_HANDLER = Registry.register(Registries.SCREEN_HANDLER, MoonComputers.location("computer_case"),
            new ExtendedScreenHandlerType<>(ComputerCaseScreenHandler::new));
    public static final ScreenHandlerType<ScreenBlockScreenHandler> SCREEN_BLOCK_SCREEN_HANDLER = Registry.register(Registries.SCREEN_HANDLER, MoonComputers.location("screen_block"),
            new ExtendedScreenHandlerType<>(ScreenBlockScreenHandler::new));

    public static void bootstrap()
    {

    }
}
