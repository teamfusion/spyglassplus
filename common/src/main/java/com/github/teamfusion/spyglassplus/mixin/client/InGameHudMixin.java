package com.github.teamfusion.spyglassplus.mixin.client;

import com.github.teamfusion.spyglassplus.SpyglassPlus;
import com.github.teamfusion.spyglassplus.client.gui.DiscoveryHudRenderer;
import com.github.teamfusion.spyglassplus.enchantment.SpyglassPlusEnchantments;
import com.github.teamfusion.spyglassplus.entity.ScopingEntity;
import com.github.teamfusion.spyglassplus.item.BinocularsItem;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Unique private static final Identifier BINOCULARS_SCOPE_TEXTURE = new Identifier(SpyglassPlus.MOD_ID, "textures/misc/binoculars_scope.png");
    @Unique private final DiscoveryHudRenderer discoveryHud = new DiscoveryHudRenderer();

    @Shadow @Final private MinecraftClient client;
    @Shadow private int scaledWidth;
    @Shadow private int scaledHeight;

    /**
     * Renders the Binoculars' overlay instead of the Spyglass' overlay on use.
     */
    @Inject(method = "renderSpyglassOverlay", at = @At("HEAD"), cancellable = true)
    private void onRenderSpyglassOverlay(float scale, CallbackInfo ci) {
        if (this.client.getCameraEntity() instanceof ScopingEntity scoping && scoping.getScopingStack().getItem() instanceof BinocularsItem) {
            this.renderBinocularsOverlay(scale);
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
        if (!this.discoveryHud.render(matrices, tickDelta, this.client.getCameraEntity())) this.discoveryHud.reset();
    }

    @Unique
    private void renderBinocularsOverlay(float scale) {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, BINOCULARS_SCOPE_TEXTURE);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        float ws;
        float hs = ws = (float)Math.min(this.scaledWidth, this.scaledHeight);
        float h = Math.min((float)this.scaledWidth / ws, (float)this.scaledHeight / hs) * scale;
        float hori = ws * h * 1.82F;
        float vert = hs * h * 0.95F;
        float left = ((float)this.scaledWidth - hori) / 2.0f;
        float top = ((float)this.scaledHeight - vert) / 2.0f;
        float right = left + hori;
        float bottom = top + vert;
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        buffer.vertex(left, bottom, -90.0).texture(0.0f, 1.0f).next();  // bottom left
        buffer.vertex(right, bottom, -90.0).texture(1.0f, 1.0f).next(); // bottom right
        buffer.vertex(right, top, -90.0).texture(1.0f, 0.0f).next();    // top right
        buffer.vertex(left, top, -90.0).texture(0.0f, 0.0f).next();     // top left
        tessellator.draw();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableTexture();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(0.0, this.scaledHeight, -90.0).color(0, 0, 0, 255).next();
        buffer.vertex(this.scaledWidth, this.scaledHeight, -90.0).color(0, 0, 0, 255).next();
        buffer.vertex(this.scaledWidth, bottom, -90.0).color(0, 0, 0, 255).next();
        buffer.vertex(0.0, bottom, -90.0).color(0, 0, 0, 255).next();
        buffer.vertex(0.0, top, -90.0).color(0, 0, 0, 255).next();
        buffer.vertex(this.scaledWidth, top, -90.0).color(0, 0, 0, 255).next();
        buffer.vertex(this.scaledWidth, 0.0, -90.0).color(0, 0, 0, 255).next();
        buffer.vertex(0.0, 0.0, -90.0).color(0, 0, 0, 255).next();
        buffer.vertex(0.0, bottom, -90.0).color(0, 0, 0, 255).next();
        buffer.vertex(left, bottom, -90.0).color(0, 0, 0, 255).next();
        buffer.vertex(left, top, -90.0).color(0, 0, 0, 255).next();
        buffer.vertex(0.0, top, -90.0).color(0, 0, 0, 255).next();
        buffer.vertex(right, bottom, -90.0).color(0, 0, 0, 255).next();
        buffer.vertex(this.scaledWidth, bottom, -90.0).color(0, 0, 0, 255).next();
        buffer.vertex(this.scaledWidth, top, -90.0).color(0, 0, 0, 255).next();
        buffer.vertex(right, top, -90.0).color(0, 0, 0, 255).next();
        tessellator.draw();
        RenderSystem.enableTexture();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
