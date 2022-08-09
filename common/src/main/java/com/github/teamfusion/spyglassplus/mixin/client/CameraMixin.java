package com.github.teamfusion.spyglassplus.mixin.client;

import com.github.teamfusion.spyglassplus.entity.SpyglassStandEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow protected abstract void setRotation(float yaw, float pitch);

    /**
     * Fix camera yaw/pitch based on a{@link SpyglassStandEntity}.
     */
    @Inject(
        method = "update",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V",
            ordinal = 0,
            shift = At.Shift.AFTER
        )
    )
    private void onUpdateRotation(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        if (focusedEntity instanceof SpyglassStandEntity spyglassStand) this.setRotation(spyglassStand.getSpyglassYaw(tickDelta), spyglassStand.getSpyglassPitch(tickDelta));
    }
}
