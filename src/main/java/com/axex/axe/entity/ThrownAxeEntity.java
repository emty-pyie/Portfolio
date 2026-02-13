package com.axex.axe.entity;

import com.axex.axe.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class ThrownAxeEntity extends ThrowableProjectile {

    private static final EntityDataAccessor<ItemStack> AXE_STACK =
            SynchedEntityData.defineId(ThrownAxeEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Boolean> STUCK =
            SynchedEntityData.defineId(ThrownAxeEntity.class, EntityDataSerializers.BOOLEAN);

    private static final int MAX_LIFETIME_TICKS = 20 * 45;
    private static final double STUCK_OFFSET = 0.5D;

    private float baseDamage;
    private BlockPos stuckPos;

    public ThrownAxeEntity(EntityType<? extends ThrownAxeEntity> type, Level level) {
        super(type, level);
    }

    public ThrownAxeEntity(Level level, LivingEntity owner, ItemStack stack, float damage) {
        this(ModEntities.THROWN_AXE.get(), level);
        setOwner(owner);
        setAxeStack(stack.copy());
        baseDamage = damage;
        setPos(owner.getX(), owner.getEyeY() - 0.1D, owner.getZ());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(AXE_STACK, ItemStack.EMPTY);
        builder.define(STUCK, false);
    }

    @Override
    public void tick() {
        super.tick();

        if (isStuck()) {
            setDeltaMovement(Vec3.ZERO);
            if (stuckPos != null) {
                setPos(
                        stuckPos.getX() + STUCK_OFFSET,
                        stuckPos.getY() + STUCK_OFFSET,
                        stuckPos.getZ() + STUCK_OFFSET
                );
            }
        }

        if (!level().isClientSide && tickCount > MAX_LIFETIME_TICKS) {
            discard();
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (isStuck()) {
            return false;
        }

        Entity owner = getOwner();
        return super.canHitEntity(entity) && entity != owner;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        Entity hit = result.getEntity();
        Entity owner = getOwner();
        DamageSource source = damageSources().trident(this, owner == null ? this : owner);

        hit.hurt(source, baseDamage);

        if (!level().isClientSide) {
            discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        BlockState state = level().getBlockState(result.getBlockPos());

        if (isWoodLike(state)) {
            stickInBlock(result.getBlockPos(), result.getDirection());
            return;
        }

        if (!level().isClientSide) {
            spawnAtLocation(getAxeStack());
            discard();
        }
    }

    private static boolean isWoodLike(BlockState state) {
        return state.is(BlockTags.LOGS)
                || state.is(BlockTags.PLANKS)
                || state.is(BlockTags.WOODEN_DOORS)
                || state.is(BlockTags.WOODEN_FENCES)
                || state.is(BlockTags.WOODEN_SLABS)
                || state.is(BlockTags.WOODEN_STAIRS);
    }

    private void stickInBlock(BlockPos pos, Direction face) {
        if (level().isClientSide) {
            return;
        }

        setStuck(true);
        stuckPos = pos.relative(face);
        setNoGravity(true);
        setDeltaMovement(Vec3.ZERO);
        hasImpulse = false;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        tag.put("Axe", getAxeStack().save(registryAccess()));
        tag.putFloat("BaseDamage", baseDamage);
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

        setAxeStack(ItemStack.parse(registryAccess(), tag.getCompound("Axe")).orElse(ItemStack.EMPTY));
        baseDamage = tag.getFloat("BaseDamage");
        setStuck(tag.getBoolean("Stuck"));

        if (tag.contains("StuckX")) {
            stuckPos = new BlockPos(tag.getInt("StuckX"), tag.getInt("StuckY"), tag.getInt("StuckZ"));
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!level().isClientSide
                && isStuck()
                && hand == InteractionHand.MAIN_HAND
                && player.getMainHandItem().isEmpty()) {
            player.setItemInHand(hand, getAxeStack().copy());
            discard();
            return InteractionResult.SUCCESS;
        }

        return super.interact(player, hand);
    }

    public static float computeDamage(LivingEntity thrower, ItemStack axeStack, float chargeScale) {
        float base = (float) thrower.getAttributeValue(Attributes.ATTACK_DAMAGE);
        Holder.Reference<Enchantment> sharpnessEnchantment = thrower.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.SHARPNESS);
        int sharpness = EnchantmentHelper.getItemEnchantmentLevel(sharpnessEnchantment, axeStack);
        float enchantBonus = sharpness > 0 ? 0.5F * sharpness + 0.5F : 0.0F;

        return (base + enchantBonus) * (0.7F + 0.8F * chargeScale);
    }

    public ItemStack getAxeStack() {
        return entityData.get(AXE_STACK);
    }

    public void setAxeStack(ItemStack stack) {
        entityData.set(AXE_STACK, stack);
    }

    public boolean isStuck() {
        return entityData.get(STUCK);
    }

    public void setStuck(boolean stuck) {
        entityData.set(STUCK, stuck);
    }
}
