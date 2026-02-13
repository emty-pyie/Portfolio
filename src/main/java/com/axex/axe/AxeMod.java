package com.axex.axe;

import com.axex.axe.network.ModNetwork;
import com.axex.axe.registry.ModEntities;
import com.axex.axe.registry.ModEvents;
import com.axex.axe.registry.ModItems;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AxeMod.MOD_ID)
public class AxeMod {
    public static final String MOD_ID = "axe";

    public AxeMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(modBus);
        ModEntities.ENTITIES.register(modBus);
        ModEvents.register(modBus);
        ModNetwork.register();

        MinecraftForge.EVENT_BUS.register(this);
    }
}
