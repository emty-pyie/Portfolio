package com.axex.axe.network;

import com.axex.axe.AxeMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

public class ModNetwork {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = ChannelBuilder
        .named(new ResourceLocation(AxeMod.MODID, "main"))
        .networkProtocolVersion(PROTOCOL_VERSION)
        .clientAcceptedVersions(PROTOCOL_VERSION::equals)
        .serverAcceptedVersions(PROTOCOL_VERSION::equals)
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
