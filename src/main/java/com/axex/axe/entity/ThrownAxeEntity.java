package com.axex.axe.entity;
import net.minecraftforge.network.NetworkHooks;

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
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public class ThrownAxeEntity extends Projectile {

    private static final EntityDataAccessor<ItemStack> AXE_STACK =
            SynchedEntityData.defineId(ThrownAxeEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Boolean> STUCK =
            SynchedEntityData.defineId(ThrownAxeEntity.class, EntityDataSerializers.BOOLEAN);

    private static final int MAX_LIFETIME_TICKS = 20 * 45;
    private static final double STUCK_OFFSET = 0.5D;

    private float baseDamage;
    private BlockPos stuckPos;
    private final ItemStack axeStack;
    private final float damage;

    public ThrownAxeEntity(EntityType<? extends ThrownAxeEntity> type, Level level) {
        super(type, level);
        this.axeStack = ItemStack.EMPTY;
        this.damage = 0;
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

        if (isStuck()) {
            setDeltaMovement(Vec3.ZERO);
            if (stuckPos != null) {
                setPos(stuckPos.getX() + STUCK_OFFSET, stuckPos.getY() + STUCK_OFFSET, stuckPos.getZ() + STUCK_OFFSET);
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

        if (state.is(BlockTags.LOGS)
                || state.is(BlockTags.PLANKS)
                || state.is(BlockTags.WOODEN_DOORS)
                || state.is(BlockTags.WOODEN_FENCES)
                || state.is(BlockTags.WOODEN_SLABS)
                || state.is(BlockTags.WOODEN_STAIRS)) {
            stickInBlock(result.getBlockPos(), result.getDirection());
            return;
        }
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
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        setAxeStack(ItemStack.parse(registryAccess(), tag.getCompound("Axe")).orElse(ItemStack.EMPTY));
        baseDamage = tag.getFloat("BaseDamage");
        setStuck(tag.getBoolean("Stuck"));

        if (tag.contains("StuckX")) {
            stuckPos = new BlockPos(tag.getInt("StuckX"), tag.getInt("StuckY"), tag.getInt("StuckZ"));
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        if (!this.level().isClientSide) {
            this.discard();
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
