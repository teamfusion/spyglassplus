package com.github.teamfusion.spyglassplus.client.gui;

import com.github.teamfusion.spyglassplus.SpyglassPlus;
import com.github.teamfusion.spyglassplus.client.config.SpyglassPlusConfig;
import com.github.teamfusion.spyglassplus.client.config.SpyglassPlusConfig.DisplayConfig.DiscoveryHudConfig;
import com.github.teamfusion.spyglassplus.client.entity.LivingEntityClientAccess;
import com.github.teamfusion.spyglassplus.client.event.DiscoveryHudRenderEvent;
import com.github.teamfusion.spyglassplus.enchantment.SpyglassPlusEnchantments;
import com.github.teamfusion.spyglassplus.entity.DiscoveryHudEntitySetup;
import com.github.teamfusion.spyglassplus.entity.ScopingEntity;
import com.github.teamfusion.spyglassplus.entity.SpyglassStandEntity;
import com.github.teamfusion.spyglassplus.mixin.client.EntityMixin;
import com.github.teamfusion.spyglassplus.mixin.client.InGameHudMixin;
import com.github.teamfusion.spyglassplus.tag.SpyglassPlusEntityTypeTags;
import com.github.teamfusion.spyglassplus.world.SpyglassRaycasting;
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
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.StatusEffectSpriteManager;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.tag.TagKey;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.random.Random;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.lang.Math.PI;
import static java.lang.Math.atan;
import static java.lang.Math.max;
import static java.lang.Math.round;
import static net.minecraft.client.gui.screen.ingame.HandledScreen.BACKGROUND_TEXTURE;
import static net.minecraft.util.math.MathHelper.clamp;
import static net.minecraft.util.math.MathHelper.cos;
import static net.minecraft.util.math.MathHelper.floor;
import static net.minecraft.util.math.MathHelper.lerp;

// TODO fix scaling

/**
 * Responsible for rendering the HUD elements created by {@link SpyglassPlusEnchantments#DISCOVERY}.
 * Responsible also for calculating {@link #targetedEntity}, used for {@link SpyglassPlusEnchantments#INDICATE}.
 *
 * @see InGameHudMixin implementation of render
 * @see EntityMixin implementation of targetedEntity
 */
@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class DiscoveryHudRenderer extends DrawableHelper {
    private static DiscoveryHudRenderer INSTANCE;
    public static final Identifier ICONS_TEXTURE = new Identifier(SpyglassPlus.MOD_ID, "textures/gui/discovery_icons.png");

    /**
     * A translation key.
     */
    public static final String
        DOTS_KEY            = translate("dots"),

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
        DOTS_TEXT = Text.translatable(DOTS_KEY),
        BEHAVIOR_ICON = Text.translatable(BEHAVIOR_ICON_KEY).setStyle(DISCOVERY_FONT_STYLE),
        HEALTH_ICON = Text.translatable(HEALTH_ICON_KEY).setStyle(DISCOVERY_FONT_STYLE),
        STRENGTH_ICON = Text.translatable(STRENGTH_ICON_KEY).setStyle(DISCOVERY_FONT_STYLE);

    public static final int
        BOX_WIDTH = 109, BOX_HEIGHT = 124,
        TITLE_BOX_WIDTH = BOX_WIDTH, TITLE_BOX_HEIGHT = 32,
        EYE_WIDTH = 20, EYE_HEIGHT = 16, EYE_PHASES = 5,
        RIGHT_SIDEBAR_TEXT_BORDER_SIZE = 4;

    public static final float
        EYE_BLINK_FREQUENCY = 0.0075F,
        EYE_BLINK_SPEED = 0.15F,
        BLACK_OPACITY = 0.2F;

    public static final EntityDimensions BASE_RENDER_BOX_DIMENSIONS = EntityDimensions.fixed(2.0F, 2.0F);

    private final MinecraftClient client = MinecraftClient.getInstance();
    private final TextRenderer textRenderer = this.client.textRenderer;
    private final Random random = Random.create();

    private int scaledWidth;

    /**
     * The entity currently being rendered.
     */
    protected Entity activeEntity;

    /**
     * The entity at the player's crosshair. May not be equal to {@link #activeEntity}.
     */
    protected Entity targetedEntity;

    /**
     * How open the discovery HUD is, similar to spyglassScale in {@link InGameHud}.
     */
    protected float openProgress;

    /**
     * The progress of a minor trail-off when stopping targeting an entity.
     */
    protected float trailOff;

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
        INSTANCE = this;
    }

    public static DiscoveryHudRenderer getInstance() {
        return INSTANCE;
    }

    /**
     * @see InGameHudMixin
     */
    public static void render(DiscoveryHudRenderer discoveryHud, MatrixStack matrices, float tickDelta, Entity camera) {
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
        this.trailOff = 0.0F;
        this.eyePhase = -0.2F;
    }

    /**
     * @see DiscoveryHudRenderer#render(DiscoveryHudRenderer, MatrixStack, float, Entity)
     */
    public boolean render(MatrixStack matrices, float tickDelta, Entity camera) {
        if (!this.client.options.getPerspective().isFirstPerson() || !(camera instanceof ScopingEntity scopingEntity) || !scopingEntity.isScoping()) {
            this.activeEntity = null;
            this.targetedEntity = null;
            return false;
        }

        this.targetedEntity = SpyglassRaycasting.raycast(camera, this.getRotation(camera, tickDelta), tickDelta);

        if (DiscoveryHudRenderEvent.PRE.invoker().render(this, matrices, tickDelta, camera).isFalse()) {
            return false;
        }

        ItemStack stack = scopingEntity.getScopingStack();
        int level = EnchantmentHelper.getLevel(SpyglassPlusEnchantments.DISCOVERY.get(), stack);
        if (!(level > 0)) {
            return false;
        }

        if (this.activeEntity != null) {
            DiscoveryHudConfig config = SpyglassPlusConfig.get().display.discoveryHud;
            EntityType<?> entityType = this.activeEntity.getType();

            if (!this.client.isPaused()) {
                float lastFrameDuration = this.client.getLastFrameDuration();

                // eye
                if (config.eyeOpens) {
                    float delta = this.eyePhase < 0 ? this.random.nextFloat() * EYE_BLINK_FREQUENCY : 1.0F / (EYE_BLINK_SPEED * 20);
                    this.eyePhase = this.eyePhase + (delta * (this.eyeClosing ? -lastFrameDuration : lastFrameDuration));
                    if (this.eyePhase >= 1.2F) {
                        this.eyeClosing = true;
                    } else if (this.eyePhase <= -0.2F) {
                        this.eyeClosing = false;
                    }
                } else {
                    this.eyePhase = 1.0F;
                    this.eyeClosing = true;
                }

                // opening
                boolean hudShouldOpen = this.hudShouldOpen();
                this.trailOff = hudShouldOpen ? 0.0F : this.trailOff + (0.1F * lastFrameDuration);
                float desiredOpenProgress = hudShouldOpen ? 1.0F : (this.trailOff < 1 && config.trailOff ? 0.8F : 0.0F);
                this.openProgress = config.openWithZoom
                    ? lerp(0.5F * lastFrameDuration, this.openProgress, desiredOpenProgress)
                    : desiredOpenProgress;
            }

            Window window = this.client.getWindow();
            this.scaledWidth = window.getScaledWidth();
            int halfHeight = window.getScaledHeight() / 2;
            int textHeight = this.textRenderer.fontHeight;

            boolean renderStats = !entityType.isIn(SpyglassPlusEntityTypeTags.IGNORE_STATS_DISCOVERY);
            boolean hasRenderBox = this.hasRenderBox(this.activeEntity);
            int boxWidth = hasRenderBox ? BOX_WIDTH : TITLE_BOX_WIDTH;
            int boxHeight = hasRenderBox ? BOX_HEIGHT : TITLE_BOX_HEIGHT;

            int boxTopY = halfHeight - (boxHeight / 2);

            /* Setup */

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            /* Left */

            matrices.push();

            int leftX = 10;
            double centerLeftX = leftX + (boxWidth / 2d);
            matrices.translate(centerLeftX, halfHeight, 0.0D);
            matrices.scale(this.openProgress, this.openProgress, this.openProgress);
            matrices.translate(-centerLeftX, -halfHeight, 0.0D);

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, this.openProgress);

            // box
            RenderSystem.setShaderTexture(0, ICONS_TEXTURE);
            this.drawTexture(matrices, leftX, boxTopY, 0, hasRenderBox ? 0 : BOX_HEIGHT, boxWidth, boxHeight);

            int eyeTextureVOffset = floor(clamp(this.eyePhase, 0.0F, 1.0F) * (EYE_PHASES - 1)) * EYE_HEIGHT;
            this.drawTexture(matrices, (int) (centerLeftX - (EYE_WIDTH / 2d)) + 1, boxTopY + 3, boxWidth, eyeTextureVOffset, EYE_WIDTH, EYE_HEIGHT);

            if (this.openProgress > 0.5F) {
                if (hasRenderBox) {
                    // draw entity
                    int entityX = (int) centerLeftX;
                    int entityY = boxTopY + boxHeight - 18;
                    EntityDimensions entityDimensions = entityType.getDimensions();
                    float scale = entityDimensions.height > BASE_RENDER_BOX_DIMENSIONS.height ? 1 / (entityDimensions.height / BASE_RENDER_BOX_DIMENSIONS.height) : 1;
                    this.drawEntity(entityX, entityY, scale, scale, 30, this.activeEntity);
                }

                // draw entity name
                this.drawTrimmedCentredText(matrices, this.activeEntity.getDisplayName(), 90, (int) (leftX + (boxWidth / 2f)) + 1, (int) (boxTopY + 14 + (textHeight / 2f)) + 1);
            }

            if (renderStats) {
                if (level >= 3) {
                    if (this.activeEntity instanceof LivingEntity livingEntity) {
                        // `effects`
                        List<StatusEffectInstance> effects = ((LivingEntityClientAccess) livingEntity).getEffects()
                                                                                                      .stream()
                                                                                                      .filter(StatusEffectInstance::shouldShowIcon)
                                                                                                      .toList();

                        List<StatusEffectInstance> beneficial = effects.stream().filter(effect -> effect.getEffectType().isBeneficial()).toList();
                        List<StatusEffectInstance> notBeneficial = effects.stream().filter(effect -> !effect.getEffectType().isBeneficial()).toList();

                        int y = boxTopY + BOX_HEIGHT + 1;
                        this.renderStatusEffects(matrices, beneficial, leftX, y, 0);
                        this.renderStatusEffects(matrices, notBeneficial, leftX, y, beneficial.isEmpty() ? 0 : 25);
                    }
                }
            }

            matrices.pop();

            /* Right */

            matrices.push();

            int rightX = 13;
            double centerRightX = this.scaledWidth - rightX;
            matrices.translate(centerRightX, halfHeight, 0.0D);
            matrices.scale(this.openProgress, this.openProgress, this.openProgress);
            matrices.translate(-centerRightX, -halfHeight, 0.0D);

            // stats
            if (renderStats) {
                if (level >= 2) {
                    // behavior
                    Text behaviorText = EntityBehavior.getText(this.activeEntity);
                    if (behaviorText != null) {
                        int behaviorY = halfHeight - (textHeight * 3) - 1;
                        this.drawTextClusterFromRight(matrices, rightX, behaviorY,
                            Text.translatable(BEHAVIOR_KEY, BEHAVIOR_ICON),
                            Text.translatable(BEHAVIOR_HOLDER_KEY, behaviorText, BEHAVIOR_ICON).formatted(Formatting.GRAY)
                        );
                    }
                }

                if (level >= 3) {
                    if (this.activeEntity instanceof LivingEntity livingEntity) {
                        // health
                        float hurt = (float) livingEntity.hurtTime / livingEntity.maxHurtTime;
                        float br = (livingEntity.isDead() ? 1F : (Float.isNaN(hurt) ? 0F : hurt)) * (1 - BLACK_OPACITY);
                        this.drawTextClusterFromRight(matrices, rightX, halfHeight, br + BLACK_OPACITY, BLACK_OPACITY, BLACK_OPACITY,
                            Text.translatable(HEALTH_KEY, HEALTH_ICON),
                            this.createHealthHolderText(HEALTH_HOLDER_KEY, HEALTH_ICON, livingEntity.getHealth())
                        );

                        if (livingEntity.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE) != null) {
                            // strength
                            int strengthY = halfHeight + (textHeight * 3) + 1;
                            this.drawTextClusterFromRight(matrices, rightX, strengthY,
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

            this.trySyncTargetedEntityToActive();
            return true;
        }

        this.trySyncTargetedEntityToActive();
        return false;
    }

    public boolean hudShouldOpen() {
        return this.targetedEntity != null;
    }

    protected void trySyncTargetedEntityToActive() {
        if (this.hudShouldOpen()) {
            this.activeEntity = this.targetedEntity;
        }
    }

    public void renderStatusEffects(MatrixStack matrices, List<StatusEffectInstance> effects, int rawX, int rawY, int yOffset) {
        if (!effects.isEmpty()) {
            int count = effects.size();
            float xOffset = max(6, count > 4 ? (BOX_WIDTH - 3f) / count : 25);

            RenderSystem.enableBlend();

            StatusEffectSpriteManager sprites = this.client.getStatusEffectSpriteManager();
            for (int i = 0, l = effects.size(); i < l; i++) {
                StatusEffectInstance effect = effects.get(i);
                StatusEffect type = effect.getEffectType();

                int duration = effect.getDuration();
                float alpha = 1.0f;
                if (duration <= 200) {
                    int m = 10 - duration / 20;
                    alpha = clamp(duration / 10f / 5 * 0.5f, 0.0f, 0.5f) + (cos((duration * (float) PI) / 5.0f) * clamp((m / 10f) * 0.25f, 0.0f, 0.25f));
                }

                float x = rawX + (xOffset * i);
                int y = rawY + yOffset;

                RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
                this.drawTexture(matrices, x, y, 141, 166, 24, 24);

                Sprite sprite = sprites.getSprite(type);
                RenderSystem.setShaderTexture(0, sprite.getAtlas().getId());
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
                int size = 18;
                this.drawSprite(matrices, x + 3, y + 3, this.getZOffset(), size, size, sprite);

                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            }

            RenderSystem.disableBlend();
        }
    }

    public void drawTexture(MatrixStack matrices, float x, float y, int u, int v, int width, int height) {
        this.drawTexture(matrices, x, y, this.getZOffset(), (float) u, (float) v, width, height, 256, 256);
    }

    public void drawTexture(MatrixStack matrices, float x, float y, float z, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        this.drawTexture(matrices, x, x + width, y, y + height, z, width, height, u, v, textureWidth, textureHeight);
    }

    public void drawTexture(MatrixStack matrices, float x0, float x1, float y0, float y1, float z, int regionWidth, int regionHeight, float u, float v, int textureWidth, int textureHeight) {
        this.drawTexturedQuad(matrices, x0, x1, y0, y1, z, (u + 0.0F) / (float) textureWidth, (u + (float) regionWidth) / (float) textureWidth, (v + 0.0F) / (float) textureHeight, (v + (float) regionHeight) / (float) textureHeight);
    }

    public void drawSprite(MatrixStack matrices, float x, float y, float z, int width, int height, Sprite sprite) {
        this.drawTexturedQuad(matrices, x, x + width, y, y + height, z, sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV());
    }

    public void drawTexturedQuad(MatrixStack matrices, float x0, float x1, float y0, float y1, float z, float u0, float u1, float v0, float v1) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        buffer.vertex(matrix, x0, y1, z).texture(u0, v1).next();
        buffer.vertex(matrix, x1, y1, z).texture(u1, v1).next();
        buffer.vertex(matrix, x1, y0, z).texture(u1, v0).next();
        buffer.vertex(matrix, x0, y0, z).texture(u0, v0).next();
        BufferRenderer.drawWithShader(buffer.end());
    }

    public void drawTrimmedCentredText(MatrixStack matrices, Text text, int maxWidth, int x, int y) {
        List<OrderedText> wrapped = this.textRenderer.wrapLines(text, maxWidth);
        if (wrapped.size() > 1) { // the text has been wrapped
            // calculate dots
            int dotsWidth = this.textRenderer.getWidth(DOTS_TEXT);

            // wrap text again, accounting for dots
            List<OrderedText> newWrapped = this.textRenderer.wrapLines(text, maxWidth - dotsWidth);
            OrderedText orderedText = newWrapped.get(0);
            int textWidth = this.textRenderer.getWidth(orderedText);

            // draw text and dots
            int width = textWidth + dotsWidth;
            this.textRenderer.draw(matrices, orderedText, (int) (x - (width / 2f)), y, 0x000000);
            this.textRenderer.draw(matrices, DOTS_TEXT, (int) (x - (width / 2f) + textWidth), y, 0x000000);
        } else {
            // the text wasn't wrapped, draw normally
            OrderedText orderedText = wrapped.get(0);
            int width = this.textRenderer.getWidth(orderedText);
            this.textRenderer.draw(matrices, orderedText, (int) (x - (width / 2f)), y, 0x000000);
        }
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
        bufferBuilder.vertex(matrix, (float) x1, (float) y2, 0.0F).color(r, g, b, alpha).next();
        bufferBuilder.vertex(matrix, (float) x2, (float) y2, 0.0F).color(r, g, b, alpha).next();
        bufferBuilder.vertex(matrix, (float) x2, (float) y1, 0.0F).color(r, g, b, alpha).next();
        bufferBuilder.vertex(matrix, (float) x1, (float) y1, 0.0F).color(r, g, b, alpha).next();
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
        return String.format("%.1f", 0.5D * round(health));
    }

    /**
     * Whether an entity renders inside of its box.
     */
    public boolean hasRenderBox(Entity entity) {
        return !entity.getType().isIn(SpyglassPlusEntityTypeTags.DO_NOT_RENDER_BOX_DISCOVERY)
            && (!(entity instanceof ItemEntity itemEntity) || !itemEntity.getStack().isEmpty());
    }

    @SuppressWarnings("deprecation")
    public void drawEntity(int x, int y, float xScale, float yScale, int scale, Entity entity) {
        float yawOffset = (float) atan(-300 / 40.0f);
        float pitchOffset = (float) atan(0 / 40.0f);

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

        Entity camera = this.client.getCameraEntity();
        float yaw = entity.getYaw();
        float pitch = entity.getPitch();
        Text customName = entity.getCustomName();

        this.client.cameraEntity = entity;
        float renderYaw = 180.0f + yawOffset * 40.0f;
        float renderPitch = -pitchOffset * 20.0f;
        entity.setYaw(renderYaw);
        entity.setPitch(renderPitch);
        entity.setCustomName(null);

        NbtCompound setupNbt = new NbtCompound();
        if (entity instanceof DiscoveryHudEntitySetup setup) {
            setup.setupBeforeDiscoveryHud(setupNbt, renderYaw, renderPitch, yawOffset, pitchOffset);
        }

        DiffuseLighting.method_34742();
        EntityRenderDispatcher dispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();
        rotationQuaternion.conjugate();
        dispatcher.setRotation(rotationQuaternion);
        dispatcher.setRenderShadows(false);

        VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        RenderSystem.runAsFancy(() -> dispatcher.render(entity, 0.0, 0.0, 0.0, 0.0f, 1.0f, matricesSub, immediate, 0xF000F0));
        immediate.draw();

        dispatcher.setRenderShadows(true);
        this.client.cameraEntity = camera;
        entity.setYaw(yaw);
        entity.setPitch(pitch);
        entity.setCustomName(customName);

        if (entity instanceof DiscoveryHudEntitySetup setup) {
            setup.cleanupAfterDiscoveryHud(setupNbt);
        }

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

    public static String translate(String suffix) {
        return "text.%s.discovery_hud.%s".formatted(SpyglassPlus.MOD_ID, suffix);
    }

    public Entity getActiveEntity() {
        return this.activeEntity;
    }

    public Entity getTargetedEntity() {
        return this.targetedEntity;
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

        public String getTranslationKey() {
            return this.translationKey;
        }

        public boolean matches(EntityType<?> entity) {
            return this.predicate.test(entity);
        }
    }
}
