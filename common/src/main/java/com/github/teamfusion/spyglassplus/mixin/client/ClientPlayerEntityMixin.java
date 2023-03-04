package com.github.teamfusion.spyglassplus.mixin.client;

import com.github.teamfusion.spyglassplus.entity.ScopingPlayer;
import com.github.teamfusion.spyglassplus.entity.SpyglassStandEntity;
import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity implements ScopingPlayer {
    @Shadow @Final protected MinecraftClient client;

    private ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    /**
     * If the player's spyglass stand does not agree that the player is its spyglass stand, stop using it.
     */
    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void onTickMovement(CallbackInfo ci) {
        this.getSpyglassStandEntity().ifPresent(entity -> {
            if (!entity.isUser(this)) {
                this.setSpyglassStand(null);
                entity.stopUsingSpyglassClient(this);
            }
        });
    }

    /**
     * Reverses side effects of {@link #onIsCamera(CallbackInfoReturnable)}.
     */
    @Inject(
        method = "tickNewAi",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;tickNewAi()V",
            shift = At.Shift.AFTER
        ),
        cancellable = true
    )
    private void onTickNewAi(CallbackInfo ci) {
        if (this.client.getCameraEntity() instanceof SpyglassStandEntity entity && entity.isUser(this)) {
            ci.cancel();
        }
    }

    /**
     * Overrides if in spyglass stand.
     */
    @Inject(method = "isCamera", at = @At("HEAD"), cancellable = true)
    private void onIsCamera(CallbackInfoReturnable<Boolean> cir) {
        if (this.client.getCameraEntity() instanceof SpyglassStandEntity entity && entity.isUser(this)) {
            cir.setReturnValue(true);
        }
    }
}
