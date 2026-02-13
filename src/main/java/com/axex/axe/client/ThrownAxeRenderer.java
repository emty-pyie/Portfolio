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

        poseStack.mulPose(Axis.YP.rotationDegrees(entity.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(entity.getXRot()));

        if (!entity.isStuck()) {
            float spin = (entity.tickCount + partialTick) * 35.0F;
            poseStack.mulPose(Axis.ZP.rotationDegrees(spin));
        }

        LivingEntity owner = entity.getOwner() instanceof LivingEntity living ? living : null;
        itemRenderer.renderStatic(
                owner,
                entity.getAxeStack(),
                ItemDisplayContext.GROUND,
                false,
                poseStack,
                bufferSource,
                entity.level(),
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
