package com.github.teamfusion.spyglassplus.mixin.client;

import com.github.teamfusion.spyglassplus.client.SpyglassPlusClient;
import com.github.teamfusion.spyglassplus.client.entity.CommandTargetManager;
import com.github.teamfusion.spyglassplus.client.gui.DiscoveryHudRenderer;
import com.github.teamfusion.spyglassplus.enchantment.SpyglassPlusEnchantments;
import com.github.teamfusion.spyglassplus.entity.ScopingEntity;
import com.github.teamfusion.spyglassplus.item.ISpyglass;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow public abstract boolean isGlowing();

    /**
     * When the client camera is scoping, has indicate, and this is not
     * the targeted entity, darken the team color, used for glowing.
     */
    @Inject(method = "getTeamColorValue", at = @At("RETURN"), cancellable = true)
    private void onGetTeamColorValue(CallbackInfoReturnable<Integer> cir) {
        if (this.isGlowing()) {
            return;
        }

        Entity that = (Entity) (Object) this;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getCameraEntity() instanceof ScopingEntity scopingEntity && scopingEntity.isScoping()) {
            ItemStack stack = scopingEntity.getScopingStack();

            if (EnchantmentHelper.getLevel(SpyglassPlusEnchantments.COMMAND.get(), stack) > 0) {
                CommandTargetManager manager = SpyglassPlusClient.COMMAND_TARGET_MANAGER;
                if (manager.getLastTargetedEntity() == that) {
                    cir.setReturnValue(0xFFA500);
                    return;
                } else if (manager.getEntity() == that) {
                    int color = 0xFF0000;
                    float progress = (float) manager.getCommandTicks() / ISpyglass.MAX_COMMAND_TICKS;
                    cir.setReturnValue(this.multiplyColorByFactor(color, progress));
                    return;
                }
            }

            if (EnchantmentHelper.getLevel(SpyglassPlusEnchantments.INDICATE.get(), stack) > 0) {
                Entity targeted = DiscoveryHudRenderer.getInstance().getTargetedEntity();
                if ((that == null || that != targeted) && !SpyglassPlusClient.INDICATE_TARGET_MANAGER.isIndicated(that)) {
                    int color = cir.getReturnValueI();
                    float factor = 0.5f;
                    cir.setReturnValue(this.multiplyColorByFactor(color, factor));
                }
            }
        }
    }

    @Unique
    private int multiplyColorByFactor(int color, float factor) {
        int r = (int) ((color >> 16 & 0xFF) * factor);
        int g = (int) ((color >> 8  & 0xFF) * factor);
        int b = (int) ((color       & 0xFF) * factor);
        return (((r << 8) + g) << 8) + b;
    }
}
