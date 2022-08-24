package com.github.teamfusion.spyglassplus.mixin.client;

import com.github.teamfusion.spyglassplus.entity.ScopingPlayer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.github.teamfusion.spyglassplus.util.MathUtil.DEGREES_TO_RADIANS;

@Environment(EnvType.CLIENT)
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> {
    @Shadow protected M model;

    private LivingEntityRendererMixin(EntityRendererFactory.Context context) {
        super(context);
    }

    /**
     * Directly update player head pitch/yaw for spyglass stand.
     */
    @Inject(
        method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/entity/model/EntityModel;setAngles(Lnet/minecraft/entity/Entity;FFFFF)V",
            shift = At.Shift.AFTER
        )
    )
    private void onAnimateModel(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light, CallbackInfo ci) {
        if (entity instanceof ScopingPlayer scoping) {
            scoping.getSpyglassStandEntity().ifPresent(spyglass -> {
                if (this.model instanceof BipedEntityModel<?> bipedEntityModel) {
                    ModelPart head = bipedEntityModel.head;
                    ModelPart hat = bipedEntityModel.hat;
                    head.yaw = spyglass.getSpyglassYaw(tickDelta) * DEGREES_TO_RADIANS;
                    head.pitch = spyglass.getSpyglassPitch(tickDelta) * DEGREES_TO_RADIANS;
                    hat.yaw = head.yaw;
                    hat.pitch = head.pitch;
                }
            });
        }
    }
}
