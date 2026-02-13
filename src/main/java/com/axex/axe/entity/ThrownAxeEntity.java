package com.axex.axe.entity;

import com.axex.axe.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class ThrownAxeEntity extends ThrowableProjectile {
    private static final EntityDataAccessor<ItemStack> AXE_STACK = SynchedEntityData.defineId(ThrownAxeEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Boolean> STUCK = SynchedEntityData.defineId(ThrownAxeEntity.class, EntityDataSerializers.BOOLEAN);

    private float baseDamage;
    private BlockPos stuckPos;

    public ThrownAxeEntity(EntityType<? extends ThrownAxeEntity> type, Level level) {
        super(type, level);
    }

    public ThrownAxeEntity(Level level, LivingEntity owner, ItemStack stack, float damage) {
        this(ModEntities.THROWN_AXE.get(), level);
        this.setOwner(owner);
        this.setAxeStack(stack.copy());
        this.baseDamage = damage;
        this.setPos(owner.getX(), owner.getEyeY() - 0.1D, owner.getZ());
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
            setDeltaMovement(Vec3.ZERO);
            if (stuckPos != null) {
                this.setPos(stuckPos.getX() + 0.5D, stuckPos.getY() + 0.5D, stuckPos.getZ() + 0.5D);
            }
        }
        if (!this.level().isClientSide && this.tickCount > 20 * 45) {
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity hit = result.getEntity();
        Entity owner = this.getOwner();

        DamageSource source = this.damageSources().trident(this, owner == null ? this : owner);
        hit.hurt(source, this.baseDamage);

        if (!this.level().isClientSide) {
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        BlockState state = this.level().getBlockState(result.getBlockPos());
        if (state.is(BlockTags.createOptional(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("axe", "axe_stickable")))
                || state.is(BlockTags.LOGS)
                || state.is(BlockTags.PLANKS)
                || state.is(BlockTags.WOODEN_STAIRS)
                || state.is(BlockTags.WOODEN_SLABS)
                || state.is(BlockTags.WOODEN_FENCES)
                || state.is(BlockTags.WOODEN_DOORS)) {
            stickInBlock(result.getBlockPos(), result.getDirection());
            return;
        }

        if (!this.level().isClientSide) {
            this.spawnAtLocation(getAxeStack());
            this.discard();
        }
    }

    private void stickInBlock(BlockPos pos, Direction face) {
        if (!this.level().isClientSide) {
            this.setStuck(true);
            this.stuckPos = pos.relative(face);
            this.setNoGravity(true);
            this.setDeltaMovement(Vec3.ZERO);
            this.hasImpulse = false;
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("Axe", getAxeStack().save(this.registryAccess()));
        tag.putFloat("BaseDamage", this.baseDamage);
        tag.putBoolean("Stuck", isStuck());
        if (stuckPos != null) {
            tag.putInt("StuckX", stuckPos.getX());
            tag.putInt("StuckY", stuckPos.getY());
            tag.putInt("StuckZ", stuckPos.getZ());
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setAxeStack(ItemStack.parse(this.registryAccess(), tag.getCompound("Axe")).orElse(ItemStack.EMPTY));
        this.baseDamage = tag.getFloat("BaseDamage");
        this.setStuck(tag.getBoolean("Stuck"));
        if (tag.contains("StuckX") && tag.contains("StuckY") && tag.contains("StuckZ")) {
            this.stuckPos = new BlockPos(tag.getInt("StuckX"), tag.getInt("StuckY"), tag.getInt("StuckZ"));
        }
    }

    @Override
    protected float getGravity() {
        return this.isStuck() ? 0.0F : 0.05F;
    }

    @Override
    protected ItemStack getDefaultItem() {
        return getAxeStack();
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!this.level().isClientSide && this.isStuck() && hand == InteractionHand.MAIN_HAND && player.getMainHandItem().isEmpty()) {
            player.setItemInHand(InteractionHand.MAIN_HAND, getAxeStack().copy());
            this.discard();
            return InteractionResult.CONSUME;
        }
        return super.interact(player, hand);
    }

    public static float computeDamage(LivingEntity thrower, ItemStack axeStack, float chargeScale) {
        float base = (float) thrower.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
        int sharpness = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.SHARPNESS, axeStack);
        float enchantBonus = sharpness > 0 ? 0.5F * sharpness + 0.5F : 0.0F;
        return (base + enchantBonus) * (0.7F + 0.8F * chargeScale);
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
}
