package com.github.teamfusion.spyglassplus.mixin.client;

import com.github.teamfusion.spyglassplus.enchantment.SpyglassPlusEnchantments;
import com.github.teamfusion.spyglassplus.entity.ScopingEntity;
import com.github.teamfusion.spyglassplus.item.ISpyglass;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpyglassItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Shadow @Final MinecraftClient client;
    @Shadow private float fovMultiplier;

    /**
     * Implements default fovMultiplier for the spyglass stand.
     */
    @ModifyVariable(
        method = "updateFovMultiplier",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/render/GameRenderer;fovMultiplier:F",
            ordinal = 0
        ),
        index = 1
    )
    private float modifyFOnUpdateFovMultiplier(float f) {
        return f != SpyglassItem.field_30922
            && this.client.options.getPerspective().isFirstPerson()
            && this.client.getCameraEntity() instanceof ScopingEntity scoping && scoping.isScoping()
            ? SpyglassItem.field_30922 : f;
    }

    /**
     * Modifies {@link #fovMultiplier} for the {@link SpyglassPlusEnchantments#SCRUTINY scrutiny enchantment}.
     */
    @Inject(method = "updateFovMultiplier", at = @At("TAIL"))
    private void modifyFovMultiplierForScrutiny(CallbackInfo ci) {
        if (!this.client.options.getPerspective().isFirstPerson()) {
            return;
        }

        if (this.client.getCameraEntity() instanceof ScopingEntity scoping) {
            ItemStack stack = scoping.getScopingStack();
            int level = ISpyglass.getLocalScrutinyLevel(stack);
            if (level > 0) {
                this.fovMultiplier *= 0.4F / level;
            } else if (level < 0) {
                this.fovMultiplier *= 1.0F + (0.22F * Math.abs(level));
            }
        }
    }
}
