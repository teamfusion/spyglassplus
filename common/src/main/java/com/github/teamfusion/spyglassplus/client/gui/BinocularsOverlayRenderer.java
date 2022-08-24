package com.github.teamfusion.spyglassplus.client.gui;

import com.github.teamfusion.spyglassplus.SpyglassPlus;
import com.github.teamfusion.spyglassplus.item.BinocularsItem;
import com.github.teamfusion.spyglassplus.item.SpyglassPlusItems;
import com.github.teamfusion.spyglassplus.mixin.client.InGameHudMixin;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

/**
 * Responsible for rendering the HUD overlay for {@link SpyglassPlusItems#BINOCULARS}.
 *
 * @see InGameHudMixin
 */
@Environment(EnvType.CLIENT)
public class BinocularsOverlayRenderer {
    public static final Identifier BINOCULARS_SCOPE_TEXTURE = new Identifier(SpyglassPlus.MOD_ID, "textures/misc/binoculars_scope.png");

    public static final ModelIdentifier INVENTORY_MODEL_ID = new ModelIdentifier(new Identifier(SpyglassPlus.MOD_ID, "binoculars"), "inventory");
    public static final ModelIdentifier INVENTORY_IN_HAND_MODEL_ID = new ModelIdentifier(new Identifier(SpyglassPlus.MOD_ID, "binoculars_in_hand"), "inventory");

    public BinocularsOverlayRenderer() {
    }

    /**
     * Modifies inventory-based binocular renders to the correct model in mixins.
     */
    public static BakedModel modifyRenderItem(ItemStack stack, ModelTransformation.Mode mode) {
        boolean isInventory = mode == ModelTransformation.Mode.GUI || mode == ModelTransformation.Mode.GROUND || mode == ModelTransformation.Mode.FIXED;
        if (isInventory && stack.getItem() instanceof BinocularsItem) {
            BakedModelManager models = MinecraftClient.getInstance().getBakedModelManager();
            return models.getModel(INVENTORY_MODEL_ID);
        }

        return null;
    }

    public void render(float scale, int scaledWidth, int scaledHeight) {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, BINOCULARS_SCOPE_TEXTURE);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        float ws;
        float hs = ws = (float) Math.min(scaledWidth, scaledHeight);
        float h = Math.min((float) scaledWidth / ws, (float) scaledHeight / hs) * scale;
        float hori = ws * h * 1.82F;
        float vert = hs * h * 0.95F;
        float left = ((float) scaledWidth - hori) / 2.0f;
        float top = ((float) scaledHeight - vert) / 2.0f;
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
        buffer.vertex(0.0, scaledHeight, -90.0).color(0, 0, 0, 255).next();
        buffer.vertex(scaledWidth, scaledHeight, -90.0).color(0, 0, 0, 255).next();
        buffer.vertex(scaledWidth, bottom, -90.0).color(0, 0, 0, 255).next();
        buffer.vertex(0.0, bottom, -90.0).color(0, 0, 0, 255).next();
        buffer.vertex(0.0, top, -90.0).color(0, 0, 0, 255).next();
        buffer.vertex(scaledWidth, top, -90.0).color(0, 0, 0, 255).next();
        buffer.vertex(scaledWidth, 0.0, -90.0).color(0, 0, 0, 255).next();
        buffer.vertex(0.0, 0.0, -90.0).color(0, 0, 0, 255).next();
        buffer.vertex(0.0, bottom, -90.0).color(0, 0, 0, 255).next();
        buffer.vertex(left, bottom, -90.0).color(0, 0, 0, 255).next();
        buffer.vertex(left, top, -90.0).color(0, 0, 0, 255).next();
        buffer.vertex(0.0, top, -90.0).color(0, 0, 0, 255).next();
        buffer.vertex(right, bottom, -90.0).color(0, 0, 0, 255).next();
        buffer.vertex(scaledWidth, bottom, -90.0).color(0, 0, 0, 255).next();
        buffer.vertex(scaledWidth, top, -90.0).color(0, 0, 0, 255).next();
        buffer.vertex(right, top, -90.0).color(0, 0, 0, 255).next();
        tessellator.draw();
        RenderSystem.enableTexture();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
