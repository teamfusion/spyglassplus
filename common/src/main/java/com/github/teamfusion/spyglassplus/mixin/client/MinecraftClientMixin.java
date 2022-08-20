package com.github.teamfusion.spyglassplus.mixin.client;

import com.github.teamfusion.spyglassplus.entity.ScopingPlayer;
import com.github.teamfusion.spyglassplus.mixin.client.access.KeyBindingInvoker;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Shadow @Nullable public ClientPlayerEntity player;
    @Shadow @Final public GameOptions options;

    /**
     * Cancels attacks when scoping.
     */
    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void onDoAttack(CallbackInfoReturnable<Boolean> cir) {
        ScopingPlayer scopingPlayer = ScopingPlayer.cast(this.player);
        if (scopingPlayer.hasSpyglassStand()) cir.setReturnValue(false);
    }

    /**
     * Cancels block breaking when scoping.
     */
    @ModifyArg(
        method = "handleInputEvents",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/MinecraftClient;handleBlockBreaking(Z)V"
        ),
        index = 0
    )
    private boolean onHandleBlockBreaking(boolean input) {
        ScopingPlayer scopingPlayer = ScopingPlayer.cast(this.player);
        if (scopingPlayer.hasSpyglassStand()) return false;
        return input;
    }

    /**
     * Cancels item usage when scoping.
     */
    @Inject(method = "doItemUse", at = @At("HEAD"), cancellable = true)
    private void onDoItemUse(CallbackInfo ci) {
        ScopingPlayer scopingPlayer = ScopingPlayer.cast(this.player);
        if (scopingPlayer.hasSpyglassStand()) ci.cancel();
    }

    /**
     * Cancels hotbar key binds when scoping.
     */
    @Inject(method = "handleInputEvents", at = @At("HEAD"))
    private void onHandleInputEvents(CallbackInfo ci) {
        ScopingPlayer scopingPlayer = ScopingPlayer.cast(this.player);
        if (scopingPlayer.hasSpyglassStand()) for (int i = 0; i < 9; ++i) ((KeyBindingInvoker) this.options.hotbarKeys[i]).invokeReset();
    }
}
