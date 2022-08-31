package com.github.teamfusion.spyglassplus.mixin.forge;

import com.github.teamfusion.spyglassplus.client.gui.DiscoveryHudRenderer;
import com.github.teamfusion.spyglassplus.client.gui.InGameHudAccess;
import com.github.teamfusion.spyglassplus.enchantment.SpyglassPlusEnchantments;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.OverlayRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(ForgeIngameGui.class)
public abstract class ForgeGuiMixin extends InGameHud implements InGameHudAccess {
    private ForgeGuiMixin(MinecraftClient client) {
        super(client);
    }

    /**
     * Renders HUD for {@link SpyglassPlusEnchantments#DISCOVERY}.
     * @implNote Does not use Forge's events for simplier access to {@link #getDiscoveryHudRenderer()} and {@link #client this.client}.
     */
    @Inject(
        method = "lambda$render$26",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/client/gui/ForgeIngameGui;post(Lnet/minecraftforge/client/gui/IIngameOverlay;Lnet/minecraft/client/util/math/MatrixStack;)V",
            shift = At.Shift.AFTER
        )
    )
    private void onRender(MatrixStack matrices, float tickDelta, OverlayRegistry.OverlayEntry overlay, CallbackInfo ci) {
        if (overlay.getOverlay() == ForgeIngameGui.SPYGLASS_ELEMENT) {
            DiscoveryHudRenderer.render(this.getDiscoveryHudRenderer(), matrices, tickDelta, this.client.getCameraEntity());
        }
    }
}
