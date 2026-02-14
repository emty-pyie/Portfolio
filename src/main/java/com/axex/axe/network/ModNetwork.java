package com.axex.axe.network;

import com.axex.axe.AxeMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.SimpleChannel;

public final class ModNetwork {

    private static final int PROTOCOL_VERSION = 1;

    public static SimpleChannel CHANNEL;

    private ModNetwork() {}

    public static void register() {
        CHANNEL = ChannelBuilder
                .named(ResourceLocation.fromNamespaceAndPath(AxeMod.MODID, "main"))
                .networkProtocolVersion(PROTOCOL_VERSION)
                .clientAcceptedVersions((status, version) -> true)
                .serverAcceptedVersions((status, version) -> true)
                .simpleChannel();

        int id = 0;

        CHANNEL.messageBuilder(ThrowAxePacket.class, id++)
                .encoder(ThrowAxePacket::encode)
                .decoder(ThrowAxePacket::decode)
                .consumerMainThread(ThrowAxePacket::handle)
                .add();
    }
}
