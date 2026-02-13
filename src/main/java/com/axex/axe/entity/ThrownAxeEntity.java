package com.axex.axe.entity;

import com.axex.axe.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public class ThrownAxeEntity extends Projectile {

    private static final EntityDataAccessor<ItemStack> AXE_STACK =
            SynchedEntityData.defineId(ThrownAxeEntity.class, EntityDataSerializers.ITEM_STACK);

    private static final EntityDataAccessor<Boolean> STUCK =
            SynchedEntityData.defineId(ThrownAxeEntity.class, EntityDataSerializers.BOOLEAN);

    private float baseDamage;
    private BlockPos stuckPos;

    public ThrownAxeEntity(EntityType<? extends ThrownAxeEntity> type, Level level) {
        super(type, level);
    }

    public ThrownAxeEntity(Level level, LivingEntity thrower, ItemStack stack, float damage) {
        this(ModEntities.THROWN_AXE.get(), level);
        this.setOwner(thrower);
        this.setAxeStack(stack.copy());
        this.baseDamage = damage;
        this.setPos(thrower.getX(), thrower.getEyeY() - 0.1D, thrower.getZ());
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(AXE_STACK, ItemStack.EMPTY);
        this.entityData.define(STUCK, false);
    }

    @Override
    public void tick() {
        super.tick();

        if (isStuck()) {
            this.setDeltaMovement(Vec3.ZERO);
            if (stuckPos != null) {
                this.setPos(
                        stuckPos.getX() + 0.5D,
                        stuckPos.getY() + 0.5D,
                        stuckPos.getZ() + 0.5D
                );
            }
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return entity != this.getOwner() && !isStuck();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        Entity owner = this.getOwner();

        DamageSource source = this.damageSources().thrown(this, owner);
        target.hurt(source, this.baseDamage);

        if (!this.level().isClientSide) {
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        BlockState state = this.level().getBlockState(result.getBlockPos());

        if (isWood(state)) {
            stick(result.getBlockPos(), result.getDirection());
        } else {
            if (!this.level().isClientSide) {
                this.spawnAtLocation(getAxeStack());
                this.discard();
            }
        }
    }

    private boolean isWood(BlockState state) {
        return state.is(BlockTags.LOGS)
                || state.is(BlockTags.PLANKS)
                || state.is(BlockTags.WOODEN_DOORS)
                || state.is(BlockTags.WOODEN_FENCES)
                || state.is(BlockTags.WOODEN_SLABS)
                || state.is(BlockTags.WOODEN_STAIRS);
    }

    private void stick(BlockPos pos, Direction face) {
        if (!this.level().isClientSide) {
            this.setStuck(true);
            this.stuckPos = pos.relative(face);
            this.setNoGravity(true);
            this.setDeltaMovement(Vec3.ZERO);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("Axe", getAxeStack().save(this.registryAccess()));
        tag.putFloat("Damage", this.baseDamage);
        tag.putBoolean("Stuck", isStuck());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setAxeStack(ItemStack.parse(this.registryAccess(), tag.getCompound("Axe")).orElse(ItemStack.EMPTY));
        this.baseDamage = tag.getFloat("Damage");
        this.setStuck(tag.getBoolean("Stuck"));
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!this.level().isClientSide && isStuck() && player.getMainHandItem().isEmpty()) {
            player.setItemInHand(hand, getAxeStack().copy());
            this.discard();
            return InteractionResult.SUCCESS;
        }
        return super.interact(player, hand);
    }

    public static float computeDamage(LivingEntity thrower, float chargeScale) {
        float base = (float) thrower.getAttributeValue(Attributes.ATTACK_DAMAGE);
        return base * (0.7F + 0.8F * chargeScale);
    }

    public ItemStack getAxeStack() {
        return this.entityData.get(AXE_STACK);
    }

    public void setAxeStack(ItemStack stack) {
        this.entityData.set(AXE_STACK, stack);
    }

    public boolean isStuck() {
        return this.entityData.get(STUCK);
    }

    public void setStuck(boolean stuck) {
        this.entityData.set(STUCK, stuck);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
