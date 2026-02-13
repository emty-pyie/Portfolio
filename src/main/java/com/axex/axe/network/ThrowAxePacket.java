package com.axex.axe.network;

import com.axex.axe.entity.ThrownAxeEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.network.CustomPayloadEvent;

public record ThrowAxePacket(int chargeTicks) {

    private static final int MAX_CHARGE_TICKS = 20;
    private static final int THROW_COOLDOWN_TICKS = 15;

    public static void encode(ThrowAxePacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.chargeTicks);
    }

    public static ThrowAxePacket decode(FriendlyByteBuf buffer) {
        return new ThrowAxePacket(buffer.readVarInt());
    }

    public static void handle(ThrowAxePacket packet, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || player.isSpectator()) {
                return;
            }

            ItemStack held = player.getMainHandItem();
            if (held.isEmpty() || !(held.getItem() instanceof AxeItem)) {
                return;
            }

            int clampedCharge = Mth.clamp(packet.chargeTicks, 0, MAX_CHARGE_TICKS);
            float chargeScale = clampedCharge / (float) MAX_CHARGE_TICKS;
            if (chargeScale < 0.1F) {
                return;
            }

            ThrownAxeEntity thrownAxe = new ThrownAxeEntity(
                    player.level(),
                    player,
                    held,
                    ThrownAxeEntity.computeDamage(player, held, chargeScale)
            );

            float velocity = 1.6F + (chargeScale * 1.4F);
            thrownAxe.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, velocity, 1.0F);

            player.level().addFreshEntity(thrownAxe);
            player.level().playSound(
                    null,
                    player.blockPosition(),
                    SoundEvents.TRIDENT_THROW.value(),
                    SoundSource.PLAYERS,
                    1.0F,
                    0.95F + player.getRandom().nextFloat() * 0.1F
            );

            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            player.getCooldowns().addCooldown(held.getItem(), THROW_COOLDOWN_TICKS);
        });

        context.setPacketHandled(true);
    }
}
