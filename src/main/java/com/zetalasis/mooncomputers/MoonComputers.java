package com.zetalasis.mooncomputers;

import com.zetalasis.mooncomputers.block.MCBlocks;
import com.zetalasis.mooncomputers.entity.MCEntities;
import com.zetalasis.mooncomputers.item.MCItems;
import com.zetalasis.mooncomputers.networking.MCPacketsS2C;
import com.zetalasis.mooncomputers.radio.RFFrequencyManager;
import com.zetalasis.mooncomputers.screen.MCScreenHandlers;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoonComputers implements ModInitializer {
	public static final String MOD_ID = "moon-computers";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Identifier location(String name)
	{
		return new Identifier(MOD_ID, name);
	}

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		MCItems.bootstrap();
		MCBlocks.bootstrap();
		MCEntities.bootstrap();
		MCScreenHandlers.bootstrap();

		MCPacketsS2C.register();

		LOGGER.info("Hello Fabric world!");

		ServerTickEvents.END_WORLD_TICK.register((world) -> {
			if (world == world.getServer().getOverworld())
			{
				RFFrequencyManager.tick();
			}
		});
	}
}