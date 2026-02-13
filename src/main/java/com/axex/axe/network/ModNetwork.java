package com.axex.axe.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.SimpleChannel;

public class ModNetwork {

    public static final SimpleChannel CHANNEL = ChannelBuilder
            .named(new ResourceLocation("axe_or_ex", "main"))
            .networkProtocolVersion(1)
            .clientAcceptedVersions(version -> true)
            .serverAcceptedVersions(version -> true)
            .simpleChannel();

    private static int packetId = 0;

    public static void register() {

        CHANNEL.messageBuilder(ThrowAxePacket.class, packetId++)
                .encoder(ThrowAxePacket::encode)
                .decoder(ThrowAxePacket::decode)
                .consumerMainThread((packet, context) -> {
                    var player = context.getSender();
                    if (player != null) {
                        ThrowAxePacket.handle(packet, player);
                    }
                })
                .add();
    }
}
