package com.axex.axe.item;

import com.axex.axe.entity.ThrownAxeEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;

public class ThrowAxeItem extends AxeItem {

    private static final int MAX_CHARGE = 20;

    public ThrowAxeItem(Tier tier, float attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, Player player, int timeLeft) {

        if (level.isClientSide) return;

        int charge = this.getUseDuration(stack) - timeLeft;
        float power = Mth.clamp(charge / (float) MAX_CHARGE, 0F, 1F);

        if (power < 0.2F) return;

        ThrownAxeEntity axe = new ThrownAxeEntity(level, player);

        axe.setItem(stack.copy());
        axe.setBaseDamage(12F + (power * 8F)); // heavy damage scaling

        axe.shootFromRotation(
                player,
                player.getXRot(),
                player.getYRot(),
                0.0F,
                1.5F + (power * 1.5F),
                1.0F
        );

        level.addFreshEntity(axe);

        level.playSound(null,
                player.blockPosition(),
                SoundEvents.TRIDENT_THROW,
                SoundSource.PLAYERS,
                1.0F,
                1.0F);

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }
}
