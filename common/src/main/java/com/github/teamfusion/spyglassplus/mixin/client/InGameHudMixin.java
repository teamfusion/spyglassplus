package com.github.teamfusion.spyglassplus.mixin.client;

import com.github.teamfusion.spyglassplus.client.event.BinocularsHudOverlayRenderEvent;
import com.github.teamfusion.spyglassplus.client.gui.BinocularsOverlayRenderer;
import com.github.teamfusion.spyglassplus.client.gui.DiscoveryHudRenderer;
import com.github.teamfusion.spyglassplus.client.gui.InGameHudAccess;
import com.github.teamfusion.spyglassplus.enchantment.SpyglassPlusEnchantments;
import com.github.teamfusion.spyglassplus.entity.ScopingEntity;
import com.github.teamfusion.spyglassplus.item.BinocularsItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(InGameHud.class)
public abstract class InGameHudMixin implements InGameHudAccess {
    @Unique private final DiscoveryHudRenderer discoveryHudRenderer = new DiscoveryHudRenderer();
    @Unique private final BinocularsOverlayRenderer binocularsOverlayRenderer = new BinocularsOverlayRenderer();

    @Shadow @Final private MinecraftClient client;
    @Shadow private int scaledWidth;
    @Shadow private int scaledHeight;

    @Unique
    @Override
    public DiscoveryHudRenderer getDiscoveryHudRenderer() {
        return this.discoveryHudRenderer;
    }

    /**
     * Renders the Binoculars' overlay instead of the Spyglass' overlay on use.
     */
    @Inject(method = "renderSpyglassOverlay", at = @At("HEAD"), cancellable = true)
    private void renderBinocularsOverlay(float scale, CallbackInfo ci) {
        if (this.client.getCameraEntity() instanceof ScopingEntity scoping && scoping.getScopingStack().getItem() instanceof BinocularsItem) {
            if (BinocularsHudOverlayRenderEvent.PRE.invoker().render(this.binocularsOverlayRenderer, scale, this.scaledWidth, this.scaledHeight).isFalse()) {
                return;
            }

            this.binocularsOverlayRenderer.render(scale, this.scaledWidth, this.scaledHeight);
            BinocularsHudOverlayRenderEvent.POST.invoker().render(this.binocularsOverlayRenderer, scale, this.scaledWidth, this.scaledHeight);
            ci.cancel();
        }
    }

    /**
     * Renders HUD for {@link SpyglassPlusEnchantments#DISCOVERY}.
     */
    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/network/ClientPlayerEntity;getFrozenTicks()I",
            shift = At.Shift.BEFORE
        )
    )
    private void renderDiscoveryHud(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        DiscoveryHudRenderer.render(this.discoveryHudRenderer, matrices, tickDelta, this.client.getCameraEntity());
    }
}
