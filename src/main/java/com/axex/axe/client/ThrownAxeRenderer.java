package com.axex.axe.client;

import com.axex.axe.entity.ThrownAxeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;

public class ThrownAxeRenderer extends EntityRenderer<ThrownAxeEntity> {

    private final ItemRenderer itemRenderer;

    public ThrownAxeRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(ThrownAxeEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

        poseStack.pushPose();

        float yaw = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
        float pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());

        poseStack.mulPose(Axis.YP.rotationDegrees(yaw - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(pitch));

        float spin = (entity.tickCount + partialTick) * 35.0F;
        poseStack.mulPose(Axis.XP.rotationDegrees(spin));

        LivingEntity owner = entity.getOwner() instanceof LivingEntity living ? living : null;

        itemRenderer.renderStatic(
                owner,
                entity.getAxeStack(),
                ItemDisplayContext.GROUND,
                false,
                poseStack,
                bufferSource,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                entity.getId()
        );

        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(ThrownAxeEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
