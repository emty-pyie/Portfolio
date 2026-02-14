package com.axex.axe.network;

import com.axex.axe.AxeMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.SimpleChannel;

public class ModNetwork {

    private static final int PROTOCOL_VERSION = 1;

    public static final SimpleChannel CHANNEL = ChannelBuilder
            .named(ResourceLocation.tryBuild(AxeMod.MODID, "main"))
            .networkProtocolVersion(PROTOCOL_VERSION)
            .clientAcceptedVersions((status, version) -> true)
            .serverAcceptedVersions((status, version) -> true)
            .simpleChannel();

    public static void register() {
        int id = 0;

        CHANNEL.messageBuilder(ThrowAxePacket.class, id++)
                .encoder(ThrowAxePacket::encode)
                .decoder(ThrowAxePacket::decode)
                .consumerMainThread(ThrowAxePacket::handle)
                .add();
    }
}
