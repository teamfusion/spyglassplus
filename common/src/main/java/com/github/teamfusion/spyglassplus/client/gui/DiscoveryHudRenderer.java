package com.github.teamfusion.spyglassplus.client.gui;

import com.github.teamfusion.spyglassplus.SpyglassPlus;
import com.github.teamfusion.spyglassplus.client.event.DiscoveryHudRenderEvent;
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
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.tag.TagKey;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.RaycastContext;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static net.minecraft.util.math.MathHelper.*;

// TODO fix scaling

/**
 * Responsible for rendering the HUD elements created by {@link SpyglassPlusEnchantments#DISCOVERY}.
 * @see InGameHudMixin
 */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class DiscoveryHudRenderer extends DrawableHelper {
    public static final Identifier ICONS_TEXTURE = new Identifier(SpyglassPlus.MOD_ID, "textures/gui/discovery_icons.png");

    /**
     * A translation key.
     */
    public static final String
        HEALTH_KEY          = translate("health"),
        HEALTH_ICON_KEY     = HEALTH_KEY + ".icon",
        HEALTH_HOLDER_KEY   = HEALTH_KEY + ".holder",

        BEHAVIOR_KEY        = translate("behavior"),
        BEHAVIOR_ICON_KEY   = BEHAVIOR_KEY + ".icon",
        BEHAVIOR_HOLDER_KEY = BEHAVIOR_KEY + ".holder",

        STRENGTH_KEY        = translate("strength"),
        STRENGTH_ICON_KEY   = STRENGTH_KEY + ".icon",
        STRENGTH_HOLDER_KEY = STRENGTH_KEY + ".holder";

    public static final Identifier DISCOVERY_FONT = new Identifier(SpyglassPlus.MOD_ID, "discovery_icons");
    public static final Style DISCOVERY_FONT_STYLE = Style.EMPTY.withFont(DISCOVERY_FONT).withFormatting(Formatting.RED);

    public static final Text
        BEHAVIOR_ICON = Text.translatable(BEHAVIOR_ICON_KEY).setStyle(DISCOVERY_FONT_STYLE),
        HEALTH_ICON = Text.translatable(HEALTH_ICON_KEY).setStyle(DISCOVERY_FONT_STYLE),
        STRENGTH_ICON = Text.translatable(STRENGTH_ICON_KEY).setStyle(DISCOVERY_FONT_STYLE);

    public static final int
        BOX_WIDTH = 97, BOX_HEIGHT = 124,
        TITLE_BOX_WIDTH = 97, TITLE_BOX_HEIGHT = 32,
        EYE_WIDTH = 20, EYE_HEIGHT = 16, EYE_PHASES = 5,
        RIGHT_SIDEBAR_TEXT_BORDER_SIZE = 4;

    public static final float
        EYE_BLINK_FREQUENCY = 0.0075F,
        EYE_BLINK_SPEED = 0.15F,
        BLACK_OPACITY = 0.2F;

    private final MinecraftClient client = MinecraftClient.getInstance();
    private final TextRenderer textRenderer = this.client.textRenderer;
    private final Random random = Random.create();

    private int scaledWidth;

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
     * @see InGameHudMixin
     */
    public static void render(DiscoveryHudRenderer discoveryHud, MatrixStack matrices, float tickDelta, Entity camera) {
        if (DiscoveryHudRenderEvent.PRE.invoker().render(discoveryHud, matrices, tickDelta, camera).isFalse()) return;

        if (discoveryHud.render(matrices, tickDelta, camera)) {
            DiscoveryHudRenderEvent.POST.invoker().render(discoveryHud, matrices, tickDelta, camera);
        } else {
            discoveryHud.reset();
        }
    }

    /**
     * Called when the HUD is not being rendered (when {@link #render(MatrixStack, float, Entity)} returns false).
     */
    public void reset() {
        this.openProgress = 0.0F;
        this.eyePhase = -0.2F;
    }

    /**
     * @see DiscoveryHudRenderer#render(DiscoveryHudRenderer, MatrixStack, float, Entity)
     */
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
            EntityType<?> type = this.activeEntity.getType();

            if (!this.client.isPaused()) {
                float lastFrameDuration = this.client.getLastFrameDuration();

                // eye
                float delta = this.eyePhase < 0
                    ? this.random.nextFloat() * EYE_BLINK_FREQUENCY
                    : 1.0F / (EYE_BLINK_SPEED * 20);

                this.eyePhase = this.eyePhase + (delta * (this.eyeClosing ? -lastFrameDuration : lastFrameDuration));

                if (this.eyePhase >= 1.2F) {
                    this.eyeClosing = true;
                } else if (this.eyePhase <= -0.2F) {
                    this.eyeClosing = false;
                }

                // opening
                this.openProgress = lerp(0.5F * lastFrameDuration, this.openProgress, targeted == null ? 0.0F : 1.0F);
            }

            Window window = this.client.getWindow();
            this.scaledWidth = window.getScaledWidth();
            int halfHeight = window.getScaledHeight() / 2;
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

            int eyeTextureVOffset = floor(clamp(this.eyePhase, 0.0F, 1.0F) * (EYE_PHASES - 1)) * EYE_HEIGHT;
            this.drawTexture(matrices, (int) (centerX - (EYE_WIDTH / 2d)) + 1, y + 3, boxWidth, eyeTextureVOffset, EYE_WIDTH, EYE_HEIGHT);

            if (this.openProgress > 0.5F) {
                if (hasRenderBox) {
                    // draw entity
                    int entityX = (int) centerX;
                    int entityY = y + boxHeight - 15;
                    EntityDimensions baseDimensions = EntityDimensions.fixed(2.0F, 2.0F);
                    EntityDimensions entityDimensions = type.getDimensions();
                    float scale = entityDimensions.height > baseDimensions.height ? 1 / entityDimensions.height / baseDimensions.height : 1;
                    this.drawEntity(entityX, entityY, scale, scale, 30, this.activeEntity);
                }

                // draw entity name
                Text text = Optional.of(this.activeEntity.getDisplayName()).filter(t -> this.textRenderer.getWidth(t) < 90)
                                    .orElseGet(() -> Text.translatable(type.getTranslationKey()));
                int textWidth = this.textRenderer.getWidth(text);
                this.drawText(matrices, text, (int) (x + (boxWidth / 2f) - (textWidth / 2f)) + 1, (int) (y + 14 + (textHeight / 2f)) + 1);
            }

            matrices.pop();

            /* Right */

            matrices.push();

            double centerRightX = this.scaledWidth - x;
            matrices.translate(centerRightX, halfHeight, 0.0D);
            matrices.scale(this.openProgress, this.openProgress, this.openProgress);
            matrices.translate(-centerRightX, -halfHeight, 0.0D);

            // stats
            if (!type.isIn(SpyglassPlusEntityTypeTags.IGNORE_STATS_DISCOVERY)) {
                if (level >= 2) {
                    // behavior
                    Text behaviorText = EntityBehavior.getText(this.activeEntity);
                    if (behaviorText != null) {
                        int behaviorY = halfHeight - (textHeight * 3) - 1;
                        this.drawTextClusterFromRight(matrices, x + 1, behaviorY,
                            Text.translatable(BEHAVIOR_KEY, BEHAVIOR_ICON),
                            Text.translatable(BEHAVIOR_HOLDER_KEY, behaviorText, BEHAVIOR_ICON).formatted(Formatting.GRAY)
                        );
                    }
                }

                if (this.activeEntity instanceof LivingEntity livingEntity) {
                    if (level >= 3) {
                        // health
                        float hurt = (float) livingEntity.hurtTime / livingEntity.maxHurtTime;
                        this.drawTextClusterFromRight(matrices, x + 1, halfHeight, ((Float.isNaN(hurt) ? 0 : hurt) * (1 - BLACK_OPACITY)) + BLACK_OPACITY, BLACK_OPACITY, BLACK_OPACITY,
                            Text.translatable(HEALTH_KEY, HEALTH_ICON),
                            this.createHealthHolderText(HEALTH_HOLDER_KEY, HEALTH_ICON, livingEntity.getHealth())
                        );

                        if (livingEntity.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE) != null) {
                            // strength
                            int strengthY = halfHeight + (textHeight * 3) + 1;
                            this.drawTextClusterFromRight(matrices, x + 1, strengthY,
                                Text.translatable(STRENGTH_KEY, STRENGTH_ICON),
                                this.createHealthHolderText(STRENGTH_HOLDER_KEY, STRENGTH_ICON,
                                    (float) livingEntity.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE)
                                )
                            );
                        }
                    }
                }
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

    public void drawText(MatrixStack matrices, Text text, int x, int y) {
        this.textRenderer.draw(matrices, text, x, y, 0x000000);
    }

    /**
     * Draws text leftwards from the right side of the screen, with a transparent background.
     */
    public void drawTextClusterFromRight(MatrixStack matrices, int rawX, int y, float br, float bg, float bb, Text... texts) {
        int x = this.scaledWidth - rawX;
        int l = texts.length;

        // background
        int longestWidth = Stream.of(texts).mapToInt(this.textRenderer::getWidth).max().orElse(0);
        fill(matrices,
            x - longestWidth - RIGHT_SIDEBAR_TEXT_BORDER_SIZE,
            y - RIGHT_SIDEBAR_TEXT_BORDER_SIZE,
            x + RIGHT_SIDEBAR_TEXT_BORDER_SIZE - 1,
            (y + (l * (this.textRenderer.fontHeight + 1))) + RIGHT_SIDEBAR_TEXT_BORDER_SIZE - 3,
            br, bg, bb, 0.5F
        );

        // text
        for (int i = 0; i < texts.length; i++) {
            Text text = texts[i];
            int width = this.textRenderer.getWidth(text);
            this.textRenderer.draw(matrices, text, x - width, y + (i * (this.textRenderer.fontHeight + 1)), 0xFFFFFF);
        }
    }

    public void drawTextClusterFromRight(MatrixStack matrices, int rawX, int y, Text... texts) {
        this.drawTextClusterFromRight(matrices, rawX, y, BLACK_OPACITY, BLACK_OPACITY, BLACK_OPACITY, texts);
    }

    /**
     * Implementation of {@link #fill(MatrixStack, int, int, int, int, int)} for RGB.
     */
    public void fill(MatrixStack matrices, int x1, int y1, int x2, int y2, float r, float g, float b, float alpha) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        if (x1 < x2) {
            int i = x1;
            x1 = x2;
            x2 = i;
        }

        if (y1 < y2) {
            int i = y1;
            y1 = y2;
            y2 = i;
        }

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, (float)x1, (float)y2, 0.0F).color(r, g, b, alpha).next();
        bufferBuilder.vertex(matrix, (float)x2, (float)y2, 0.0F).color(r, g, b, alpha).next();
        bufferBuilder.vertex(matrix, (float)x2, (float)y1, 0.0F).color(r, g, b, alpha).next();
        bufferBuilder.vertex(matrix, (float)x1, (float)y1, 0.0F).color(r, g, b, alpha).next();
        BufferRenderer.drawWithShader(bufferBuilder.end());

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    /**
     * Constructs text that is displayed below a statistic.
     */
    public Text createHealthHolderText(String key, Text icon, float health) {
        return Text.translatable(key, this.formatHearts(health), icon).formatted(Formatting.GRAY);
    }

    /**
     * Transforms a given health value into its representation as hearts, to 2 decimal places.
     */
    public String formatHearts(double health) {
        return String.format("%.1f", 0.5D * Math.round(health));
    }

    /**
     * Whether an entity renders inside of its box.
     */
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

        matricesSub.scale(xScale * scale, yScale * scale, scale);

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
        // calculate a position vector from the camera's rotation
        Vec2f rotation = this.getRotation(camera, tickDelta);
        Vec3d vector = ((EntityInvoker) camera).invokeGetRotationVector(rotation.y, rotation.x);

        // calculate minimum and maximum points of raycast
        Vec3d min = camera.getCameraPosVec(tickDelta);
        Vec3d max = min.add(vector.x * distance, vector.y * distance, vector.z * distance);

        // grab default hit result
        HitResult hit = camera.world.raycast(new RaycastContext(min, max, RaycastContext.ShapeType.VISUAL, RaycastContext.FluidHandling.NONE, camera));
        if (hit != null) distance = hit.getPos().squaredDistanceTo(min);

        // calculate entity hit result
        Box net = camera.getBoundingBox().stretch(vector.multiply(distance)).expand(1.0F);
        EntityHitResult entityHit = this.raycast(camera, min, max, net, this::isVisibleToRaycast, distance);

        if (entityHit != null) {
            Entity entity = entityHit.getEntity();
            Vec3d pos = entityHit.getPos();
            double entityDistance = min.squaredDistanceTo(pos);
            if (entityDistance < distance || hit == null) return entity;
        }

        return null;
    }

    public boolean isVisibleToRaycast(Entity entity) {
        return !entity.isSpectator() && entity.canHit() && !entity.isInvisibleTo(this.client.player) && !entity.getType().isIn(SpyglassPlusEntityTypeTags.IGNORE_DISCOVERY);
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

    public Entity getActiveEntity() {
        return this.activeEntity;
    }

    public float getOpenProgress() {
        return this.openProgress;
    }

    public float getEyePhase() {
        return this.eyePhase;
    }

    public boolean isEyeClosing() {
        return this.eyeClosing;
    }

    /**
     * Represents an entity's overall behavior.
     */
    public enum EntityBehavior {
        PASSIVE(SpyglassPlusEntityTypeTags.DISCOVERY_ENCHANTMENT_ENTITY_BEHAVIOR_PASSIVE),
        NEUTRAL(SpyglassPlusEntityTypeTags.DISCOVERY_ENCHANTMENT_ENTITY_BEHAVIOR_NEUTRAL),
        HOSTILE(SpyglassPlusEntityTypeTags.DISCOVERY_ENCHANTMENT_ENTITY_BEHAVIOR_HOSTILE),
        BOSS(SpyglassPlusEntityTypeTags.DISCOVERY_ENCHANTMENT_ENTITY_BEHAVIOR_BOSS);

        private final Predicate<EntityType<?>> predicate;
        private final String translationKey;

        EntityBehavior(TagKey<EntityType<?>> tag) {
            this.predicate = type -> type.isIn(tag);
            this.translationKey = BEHAVIOR_KEY + ".type." + this.name().toLowerCase(Locale.ROOT);
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
