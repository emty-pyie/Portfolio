package com.axex.axe.network;

import com.axex.axe.AxeMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.SimpleChannel;

public class ModNetwork {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = ChannelBuilder
            .named(ResourceLocation.fromNamespaceAndPath(AxeMod.MODID, "main"))
            .networkProtocolVersion(PROTOCOL_VERSION)
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
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
