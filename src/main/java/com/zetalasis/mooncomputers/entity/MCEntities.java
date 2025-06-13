package com.zetalasis.mooncomputers.entity;

import com.zetalasis.mooncomputers.MoonComputers;
import com.zetalasis.mooncomputers.block.MCBlocks;
import com.zetalasis.mooncomputers.block.entity.ComputerCaseEntity;
import com.zetalasis.mooncomputers.block.entity.ScreenBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class MCEntities {
    public static final BlockEntityType<ComputerCaseEntity> COMPUTER_CASE_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, MoonComputers.location("computer_case"),
            FabricBlockEntityTypeBuilder.create(ComputerCaseEntity::new, MCBlocks.COMPUTER_CASE).build());
    public static final BlockEntityType<ScreenBlockEntity> SCREEN_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, MoonComputers.location("screen"),
            FabricBlockEntityTypeBuilder.create(ScreenBlockEntity::new, MCBlocks.SCREEN).build());

    public static void bootstrap()
    {

    }
}