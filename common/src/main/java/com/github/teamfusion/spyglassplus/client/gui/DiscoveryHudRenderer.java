package com.github.teamfusion.spyglassplus.client.gui;

import com.github.teamfusion.spyglassplus.SpyglassPlus;
import com.github.teamfusion.spyglassplus.enchantment.SpyglassPlusEnchantments;
import com.github.teamfusion.spyglassplus.entity.DiscoveryHudEntitySetup;
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
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

import java.util.Arrays;
import java.util.Locale;
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

    public static final String
        HEALTH_KEY   = translate("health"),
        BEHAVIOR_KEY = translate("behavior"),
        BEHAVIOR_HOLDER_KEY = translate("behavior_holder"),
        STRENGTH_KEY = translate("strength");

    public static final int
        EYE_PHASES = 5, EYE_BLINK_TIME = 30,
        BOX_WIDTH = 97, BOX_HEIGHT = 124,
        TITLE_BOX_WIDTH = 97, TITLE_BOX_HEIGHT = 32,
        EYE_WIDTH = 20, EYE_HEIGHT = 16;

    private final MinecraftClient client = MinecraftClient.getInstance();
    private final TextRenderer textRenderer = this.client.textRenderer;

    private int scaledWidth, scaledHeight;

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

    public static void render(DiscoveryHudRenderer discoveryHud, MatrixStack matrices, float tickDelta, Entity camera) {
        if (!discoveryHud.render(matrices, tickDelta, camera)) discoveryHud.reset();
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
            this.scaledWidth = window.getScaledWidth();
            this.scaledHeight = window.getScaledHeight();
            int halfHeight = this.scaledHeight / 2;
            int textHeight = this.textRenderer.fontHeight;

            boolean hasRenderBox = this.hasRenderBox(this.activeEntity);
            int boxWidth = hasRenderBox ? BOX_WIDTH : TITLE_BOX_WIDTH;
            int boxHeight = hasRenderBox ? BOX_HEIGHT : TITLE_BOX_HEIGHT;

            int x = 14;
            int y = halfHeight - (boxHeight / 2);

            /* Setup */

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            /* Left */

            matrices.push();

            double centerX = x + (boxWidth / 2d);
            matrices.translate(centerX, halfHeight, 0.0D);
            matrices.scale(this.openProgress, this.openProgress, this.openProgress);
            matrices.translate(-centerX, -halfHeight, 0.0D);

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, this.openProgress);

            // box
            RenderSystem.setShaderTexture(0, ICONS_TEXTURE);
            this.drawTexture(matrices, x, y, 0, hasRenderBox ? 0 : BOX_HEIGHT, boxWidth, boxHeight);

            int eyeTextureVOffset = floor(clamp(this.eyePhase, 0.0F, 1.0F) * (EYE_PHASES - 1));
            this.drawTexture(matrices, (int) (centerX - (EYE_WIDTH / 2d)) + 1, y + 3, boxWidth, eyeTextureVOffset * EYE_HEIGHT, EYE_WIDTH, EYE_HEIGHT);

            if (this.openProgress > 0.5F) {
                EntityType<?> type = this.activeEntity.getType();

                if (hasRenderBox) {
                    int entityX = (int) centerX;
                    int entityY = y + boxHeight - 15;
                    EntityDimensions base = EntityDimensions.fixed(1.0F, 1.0F);
                    EntityDimensions edim = type.getDimensions();
                    this.drawEntity(entityX, entityY, edim.width / base.width, edim.height / base.height, 40, this.activeEntity);
                }

                Text text = Optional.of(this.activeEntity.getDisplayName()).filter(t -> this.textRenderer.getWidth(t) < 90)
                                    .orElseGet(() -> Text.translatable(type.getTranslationKey()));
                int textWidth = this.textRenderer.getWidth(text);
                this.draw(matrices, text, (int) (x + (boxWidth / 2f) - (textWidth / 2f)) + 1, (int) (y + 14 + (textHeight / 2f)) + 1);
            }

            matrices.pop();

            /* Right */

            matrices.push();

            double centerRightX = x + this.scaledWidth - 28;
            matrices.translate(centerRightX, halfHeight, 0.0D);
            matrices.scale(this.openProgress, this.openProgress, this.openProgress);
            matrices.translate(-centerRightX, -halfHeight, 0.0D);

            // stats
            Text behaviorText = EntityBehavior.getText(this.activeEntity);
            if (behaviorText != null) {
                this.drawFromRight(matrices, Text.translatable(BEHAVIOR_KEY), 14, halfHeight);
                this.drawFromRight(matrices, Text.translatable(BEHAVIOR_HOLDER_KEY, behaviorText).formatted(Formatting.GRAY), 14, halfHeight + 1 + textHeight);
            }

            matrices.pop();

            /* Cleanup */

            RenderSystem.disableBlend();

            if (targeted != null) this.activeEntity = targeted;

            return true;
        }

        if (targeted != null) this.activeEntity = targeted;

        return false;
    }

    public boolean hasRenderBox(Entity entity) {
        return !entity.getType().isIn(SpyglassPlusEntityTypeTags.DO_NOT_RENDER_BOX_DISCOVERY);
    }

    @SuppressWarnings("deprecation")
    public void drawEntity(int x, int y, float xScale, float yScale, int scale, Entity entity) {
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

        float yaw = entity.getYaw();
        float pitch = entity.getPitch();
        Text customName = entity.getCustomName();

        float renderYaw = 180.0f + yawOffset * 40.0f;
        float renderPitch = -pitchOffset * 20.0f;
        entity.setYaw(renderYaw);
        entity.setPitch(renderPitch);
        entity.setCustomName(null);

        NbtCompound setupNbt = new NbtCompound();
        if (entity instanceof DiscoveryHudEntitySetup setup) setup.setupBeforeDiscoveryHud(setupNbt, renderYaw, renderPitch, yawOffset, pitchOffset);

        DiffuseLighting.method_34742();
        EntityRenderDispatcher dispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();
        rotationQuaternion.conjugate();
        dispatcher.setRotation(rotationQuaternion);
        dispatcher.setRenderShadows(false);

        VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        RenderSystem.runAsFancy(() -> dispatcher.render(entity, 0.0, 0.0, 0.0, 0.0f, 1.0f, matricesSub, immediate, 0xF000F0));
        immediate.draw();

        dispatcher.setRenderShadows(true);
        entity.setYaw(yaw);
        entity.setPitch(pitch);
        entity.setCustomName(customName);

        if (entity instanceof DiscoveryHudEntitySetup setup) setup.cleanupAfterDiscoveryHud(setupNbt);

        matrices.pop();
        RenderSystem.applyModelViewMatrix();
        DiffuseLighting.enableGuiDepthLighting();
    }

    public void draw(MatrixStack matrices, Text text, int x, int y) {
        this.textRenderer.draw(matrices, text, x, y, 0x000000);
    }

    public void drawFromRight(MatrixStack matrices, Text text, int x, int y) {
        int width = this.textRenderer.getWidth(text);
        this.textRenderer.draw(matrices, text, this.scaledWidth - x - width, y, 0xFFFFFF);
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
        EntityHitResult entityHit = this.raycast(camera, min, max, box, entity -> !entity.isSpectator() && entity.canHit() && !entity.isInvisibleTo(this.client.player) && !entity.getType().isIn(SpyglassPlusEntityTypeTags.IGNORE_DISCOVERY), distance);
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
                margin == 0.0F && !candidate.getType().isIn(SpyglassPlusEntityTypeTags.IGNORE_MARGIN_EXPANSION_DISCOVERY)
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

    public static String translate(String suffix) {
        return "text.%s.discovery_hud.%s".formatted(SpyglassPlus.MOD_ID, suffix);
    }

    public enum EntityBehavior {
        PASSIVE(SpyglassPlusEntityTypeTags.DISCOVERY_ENCHANTMENT_ENTITY_BEHAVIOR_PASSIVE),
        NEUTRAL(SpyglassPlusEntityTypeTags.DISCOVERY_ENCHANTMENT_ENTITY_BEHAVIOR_NEUTRAL),
        HOSTILE(SpyglassPlusEntityTypeTags.DISCOVERY_ENCHANTMENT_ENTITY_BEHAVIOR_HOSTILE),
        BOSS(SpyglassPlusEntityTypeTags.DISCOVERY_ENCHANTMENT_ENTITY_BEHAVIOR_BOSS);

        private final Predicate<EntityType<?>> predicate;
        private final String translationKey;

        EntityBehavior(TagKey<EntityType<?>> tag) {
            this.predicate = type -> type.isIn(tag);
            this.translationKey = BEHAVIOR_KEY + "." + this.name().toLowerCase(Locale.ROOT);
        }

        public String getTranslationKey() {
            return this.translationKey;
        }

        public boolean matches(EntityType<?> entity) {
            return this.predicate.test(entity);
        }

        /**
         * @implNote I would implement some fancy caching stuff, but this runs on tags that can be modified at runtime
         */
        public static EntityBehavior get(Entity entity) {
            return Arrays.stream(values())
                         .filter(behavior -> behavior.matches(entity.getType()))
                         .findFirst()
                         .orElse(null);
        }

        public static Text getText(Entity entity) {
            return Optional.ofNullable(EntityBehavior.get(entity))
                           .map(EntityBehavior::getTranslationKey)
                           .map(Text::translatable)
                           .orElse(null);
        }
    }
}
