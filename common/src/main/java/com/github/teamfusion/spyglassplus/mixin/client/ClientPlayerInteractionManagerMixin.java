package com.github.teamfusion.spyglassplus.mixin.client;

import com.github.teamfusion.spyglassplus.entity.ScopingPlayer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
    @Shadow @Final private MinecraftClient client;

    /**
     * Always remove experience bar if scoping.
     */
    @Inject(method = "hasExperienceBar", at = @At("HEAD"), cancellable = true)
    private void onhasExperienceBar(CallbackInfoReturnable<Boolean> cir) {
        if (this.client.player instanceof ScopingPlayer scoping && scoping.hasSpyglassStand()) {
            cir.setReturnValue(false);
        }
    }
}
