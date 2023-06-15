package com.github.teamfusion.spyglassplus.mixin.client;

import com.github.teamfusion.spyglassplus.entity.ScopingPlayer;
import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@SuppressWarnings("InvalidInjectorMethodSignature")
@Environment(EnvType.CLIENT)
@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
    @Shadow @Final private MinecraftClient client;
    @Shadow @Final private BufferBuilderStorage bufferBuilders;
    @Shadow @Nullable private PostEffectProcessor entityOutlinePostProcessor;
    @Shadow private int regularEntityCount;

    @Shadow protected abstract boolean canDrawEntityOutlines();
    @Shadow protected abstract void renderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices);

    /**
     * Carries spoofed bl4 across afterRenderEntities to beforeTryRenderOutlines.
     */
    @Unique private boolean renderOutline;

    /**
     * If the focused entity is spyglass stand, return null to skip player check.
     * <p>Forge API implements checks to do this itself.</p>
     */
    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;drawCurrentLayer()V",
            ordinal = 0
        ),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void afterRenderEntities(
            MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera,
            GameRenderer gameRenderer, LightmapTextureManager lightmap, Matrix4f positionMatrix, CallbackInfo ci,
            Profiler profiler, Vec3d cameraPos, double x, double y, double z
    ) {
        if (!this.shouldForceRenderPlayer()) {
            return;
        }

        // reimplemented entity renderer from WorldRenderer#render
        Entity entity = this.client.player;
        VertexConsumerProvider vertex;

        this.regularEntityCount++;

        if (entity.age == 0) {
            entity.lastRenderX = entity.getX();
            entity.lastRenderY = entity.getY();
            entity.lastRenderZ = entity.getZ();
        }

        if (this.canDrawEntityOutlines() && this.client.hasOutline(entity)) {
            this.renderOutline = true;

            OutlineVertexConsumerProvider outlineVertices = this.bufferBuilders.getOutlineVertexConsumers();
            vertex = outlineVertices;
            int teamColor = entity.getTeamColorValue();
            int r = teamColor >> 16 & 0xFF;
            int g = teamColor >> 8  & 0xFF;
            int b = teamColor       & 0xFF;
            outlineVertices.setColor(r, g, b, 0xFF);
        } else {
            vertex = this.bufferBuilders.getEntityVertexConsumers();
        }

        this.renderEntity(entity, x, y, z, tickDelta, matrices, vertex);
    }

    /**
     * Properly render outlines if forcing render in afterRenderEntities.
     */
    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/OutlineVertexConsumerProvider;draw()V",
            shift = At.Shift.AFTER
        ),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void beforeTryRenderOutlines(
        MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera,
        GameRenderer gameRenderer, LightmapTextureManager lightmap, Matrix4f positionMatrix, CallbackInfo ci,
        Profiler profiler, Vec3d cameraPos, double x, double y, double z,
        Matrix4f positionMatrix2, boolean hasCapturedFrustum, Frustum frustum, float viewDistance,
        boolean thickFog, boolean renderOutline
    ) {
        if (!this.shouldForceRenderPlayer()) {
            return;
        }

        if (!renderOutline && this.renderOutline) {
            this.entityOutlinePostProcessor.render(tickDelta);
            this.client.getFramebuffer().beginWrite(false);
        }

        this.renderOutline = false;
    }

    @Unique
    private boolean shouldForceRenderPlayer() {
        if (Platform.isForge()) {
            return false;
        }

        ScopingPlayer scopingPlayer = ScopingPlayer.cast(this.client.player);
        return scopingPlayer.hasSpyglassStand() && !this.client.options.getPerspective().isFirstPerson();
    }
}
