package com.github.teamfusion.spyglassplus.client.render.entity;

import com.github.teamfusion.spyglassplus.SpyglassPlus;
import com.github.teamfusion.spyglassplus.client.model.entity.SpyglassPlusEntityModelLayers;
import com.github.teamfusion.spyglassplus.client.model.entity.SpyglassStandEntityModel;
import com.github.teamfusion.spyglassplus.entity.SpyglassStandEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;

import static java.lang.Math.PI;
import static net.minecraft.util.math.MathHelper.sin;

@Environment(EnvType.CLIENT)
public class SpyglassStandEntityRenderer<T extends SpyglassStandEntity> extends EntityRenderer<T> {
    public static final Identifier TEXTURE = new Identifier(SpyglassPlus.MOD_ID, "textures/entity/spyglass_stand/spyglass_stand.png");

    protected final SpyglassStandEntityModel<T> model, modelSmall;
    protected final SpyglassStandEntityModel<T> spyglassModel, spyglassModelSmall;

    public SpyglassStandEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.model = new SpyglassStandEntityModel<>(context.getPart(SpyglassPlusEntityModelLayers.SPYGLASS_STAND));
        this.modelSmall = new SpyglassStandEntityModel<>(context.getPart(SpyglassPlusEntityModelLayers.SPYGLASS_STAND_SMALL));
        this.spyglassModel = new SpyglassStandEntityModel<>(context.getPart(SpyglassPlusEntityModelLayers.SPYGLASS_STAND_SPYGLASS));
        this.spyglassModelSmall = new SpyglassStandEntityModel<>(context.getPart(SpyglassPlusEntityModelLayers.SPYGLASS_STAND_SPYGLASS_SMALL));
    }

    @Override
    public void render(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light) {
        MinecraftClient client = MinecraftClient.getInstance();
        boolean visible = !entity.isInvisible();
        boolean invisible = !visible && !entity.isInvisibleTo(client.player);
        boolean outline = client.hasOutline(entity);

        float alpha = invisible ? 0.15f : 1.0f;

        matrices.push();

        matrices.scale(-1.0f, -1.0f, 1.0f);
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-180f));

        float shake = (float) (entity.world.getTime() - entity.getLastHitTime()) + tickDelta;
        if (shake < 5.0f) {
            matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(sin((shake / 1.5f) * (float) PI) * 3.0f));
        }

        matrices.translate(0.0, -1.501f, 0.0);

        // render model
        RenderLayer layer = this.getRenderLayer(entity, TEXTURE, visible, invisible, outline);
        this.render(this.getModel(entity, false), layer, alpha, entity, tickDelta, matrices, vertices, light);

        // render spyglass, with glint conditionally
        SpyglassStandEntityModel<T> spyglassModel = this.getModel(entity, true);
        this.render(spyglassModel, layer, alpha, entity, tickDelta, matrices, vertices, light);
        if (entity.getSpyglassStack().hasEnchantments()) {
            this.render(spyglassModel, RenderLayer.getEntityGlint(), alpha, entity, tickDelta, matrices, vertices, light);
        }

        matrices.pop();

        if (this.hasLabel(entity)) {
            this.renderLabelIfPresent(entity, entity.getDisplayName(), matrices, vertices, light);
        }
    }

    public void render(SpyglassStandEntityModel<T> model, RenderLayer layer, float alpha, T entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light) {
        if (layer == null) {
            return;
        }

        model.riding = entity.hasVehicle();
        model.child = entity.isSmall();

        model.animateModel(entity, 0.0f, 0.0f, tickDelta);
        model.setAngles(entity, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);

        model.render(matrices, vertices.getBuffer(layer), light, OverlayTexture.DEFAULT_UV, 1.0f, 1.0f, 1.0f, alpha);
    }

    @Override
    protected boolean hasLabel(T entity) {
        double distance = this.dispatcher.getSquaredDistanceToCamera(entity);
        float bound = entity.isInSneakingPose() ? 32.0f : 64.0f;
        return !(distance >= (double) (bound * bound)) && entity.isCustomNameVisible();
    }

    public SpyglassStandEntityModel<T> getModel(T entity, boolean spyglass) {
        return entity.isSmall() ? (spyglass ? this.spyglassModelSmall : this.modelSmall) : (spyglass ? this.spyglassModel : this.model);
    }

    public RenderLayer getRenderLayer(T entity, Identifier texture, boolean showBody, boolean translucent, boolean showOutline) {
        if (!entity.isMarker()) {
            if (translucent) return RenderLayer.getItemEntityTranslucentCull(texture);
            if (showBody) return this.getModel(entity, false).getLayer(texture);
            if (showOutline) return RenderLayer.getOutline(texture);
        } else {
            if (translucent) return RenderLayer.getEntityTranslucent(texture, false);
            if (showBody) return RenderLayer.getEntityCutoutNoCull(texture, false);
        }

        return null;
    }

    @Override
    public Identifier getTexture(T entity) {
        return TEXTURE;
    }
}
