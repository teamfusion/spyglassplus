package com.github.teamfusion.spyglassplus.mixin.client;

import com.github.teamfusion.spyglassplus.SpyglassPlus;
import com.github.teamfusion.spyglassplus.enchantment.SpyglassPlusEnchantments;
import com.github.teamfusion.spyglassplus.entity.ScopingEntity;
import com.github.teamfusion.spyglassplus.item.BinocularsItem;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.StatusEffectSpriteManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.RaycastContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
@Mixin(InGameHud.class)
public abstract class InGameHudMixin extends DrawableHelper {
    @Unique private static final Identifier BINOCULARS_SCOPE_TEXTURE = new Identifier(SpyglassPlus.MOD_ID, "textures/misc/binoculars_scope.png");
    private static final Identifier SCOPE_GUI_LOCATION = new Identifier(SpyglassPlus.MOD_ID, "textures/gui/scope_gui.png");
    private static final Identifier SCOPE_GUI_ICON_LOCATION = new Identifier(SpyglassPlus.MOD_ID, "textures/gui/scope_gui_icons.png");

    @Shadow @Final private MinecraftClient client;
    @Shadow private int scaledWidth;
    @Shadow private int scaledHeight;

    private static int eyetick;

    private static boolean isEyeBlink = true;
    private static int eyePhase;

    private static int scopeTick;

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
     * Renders relevant elements after the spyglass is rendered.
     */
    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/hud/InGameHud;renderSpyglassOverlay(F)V",
            shift = At.Shift.AFTER
        )
    )
    private void postRenderSpyglassOverlay(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        /*if (this.client.getCameraEntity() instanceof ScopingEntity scoping) {
            ItemStack stack = scoping.getScopingStack();
            int discovery = EnchantmentHelper.getLevel(SpyglassPlusEnchantments.DISCOVERY.get(), stack);
            if (discovery > 0) {
                Window window = this.client.getWindow();
                float aspectRatio = (float) window.getWidth() / window.getHeight();
            }
        }*/

        if (this.client.getCameraEntity() instanceof ScopingEntity scoping) {
            ItemStack stack = scoping.getScopingStack();
            int width = client.getWindow().getScaledWidth();
            int height = client.getWindow().getScaledHeight();
            int leftPos = (int) ((width) * 0.15F);
            int rightPos = (int) ((width) / 1.25F);
            int topPos = (height) / 2;

            double ratio = 16 / 9;

            double newDelta = Math.abs((double) width / (double) height - ratio);

            double guiScale = client.getWindow().getScaleFactor();
            int sidebarWidth = 256;
            float sidebarScale = (float) ((64 * guiScale) / Math.min(sidebarWidth, 64 * guiScale));

            if (eyetick < 30 * 20 * 20) {
                ++eyetick;
            } else {
                if (isEyeBlink) {
                    eyePhase += 1;
                    if (eyePhase == 4) {
                        isEyeBlink = false;
                    }
                } else {
                    eyePhase -= 1;
                    if (eyePhase == 0) {
                        isEyeBlink = true;
                    }
                }

                eyetick = 0;
            }

            if (scoping.isScoping()) {
                if (scopeTick < 2 * 20 * 60) {
                    ++scopeTick;
                }
            } else {
                scopeTick = 0;
            }

            int level = EnchantmentHelper.getLevel(SpyglassPlusEnchantments.DISCOVERY.get(), stack);
            if (level > 0 && sidebarScale > 0) {
                if (newDelta > ratio / 1.25F) {
                    matrices.push();
                    matrices.translate(leftPos, topPos, 0.0D);
                    LivingEntity entity = this.client.player;
                    if (entity != null) {
                        Formatting[] textformatting = new Formatting[]{Formatting.WHITE};

                        MutableText s = Text.translatable(SpyglassPlus.MOD_ID + ".spyglass.info.health").formatted(textformatting);

                        MutableText s2 = Text.literal("(  * " + entity.getHealth() / 2 + ")").formatted(textformatting);

                        /*
                         * right side render start
                         */
                        matrices.push();

                        //set right translate
                        matrices.translate((double) -leftPos + rightPos, 0.0F, 0.0D);
                        matrices.scale(sidebarScale, sidebarScale, sidebarScale);
                        this.client.textRenderer.draw(matrices, s, 20, -70, 0xe0e0e0);
                        this.client.textRenderer.draw(matrices, s2, 20, -60, 0xe0e0e0);
                        RenderSystem.setShader(GameRenderer::getPositionTexShader);
                        RenderSystem.setShaderTexture(0, GUI_ICONS_TEXTURE);

                        renderHeart(matrices, 23, -60, true);
                        renderHeart(matrices, 23, -60, false);

                        //attack damage
                        if (entity.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE) != null) {
                            MutableText s3 = Text.translatable(SpyglassPlus.MOD_ID + ".spyglass.info.damage").formatted(textformatting);

                            MutableText s4 = Text.literal("(  * " + entity.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) / 2 + ")").formatted(textformatting);
                            this.client.textRenderer.draw(matrices, s3, 20, -50, 0xe0e0e0);
                            this.client.textRenderer.draw(matrices, s4, 20, -40, 0xe0e0e0);
                            RenderSystem.setShader(GameRenderer::getPositionTexShader);
                            RenderSystem.setShaderTexture(0, GUI_ICONS_TEXTURE);

                            renderHeart(matrices, 23, -40, true);
                            renderHeart(matrices, 23, -40, false);
                        }

                        if (level > 1) {
                            Collection<StatusEffectInstance> collection = entity.getStatusEffects();

                            if (!collection.isEmpty()) {
                                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                                int k2 = 33;
                                if (collection.size() > 5) {
                                    k2 = 132 / (collection.size() - 1);
                                }


                                Iterable<StatusEffectInstance> iterable = collection.stream().filter(StatusEffectInstance::shouldShowIcon).sorted().collect(Collectors.toList());
                                renderIcons(matrices, 23, -(k2 - 8), 6, iterable);
                            }
                        }

                        //reset
                        matrices.pop();
                        /*
                         * right side render finished
                         */

                        //entity and gui
                        //idk why I should have to double
                        matrices.push();
                        matrices.scale(sidebarScale * 0.75F, sidebarScale * 0.75F, sidebarScale * 0.75F);
                        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
                        RenderSystem.setShaderTexture(0, SCOPE_GUI_LOCATION);
                        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                        this.drawTexture(matrices, -98, -80, 0, 0, 64 * 2, 124 * 2);
                        this.drawTexture(matrices, -66, -80, 128, (32 * eyePhase), 64, 32);
                        matrices.pop();
                        //render entity
                        matrices.push();
                        float entityWidth = entity.getDimensions(entity.getPose()).width;
                        float entityHeight = entity.getDimensions(entity.getPose()).height;

                        renderEntity(leftPos - 25, topPos + 90, (int) (25 / entityWidth), 270.0F, -270.0F, entity);
                        matrices.pop();
                        matrices.scale(sidebarScale, sidebarScale, sidebarScale);
                        this.client.textRenderer.draw(matrices, entity.getDisplayName(), -43, -30, 0x212121);
                    }
                    matrices.pop();

                    RenderSystem.setShaderTexture(0, GUI_ICONS_TEXTURE);
                } else {
                    if (scopeTick < 2 * 20 * 60) {
                        Formatting[] textformatting = new Formatting[]{Formatting.WHITE};

                        MutableText s = Text.translatable(SpyglassPlus.MOD_ID + ".spyglass.info.cannot_render").formatted(textformatting);

                        matrices.push();
                        matrices.translate((double) width / 2, topPos, 0.0D);
                        matrices.scale(0.75F, 0.75F, 0.75F);

                        this.client.textRenderer.draw(matrices, s, -220, -120, 0x212121);
                        matrices.pop();
                    }
                }
            }
        }
    }

    /*
     * This method make not weired look and isometric view
     */
    private void renderEntity(int p_98851_, int p_98852_, int p_98853_, float p_98854_, float p_98855_, LivingEntity p_98856_) {
        float f = (float) Math.atan(p_98854_ / 40.0F);
        float f1 = (float) Math.atan(p_98855_ / 40.0F);
        MatrixStack matrices = RenderSystem.getModelViewStack();
        matrices.push();
        matrices.translate(p_98851_, p_98852_, 1050.0D);
        matrices.scale(1.0F, 1.0F, -1.0F);
        RenderSystem.applyModelViewMatrix();
        MatrixStack posestack1 = new MatrixStack();
        posestack1.translate(0.0D, 0.0D, 1000.0D);
        posestack1.scale((float) p_98853_, (float) p_98853_, (float) p_98853_);
        Quaternion quaternion = Vec3f.POSITIVE_Z.getDegreesQuaternion(180.0F);
        Quaternion quaternion1 = Vec3f.POSITIVE_X.getDegreesQuaternion(f1 * 20.0F);
        quaternion.hamiltonProduct(quaternion1);
        posestack1.multiply(quaternion);
        float f2 = p_98856_.bodyYaw;
        float f3 = p_98856_.getYaw();
        float f4 = p_98856_.getPitch();
        float f5 = p_98856_.headYaw;
        float f6 = p_98856_.prevHeadYaw;
        p_98856_.bodyYaw = 180.0F + f * 20.0F;
        p_98856_.setYaw(180.0F + f * 40.0F);
        p_98856_.setPitch(0.0F);
        p_98856_.headYaw = p_98856_.getYaw();
        p_98856_.prevHeadYaw = p_98856_.getPitch();
        DiffuseLighting.method_34742();
        EntityRenderDispatcher entityrenderdispatcher = this.client.getEntityRenderDispatcher();
        quaternion1.conjugate();
        entityrenderdispatcher.setRotation(quaternion1);
        entityrenderdispatcher.setRenderShadows(false);
        VertexConsumerProvider.Immediate multibuffersource$buffersource = this.client.getBufferBuilders().getEntityVertexConsumers();
        RenderSystem.runAsFancy(() -> {
            entityrenderdispatcher.render(p_98856_, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, posestack1, multibuffersource$buffersource, 15728880);
        });
        multibuffersource$buffersource.draw();
        entityrenderdispatcher.setRenderShadows(true);
        p_98856_.bodyYaw = f2;
        p_98856_.setYaw(f3);
        p_98856_.setPitch(f4);
        p_98856_.headYaw = f5;
        p_98856_.prevHeadYaw = f6;
        matrices.pop();
        RenderSystem.applyModelViewMatrix();
        DiffuseLighting.enableGuiDepthLighting();
    }

    private void renderIcons(MatrixStack matrices, int x, int y, int offsetX, Iterable<StatusEffectInstance> p_194012_) {
        int i = 0;
        StatusEffectSpriteManager manager = this.client.getStatusEffectSpriteManager();
        for (StatusEffectInstance mobeffectinstance : p_194012_) {
            StatusEffect mobeffect = mobeffectinstance.getEffectType();
            Sprite textureatlassprite = manager.getSprite(mobeffect);
            RenderSystem.setShaderTexture(0, textureatlassprite.getAtlas().getId());
            DrawableHelper.drawSprite(matrices, x + 6 + i, y, 0, 18, 18, textureatlassprite);
            i += offsetX;
        }

    }

    private void renderHeart(MatrixStack matrices, int x, int y, boolean isContainer) {
        this.drawTexture(matrices, x, y, 16 + (2 * 2 + (isContainer ? 1 : 0)) * 9, 0, 9, 9);
    }

    private Entity checkEntityWithNoBlockClip(Entity user, double distance) {
        Predicate<Entity> e = entity -> !entity.isSpectator() && entity.isAlive();
        Vec3d eyePos = user.getLerpedPos(1.0F);
        Vec3d lookVec = user.getRotationVector();
        Vec3d distanceVec = eyePos.add(lookVec.multiply(distance));
        Box playerBox = user.getBoundingBox().stretch(lookVec.multiply(distance)).expand(1.0D);

        HitResult hitresult = user.world.raycast(new RaycastContext(eyePos, distanceVec, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, user));

        if (hitresult.getType() != HitResult.Type.MISS) {
            distanceVec = hitresult.getPos();
        }

        EntityHitResult traceResult = ProjectileUtil.getEntityCollision(user.getWorld(), user, eyePos, distanceVec, playerBox, e);

        if (traceResult == null) {
            return null;
        }


        return traceResult.getEntity();
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
