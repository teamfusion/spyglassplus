package com.github.teamfusion.spyglassplus.mixin.client;

import com.github.teamfusion.spyglassplus.item.BinocularsItem;
import com.github.teamfusion.spyglassplus.item.ISpyglass;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.PlayerHeldItemFeatureRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Arm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(PlayerHeldItemFeatureRenderer.class)
public abstract class PlayerHeldItemFeatureRendererMixin {
    @Shadow protected abstract void renderSpyglass(LivingEntity entity, ItemStack stack, Arm arm, MatrixStack matrices, VertexConsumerProvider vertices, int light);

    /**
     * Render non-spyglass spyglass items.
     */
    @Inject(method = "renderItem", at = @At("HEAD"), cancellable = true)
    private void onRenderItem(LivingEntity entity, ItemStack stack, ModelTransformation.Mode mode, Arm arm, MatrixStack matrices, VertexConsumerProvider vertices, int light, CallbackInfo ci) {
        if (!stack.isOf(Items.SPYGLASS) && stack.getItem() instanceof ISpyglass && entity.getActiveItem() == stack && entity.handSwingTicks == 0) {
            this.renderSpyglass(entity, stack, arm, matrices, vertices, light);
            ci.cancel();
        }
    }

    /**
     * Shift binoculars to render on both eyes.
     */
    @Inject(
        method = "renderSpyglass",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/util/math/MatrixStack;translate(DDD)V",
            shift = At.Shift.AFTER
        )
    )
    private void onRenderSpyglass(LivingEntity entity, ItemStack stack, Arm arm, MatrixStack matrices, VertexConsumerProvider vertices, int light, CallbackInfo ci) {
        if (stack.getItem() instanceof BinocularsItem) {
            matrices.translate((arm == Arm.LEFT ? 2.5D : -2.5D) / 16.0D, 0.0D, 0.0D);
        }
    }
}
