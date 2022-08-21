package com.github.teamfusion.spyglassplus.mixin.client;

import com.github.teamfusion.spyglassplus.client.gui.DiscoveryHudRenderer;
import com.github.teamfusion.spyglassplus.enchantment.SpyglassPlusEnchantments;
import com.github.teamfusion.spyglassplus.entity.ScopingEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

@Environment(EnvType.CLIENT)
@Mixin(Entity.class)
public class EntityMixin {
    /**
     * When the client camera is scoping, has indicate, and this is not
     * the targeted entity, darken the team color, used for glowing.
     */
    @Inject(method = "getTeamColorValue", at = @At("RETURN"), cancellable = true)
    private void onGetTeamColorValue(CallbackInfoReturnable<Integer> cir) {
        Entity that = (Entity) (Object) this;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getCameraEntity() instanceof ScopingEntity scopingEntity && scopingEntity.isScoping()) {
            ItemStack stack = scopingEntity.getScopingStack();
            if (EnchantmentHelper.getLevel(SpyglassPlusEnchantments.INDICATE.get(), stack) > 0) {
                Entity targeted = DiscoveryHudRenderer.getInstance().getTargetedEntity();
                if (targeted == null || that != targeted) {
                    Color color = new Color(cir.getReturnValueI());
                    cir.setReturnValue(color.darker().darker().getRGB());
                }
            }
        }
    }
}
