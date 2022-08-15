package com.github.teamfusion.spyglassplus.mixin.client;

import com.github.teamfusion.spyglassplus.enchantment.SpyglassPlusEnchantments;
import com.github.teamfusion.spyglassplus.entity.ScopingEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static java.lang.Math.*;

@Environment(EnvType.CLIENT)
@Mixin(LightmapTextureManager.class)
public class LightmapTextureManagerMixin {
    @Unique private static long lastOpenedSpyglassAt;

    /**
     * Modifies brightness for the {@link SpyglassPlusEnchantments#ILLUMINATE illuminate enchantment}.
     */
    @Inject(method = "getBrightness", at = @At("RETURN"), cancellable = true)
    private static void modifyBrightnessForIlluminate(DimensionType type, int lightLevel, CallbackInfoReturnable<Float> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!client.options.getPerspective().isFirstPerson()) return;

        if (client.getCameraEntity() instanceof ScopingEntity scoping) {
            ItemStack stack = scoping.getScopingStack();
            long time = Util.getMeasuringTimeMs();
            if (EnchantmentHelper.getLevel(SpyglassPlusEnchantments.ILLUMINATE.get(), stack) > 0) {
                long diff = time - lastOpenedSpyglassAt;
                cir.setReturnValue(min(1.0F, max(cir.getReturnValueF(), diff / 1000f)));
            } else lastOpenedSpyglassAt = time;
        }
    }
}
