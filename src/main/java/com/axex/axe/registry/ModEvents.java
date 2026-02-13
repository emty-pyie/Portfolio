package com.axex.axe.registry;

import com.axex.axe.AxeMod;
import com.axex.axe.client.ThrownAxeRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public final class ModEvents {
    private ModEvents() {
    }

    public static void register(IEventBus modBus) {
        // hook point for future registration
    }

    @Mod.EventBusSubscriber(modid = AxeMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntities.THROWN_AXE.get(), ThrownAxeRenderer::new);
        }
    }
}
