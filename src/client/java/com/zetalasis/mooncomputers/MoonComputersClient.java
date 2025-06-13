package com.zetalasis.mooncomputers;

import com.zetalasis.mooncomputers.entity.MCEntities;
import com.zetalasis.mooncomputers.entity.ScreenBlockRenderer;
import com.zetalasis.mooncomputers.networking.MCPacketsC2S;
import com.zetalasis.mooncomputers.screen.ComputerCaseScreen;
import com.zetalasis.mooncomputers.screen.MCScreenHandlers;
import com.zetalasis.mooncomputers.screen.ScreenBlockScreen;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public class MoonComputersClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MCPacketsC2S.register();

		HandledScreens.register(MCScreenHandlers.COMPUTER_CASE_SCREEN_HANDLER, ComputerCaseScreen::new);
		HandledScreens.register(MCScreenHandlers.SCREEN_BLOCK_SCREEN_HANDLER, ScreenBlockScreen::new);

		BlockEntityRendererFactories.register(MCEntities.SCREEN_ENTITY, ScreenBlockRenderer::new);
	}
}