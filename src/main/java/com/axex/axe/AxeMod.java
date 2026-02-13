package com.axex.axe;

import com.axex.axe.network.ModNetwork;
import com.axex.axe.registry.ModEntities;
import com.axex.axe.registry.ModItems;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AxeMod.MODID)
public class AxeMod {

    public static final String MODID = "axe_or_ex";

    public AxeMod(FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();

        ModItems.ITEMS.register(modBus);
        ModEntities.ENTITIES.register(modBus);

        ModNetwork.register();

        MinecraftForge.EVENT_BUS.register(this);
    }
}
