package com.zetalasis.mooncomputers.block;

import com.zetalasis.mooncomputers.MoonComputers;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class MCBlocks {
    public static final Block COMPUTER_CASE = registerBlock("computer_case", new ComputerCase(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK)));
    public static final Block SCREEN = registerBlock("screen", new ScreenBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK)));

    private static Block registerBlock(String name, Block block)
    {
        Block temp = Registry.register(Registries.BLOCK, MoonComputers.location(name),
                block);

        Registry.register(Registries.ITEM, MoonComputers.location(name),
                new BlockItem(block, new Item.Settings()));

        return temp;
    }

    public static void bootstrap()
    {

    }
}
