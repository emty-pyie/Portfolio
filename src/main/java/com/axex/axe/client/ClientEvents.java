package com.axex.axe.client;

import com.axex.axe.AxeMod;
import com.axex.axe.network.ModNetwork;
import com.axex.axe.network.ThrowAxePacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = AxeMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ClientEvents {
    private static final String CATEGORY = "key.categories.axe";
    public static final KeyMapping THROW_AXE_KEY = new KeyMapping("key.axe.throw", GLFW.GLFW_KEY_R, CATEGORY);

    private ClientEvents() {
    }

    @SubscribeEvent
    public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        event.register(THROW_AXE_KEY);
    }

    @Mod.EventBusSubscriber(modid = AxeMod.MOD_ID, value = Dist.CLIENT)
    public static class Runtime {
        private static boolean charging;
        private static int chargeTicks;

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) {
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.screen != null) {
                reset();
                return;
            }

            if (THROW_AXE_KEY.isDown()) {
                charging = true;
                chargeTicks = Math.min(chargeTicks + 1, 20);
                if (mc.player.isUsingItem()) {
                    mc.player.stopUsingItem();
                }
                return;
            }

            if (charging) {
                ModNetwork.CHANNEL.sendToServer(new ThrowAxePacket(chargeTicks));
            }
            reset();
        }

        private static void reset() {
            charging = false;
            chargeTicks = 0;
        }
    }
}
