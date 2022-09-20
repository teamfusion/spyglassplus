package com.github.teamfusion.spyglassplus.mixin.client;

import com.github.teamfusion.spyglassplus.client.SpyglassPlusClient;
import com.github.teamfusion.spyglassplus.client.entity.CommandTargetManager;
import com.github.teamfusion.spyglassplus.enchantment.SpyglassPlusEnchantments;
import com.github.teamfusion.spyglassplus.entity.ScopingEntity;
import com.github.teamfusion.spyglassplus.entity.ScopingPlayer;
import com.github.teamfusion.spyglassplus.mixin.client.access.KeyBindingInvoker;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
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
public abstract class MinecraftClientMixin {
    @Shadow @Nullable public ClientPlayerEntity player;
    @Shadow @Final public GameOptions options;

    @Shadow @Nullable public abstract Entity getCameraEntity();

    /**
     * Cancels attacks when scoping.
     */
    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void onDoAttack(CallbackInfoReturnable<Boolean> cir) {
        ScopingPlayer scopingPlayer = ScopingPlayer.cast(this.player);
        if (scopingPlayer.hasSpyglassStand()) {
            cir.setReturnValue(false);
        }
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
        if (scopingPlayer.hasSpyglassStand()) {
            ci.cancel();
        }
    }

    /**
     * Cancels hotbar key binds when scoping.
     */
    @Inject(method = "handleInputEvents", at = @At("HEAD"))
    private void onHandleInputEvents(CallbackInfo ci) {
        ScopingPlayer scopingPlayer = ScopingPlayer.cast(this.player);
        if (scopingPlayer.hasSpyglassStand()) {
            for (int i = 0; i < 9; ++i) {
                ((KeyBindingInvoker) this.options.hotbarKeys[i]).invokeReset();
            }
        }
    }


    /**
     * Enables glowing for {@link SpyglassPlusEnchantments#INDICATE}.
     */
    @Inject(method = "hasOutline", at = @At("HEAD"), cancellable = true)
    private void onHasOutline(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (SpyglassPlusClient.INDICATE_TARGET_MANAGER.isIndicated(entity)) {
            cir.setReturnValue(true);
            return;
        }

        if (this.getCameraEntity() instanceof ScopingEntity scopingEntity && scopingEntity.isScoping()) {
            if (this.options.getPerspective().isFirstPerson()) {
                ItemStack stack = scopingEntity.getScopingStack();

                if (EnchantmentHelper.getLevel(SpyglassPlusEnchantments.INDICATE.get(), stack) > 0) {
                    cir.setReturnValue(true);
                    return;
                }

                if (EnchantmentHelper.getLevel(SpyglassPlusEnchantments.COMMAND.get(), stack) > 0) {
                    CommandTargetManager manager = SpyglassPlusClient.COMMAND_TARGET_MANAGER;
                    if (manager.getEntity() == entity || manager.getLastTargetedEntity() == entity) {
                        cir.setReturnValue(true);
                    }
                }
            }
        }
    }

    /**
     * Resets indicated entities on disconnect.
     */
    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("HEAD"))
    private void onDisconnect(Screen screen, CallbackInfo ci) {
        SpyglassPlusClient.INDICATE_TARGET_MANAGER.reset();
    }
}
