package com.axex.axe.entity;

import com.axex.axe.registry.ModEntities;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public class ThrownAxeEntity extends Projectile {

    private final ItemStack axeStack;
    private final float damage;

    public ThrownAxeEntity(EntityType<? extends ThrownAxeEntity> type, Level level) {
        super(type, level);
        this.axeStack = ItemStack.EMPTY;
        this.damage = 0;
    }

    public ThrownAxeEntity(Level level, LivingEntity thrower, ItemStack stack, float damage) {
        super(ModEntities.THROWN_AXE.get(), level);
        this.setOwner(thrower);
        this.axeStack = stack.copy();
        this.damage = damage;
        this.setPos(thrower.getX(), thrower.getEyeY() - 0.1, thrower.getZ());
    }

    public static float computeDamage(LivingEntity thrower, ItemStack axeStack, float chargeScale) {
        float base = 6.0F;
        float scaled = base * (0.6F + chargeScale);

        int sharpness = 0;

        if (thrower.level() instanceof Level level) {
            var registry = level.registryAccess()
                    .registryOrThrow(Registries.ENCHANTMENT);

            var sharpHolder = registry.getHolder(Enchantments.SHARPNESS);

            if (sharpHolder.isPresent()) {
                sharpness = EnchantmentHelper.getItemEnchantmentLevel(
                        sharpHolder.get(),
                        axeStack
                );
            }
        }

        return scaled + (sharpness * 1.25F);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();

        this.setDeltaMovement(this.getDeltaMovement().scale(0.99));

        if (!this.level().isClientSide) {
            HitResult result = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
            if (result.getType() != HitResult.Type.MISS) {
                this.onHit(result);
            }
        }

        this.setPos(
                this.getX() + this.getDeltaMovement().x,
                this.getY() + this.getDeltaMovement().y,
                this.getZ() + this.getDeltaMovement().z
        );
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!this.level().isClientSide) {
            Entity target = result.getEntity();
            Entity owner = this.getOwner();

            DamageSource source = this.damageSources().thrown(this, owner);

            target.hurt(source, damage);

            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        if (!this.level().isClientSide) {
            this.discard();
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return entity != this.getOwner();
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public ItemStack getAxeStack() {
        return axeStack;
    }
}
