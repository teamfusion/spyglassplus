package com.github.teamfusion.spyglassplus.mixin.forge;

import com.github.teamfusion.spyglassplus.client.gui.DiscoveryHudRenderer;
import com.github.teamfusion.spyglassplus.client.gui.InGameHudAccess;
import com.github.teamfusion.spyglassplus.enchantment.SpyglassPlusEnchantments;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(ForgeGui.class)
public abstract class ForgeGuiMixin extends InGameHud implements InGameHudAccess {
    private ForgeGuiMixin(MinecraftClient client, ItemRenderer itemRenderer) {
        super(client, itemRenderer);
    }

    /**
     * Renders HUD for {@link SpyglassPlusEnchantments#DISCOVERY}.
     */
    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        DiscoveryHudRenderer.render(this.getDiscoveryHud(), matrices, tickDelta, this.client.getCameraEntity());
    }
}
