package com.github.teamfusion.spyglassplus.mixin.forge.client;

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
import net.minecraftforge.client.gui.overlay.NamedGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
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
     * @implNote Does not use Forge's events for simplier access to {@link #getDiscoveryHudRenderer()} and {@link #client this.client}.
     */
    @Inject(
        method = "lambda$render$0",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/client/gui/overlay/ForgeGui;post(Lnet/minecraftforge/client/gui/overlay/NamedGuiOverlay;Lnet/minecraft/client/util/math/MatrixStack;)V",
            shift = At.Shift.AFTER
        )
    )
    private void onRender(MatrixStack matrices, float tickDelta, NamedGuiOverlay overlay, CallbackInfo ci) {
        if (overlay == VanillaGuiOverlay.SPYGLASS.type()) DiscoveryHudRenderer.render(this.getDiscoveryHudRenderer(), matrices, tickDelta, this.client.getCameraEntity());
    }
}
