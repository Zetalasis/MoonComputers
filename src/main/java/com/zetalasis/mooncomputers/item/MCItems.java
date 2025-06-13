package com.zetalasis.mooncomputers.item;

import com.zetalasis.mooncomputers.MoonComputers;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class MCItems {
    public static Item FLOPPY_DISK = Registry.register(Registries.ITEM, MoonComputers.location("floppy_disk"),
            new Item(new Item.Settings()));

    public static void bootstrap()
    {

    }
}
