package com.axex.axe.registry;

import com.axex.axe.AxeMod;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AxeMod.MOD_ID);

    public static final RegistryObject<Item> THROWABLE_AXE = ITEMS.register("throwable_axe",
            () -> new Item(new Item.Properties().stacksTo(1)));

    private ModItems() {
    }
}
