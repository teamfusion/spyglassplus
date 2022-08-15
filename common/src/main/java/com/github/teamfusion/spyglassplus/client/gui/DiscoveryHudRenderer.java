package com.github.teamfusion.spyglassplus.client.gui;

import com.github.teamfusion.spyglassplus.SpyglassPlus;
import com.github.teamfusion.spyglassplus.enchantment.SpyglassPlusEnchantments;
import com.github.teamfusion.spyglassplus.entity.ScopingEntity;
import com.github.teamfusion.spyglassplus.entity.SpyglassStandEntity;
import com.github.teamfusion.spyglassplus.mixin.EntityInvoker;
import com.github.teamfusion.spyglassplus.mixin.client.InGameHudMixin;
import com.github.teamfusion.spyglassplus.tag.SpyglassPlusEntityTypeTags;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

import java.util.Optional;
import java.util.function.Predicate;

import static net.minecraft.util.math.MathHelper.*;

// TODO fix scaling

/**
 * Responsible for rendering the HUD elements created by {@link SpyglassPlusEnchantments#DISCOVERY}.
 * @see InGameHudMixin
 */
@Environment(EnvType.CLIENT)
public class DiscoveryHudRenderer extends DrawableHelper {
    public static final Identifier ICONS_TEXTURE = new Identifier(SpyglassPlus.MOD_ID, "textures/gui/discovery_icons.png");

    public static final int
        EYE_PHASES = 5, EYE_BLINK_TIME = 30,
        BOX_WIDTH = 97, BOX_HEIGHT = 124,
        EYE_WIDTH = 20, EYE_HEIGHT = 16;

    private final MinecraftClient client = MinecraftClient.getInstance();
    private final TextRenderer textRenderer = this.client.textRenderer;

    /**
     * The entity currently being rendered.
     */
    protected Entity activeEntity;

    /**
     * How open the discovery HUD is, similar to spyglassScale in {@link InGameHud}.
     */
    protected float openProgress;

    /**
     * The progress of the HUD eye blinking.
     * <p>Min: -0.2, Max: 1.2</p>
     */
    protected float eyePhase;

    /**
     * Whether the eye is closing or not. Controls whether to add or subtract from {@link #eyePhase}.
     */
    protected boolean eyeClosing;

    public DiscoveryHudRenderer() {
    }

    /**
     * Called when the HUD is not being rendered (when {@link #render(MatrixStack, float, Entity)} returns false).
     */
    public void reset() {
        this.openProgress = 0.0F;
        this.eyePhase = -0.2F;
    }

    public boolean render(MatrixStack matrices, float tickDelta, Entity camera) {
        if (!this.client.options.getPerspective().isFirstPerson() || !(camera instanceof ScopingEntity scopingEntity) || !scopingEntity.isScoping()) {
            this.activeEntity = null;
            return false;
        }

        ItemStack stack = scopingEntity.getScopingStack();
        int level = EnchantmentHelper.getLevel(SpyglassPlusEnchantments.DISCOVERY.get(), stack);
        if (!(level > 0)) return false;

        Entity targeted = this.raycast(camera, tickDelta, 64.0D);
        if (this.activeEntity != null) {
            if (!this.client.isPaused()) {
                float lastFrameDuration = this.client.getLastFrameDuration();

                this.eyePhase = clamp(this.eyePhase + ((1.0F / (EYE_BLINK_TIME * 20)) * (this.eyeClosing ? -lastFrameDuration : lastFrameDuration)), -0.2F, 1.2F);
                if (this.eyePhase >= 1.2F) {
                    this.eyeClosing = true;
                } else if (this.eyePhase <= -0.2F) this.eyeClosing = false;

                this.openProgress = lerp(0.5F * lastFrameDuration, this.openProgress, targeted == null ? 0.0F : 1.0F);
            }

            Window window = this.client.getWindow();
            int scaledHeight = window.getScaledHeight();
            int halfHeight = scaledHeight / 2;

            int x = 14;
            int y = halfHeight - (BOX_HEIGHT / 2);

            // scale
            matrices.push();

            double centerX = x + (BOX_WIDTH / 2d);
            matrices.translate(centerX, halfHeight, 0.0D);
            matrices.scale(this.openProgress, this.openProgress, this.openProgress);
            matrices.translate(-centerX, -halfHeight, 0.0D);

            // setup
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            // render
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, this.openProgress); // transparency

            RenderSystem.setShaderTexture(0, ICONS_TEXTURE);
            this.drawTexture(matrices, x, y, 0, 0, BOX_WIDTH, BOX_HEIGHT);

            int eyeTextureVOffset = floor(clamp(this.eyePhase, 0.0F, 1.0F) * (EYE_PHASES - 1));
            this.drawTexture(matrices, (int) (centerX - (EYE_WIDTH / 2d)) + 1, y + 3, BOX_WIDTH, eyeTextureVOffset * EYE_HEIGHT, EYE_WIDTH, EYE_HEIGHT);

            if (this.openProgress > 0.5F) {
                EntityType<?> type = this.activeEntity.getType();

                if (this.activeEntity instanceof LivingEntity livingEntity) {
                    int entityX = (int) centerX;
                    int entityY = y + BOX_HEIGHT - 15;
                    EntityDimensions base = EntityDimensions.fixed(1.0F, 1.0F);
                    EntityDimensions edim = type.getDimensions();
                    this.drawEntity(entityX, entityY, edim.width / base.width, edim.height / base.height, 40, livingEntity);
                }

                Text text = Optional.of(this.activeEntity.getDisplayName()).filter(t -> this.textRenderer.getWidth(t) < 90)
                                    .orElseGet(() -> Text.translatable(type.getTranslationKey()));
                int textWidth = this.textRenderer.getWidth(text);
                int textHeight = this.textRenderer.fontHeight;
                this.textRenderer.draw(matrices, text, (int) (x + (BOX_WIDTH / 2f) - (textWidth / 2f)) + 1, (int) (y + 14 + (textHeight / 2f)), 0x000000);
            }

            // post
            RenderSystem.disableBlend();
            matrices.pop();

            if (targeted != null) this.activeEntity = targeted;

            return true;
        }

        if (targeted != null) this.activeEntity = targeted;

        return false;
    }

    @SuppressWarnings("deprecation")
    public void drawEntity(int x, int y, float xScale, float yScale, int scale, LivingEntity entity) {
        float yawOffset = (float) Math.atan(-300 / 40.0f);
        float pitchOffset = (float) Math.atan(0 / 40.0f);

        MatrixStack matrices = RenderSystem.getModelViewStack();
        matrices.push();
        matrices.translate(x, y, 1050.0);
        matrices.scale(this.openProgress, this.openProgress, this.openProgress);
        matrices.scale(1.0f, 1.0f, -1.0f);
        RenderSystem.applyModelViewMatrix();

        MatrixStack matricesSub = new MatrixStack();
        matricesSub.translate(0.0, 0.0, 1000.0);

        float xyScale = Math.min(xScale, yScale);
        matricesSub.scale(xyScale * scale, xyScale * scale, scale);

        Quaternion yawQuaternion = Vec3f.POSITIVE_Z.getDegreesQuaternion(180.0f);
        Quaternion rotationQuaternion = Vec3f.POSITIVE_X.getDegreesQuaternion(pitchOffset * 20.0f);
        yawQuaternion.hamiltonProduct(rotationQuaternion);
        matricesSub.multiply(yawQuaternion);
        matricesSub.translate(0.0D, (1F - this.openProgress) * 2, 0.0D);

        float bodyYaw = entity.bodyYaw;
        float yaw = entity.getYaw();
        float pitch = entity.getPitch();
        float prevHeadYaw = entity.prevHeadYaw;
        float headYaw = entity.headYaw;
        Text customName = entity.getCustomName();

        entity.bodyYaw = 180.0f + yawOffset * 20.0f;
        entity.setYaw(180.0f + yawOffset * 40.0f);
        entity.setPitch(-pitchOffset * 20.0f);
        entity.headYaw = entity.getYaw();
        entity.prevHeadYaw = entity.getYaw();
        entity.setCustomName(null);

        DiffuseLighting.method_34742();
        EntityRenderDispatcher dispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();
        rotationQuaternion.conjugate();
        dispatcher.setRotation(rotationQuaternion);
        dispatcher.setRenderShadows(false);

        VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        RenderSystem.runAsFancy(() -> dispatcher.render(entity, 0.0, 0.0, 0.0, 0.0f, 1.0f, matricesSub, immediate, 0xF000F0));
        immediate.draw();

        dispatcher.setRenderShadows(true);
        entity.bodyYaw = bodyYaw;
        entity.setYaw(yaw);
        entity.setPitch(pitch);
        entity.prevHeadYaw = prevHeadYaw;
        entity.headYaw = headYaw;
        entity.setCustomName(customName);

        matrices.pop();
        RenderSystem.applyModelViewMatrix();
        DiffuseLighting.enableGuiDepthLighting();
    }

    @Override
    public void drawTexture(MatrixStack matrices, int x, int y, int u, int v, int width, int height) {
        drawTexture(matrices, x, y, this.getZOffset(), (float)u, (float)v, width, height, 128, 128);
    }


    /**
     * Retrieves the rotation from the camera, dependent on whether it is a {@link SpyglassStandEntity}.
     */
    public Vec2f getRotation(Entity camera, float tickDelta) {
        return camera instanceof SpyglassStandEntity stand
            ? new Vec2f(stand.getSpyglassYaw(tickDelta), stand.getSpyglassPitch(tickDelta))
            : new Vec2f(camera.getYaw(tickDelta), camera.getPitch(tickDelta));
    }

    /**
     * Retrieves the entity that the camera is looking at.
     */
    public Entity raycast(Entity camera, float tickDelta, double distance) {
        HitResult hit = camera.raycast(distance, tickDelta, false);
        Vec3d min = camera.getCameraPosVec(tickDelta);
        if (hit != null) distance = hit.getPos().squaredDistanceTo(min);

        Vec2f rotation = this.getRotation(camera, tickDelta);
        Vec3d rotationVector = ((EntityInvoker) camera).invokeGetRotationVector(rotation.y, rotation.x);
        Vec3d max = min.add(rotationVector.x * distance, rotationVector.y * distance, rotationVector.z * distance);

        Box box = camera.getBoundingBox().stretch(rotationVector.multiply(distance)).expand(1.0F);
        EntityHitResult entityHit = this.raycast(camera, min, max, box, entity -> !entity.isSpectator() && entity.canHit() && !entity.isInvisibleTo(this.client.player) && !entity.getType().isIn(SpyglassPlusEntityTypeTags.IGNORE_FOR_DISCOVERY_ENCHANTMENT), distance);
        if (entityHit != null) {
            Entity entity = entityHit.getEntity();
            Vec3d pos = entityHit.getPos();
            double distanceTo = min.squaredDistanceTo(pos);
            if (distanceTo < distance || hit == null) return entity;
        }

        return null;
    }

    /**
     * Modified and mapped version of {@link ProjectileUtil#raycast(Entity, Vec3d, Vec3d, Box, Predicate, double)}.
     * <p>Modifies the result of {@link Entity#getTargetingMargin()}.</p>
     */
    public EntityHitResult raycast(Entity entity, Vec3d min, Vec3d max, Box box, Predicate<Entity> predicate, double distance) {
        double runningDistance = distance;
        Entity resultEntity = null;
        Vec3d resultPos = null;

        for (Entity candidate : entity.world.getOtherEntities(entity, box, predicate)) {
            float margin = candidate.getTargetingMargin();
            Box candidateBoundingBox = candidate.getBoundingBox().expand(
                margin == 0.0F && !candidate.getType().isIn(SpyglassPlusEntityTypeTags.IGNORE_MARGIN_EXPANSION_FOR_DISCOVERY_ENCHANTMENT)
                    ? 0.175F : margin
            );

            Optional<Vec3d> optional = candidateBoundingBox.raycast(min, max);
            if (candidateBoundingBox.contains(min)) {
                if (!(runningDistance >= 0.0)) continue;
                resultEntity = candidate;
                resultPos = optional.orElse(min);
                runningDistance = 0.0;
                continue;
            }

            double squaredDistance;
            Vec3d runningPos;
            if (optional.isEmpty() || !((squaredDistance = min.squaredDistanceTo(runningPos = optional.get())) < runningDistance) && runningDistance != 0.0) continue;
            if (candidate.getRootVehicle() == entity.getRootVehicle()) {
                if (runningDistance != 0.0) continue;
                resultEntity = candidate;
                resultPos = runningPos;
                continue;
            }

            resultEntity = candidate;
            resultPos = runningPos;
            runningDistance = squaredDistance;
        }

        return resultEntity == null ? null : new EntityHitResult(resultEntity, resultPos);
    }
}
