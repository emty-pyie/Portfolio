package com.axex.axe.client;

import com.axex.axe.entity.ThrownAxeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;

public class ThrownAxeRenderer extends EntityRenderer<ThrownAxeEntity> {
    public ThrownAxeRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(ThrownAxeEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(entity.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(entity.getXRot()));
        if (!entity.isStuck()) {
            float spin = (entity.tickCount + partialTick) * 35.0F;
            poseStack.mulPose(Axis.ZP.rotationDegrees(spin));
        }

        this.entityRenderDispatcher.getItemInHandRenderer().renderItem(
                entity.getOwner() instanceof net.minecraft.world.entity.LivingEntity living ? living : null,
                entity.getAxeStack(),
                ItemDisplayContext.GROUND,
                false,
                poseStack,
                bufferSource,
                entity.level(),
                packedLight,
                OverlayTexture.NO_OVERLAY,
                0
        );
        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(ThrownAxeEntity entity) {
        return net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS;
    }
}
