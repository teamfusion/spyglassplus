package com.github.teamfusion.spyglassplus.mixin;

import com.github.teamfusion.spyglassplus.entity.ScopingPlayer;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    /**
     * Prevents movement input when scoping in spyglass stand.
     */
    @Inject(method = "isImmobile", at = @At("HEAD"), cancellable = true)
    private void onIsImmobile(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity that = (LivingEntity) (Object) this;
        if (that instanceof ScopingPlayer scopingPlayer && scopingPlayer.getSpyglassStand().isPresent()) cir.setReturnValue(true);
    }
}
