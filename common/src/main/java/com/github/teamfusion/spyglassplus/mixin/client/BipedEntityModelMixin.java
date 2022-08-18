package com.github.teamfusion.spyglassplus.mixin.client;

import com.github.teamfusion.spyglassplus.item.BinocularsItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.BipedEntityModel.ArmPose;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(BipedEntityModel.class)
public abstract class BipedEntityModelMixin<T extends LivingEntity> {
    @Shadow public ArmPose rightArmPose;
    @Shadow public ArmPose leftArmPose;

    @Shadow protected abstract void positionLeftArm(T entity);
    @Shadow protected abstract void positionRightArm(T entity);

    /**
     * Set both hands to spyglass pose when using binoculars.
     */
    @Inject(
        method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;isUsingItem()Z",
            shift = At.Shift.AFTER
        )
    )
    private void onGetArmPose(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch, CallbackInfo ci) {
        if ((this.leftArmPose == ArmPose.SPYGLASS || this.rightArmPose == ArmPose.SPYGLASS) && entity.getActiveItem().getItem() instanceof BinocularsItem) {
            this.rightArmPose = ArmPose.SPYGLASS;
            this.leftArmPose = ArmPose.SPYGLASS;

            this.positionLeftArm(entity);
            this.positionRightArm(entity);
        }
    }
}
