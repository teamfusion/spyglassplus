package com.github.teamfusion.spyglassplus.client.model.entity;

import com.github.teamfusion.spyglassplus.entity.SpyglassStandEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;

import static com.github.teamfusion.spyglassplus.util.MathUtil.DEGREES_TO_RADIANS;
import static net.minecraft.client.render.entity.model.EntityModelPartNames.LEFT_LEG;
import static net.minecraft.client.render.entity.model.EntityModelPartNames.RIGHT_LEG;

@SuppressWarnings({ "unused", "FieldCanBeLocal" })
@Environment(EnvType.CLIENT)
public class SpyglassStandEntityModel<T extends SpyglassStandEntity> extends SinglePartEntityModel<T> {
    private static final String
        HOLDER = "holder",
        SPYGLASS = "spyglass",
        TRIPOD = "tripod",
        BACK_LEG = "back_leg";

    private final ModelPart
        root,
        holder,
        spyglass,
        tripod,
        leftLeg,
        rightLeg,
        backLeg;

    public SpyglassStandEntityModel(ModelPart root) {
        this.root = root;

        this.holder = root.getChild(HOLDER);
        this.spyglass = this.holder.getChild(SPYGLASS);

        this.tripod = root.getChild(TRIPOD);
        this.leftLeg = this.tripod.getChild(LEFT_LEG);
        this.rightLeg = this.tripod.getChild(RIGHT_LEG);
        this.backLeg = this.tripod.getChild(BACK_LEG);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();

        ModelPartData holder = root.addChild(
            HOLDER,
            ModelPartBuilder.create()
                            .uv(0, 0)
                            .cuboid(-2.0F, -4.0F, 0.0F, 4.0F, 4.0F, 1.0F),
            ModelTransform.pivot(0.0F, -2.0F, 0.0F)
        );

        ModelPartData spyglass = holder.addChild(SPYGLASS, ModelPartBuilder.create(), ModelTransform.NONE);

        ModelPartData tripod = root.addChild(
            TRIPOD,
            ModelPartBuilder.create(),
            ModelTransform.pivot(0.0F, 24.0F, 0.0F)
        );

        ModelPartData leftLeg = tripod.addChild(
            LEFT_LEG,
            ModelPartBuilder.create()
                            .uv(0, 26)
                            .cuboid(7.0F, -24.5F, 0.0F, 2.0F, 27.0F, 2.0F),
            ModelTransform.rotation(-0.2094F, 0.6196F, -0.3752F)
        );

        ModelPartData rightLeg = tripod.addChild(
            RIGHT_LEG,
            ModelPartBuilder.create()
                            .uv(0, 26)
                            .cuboid(-9.0F, -24.5F, 0.0F, 2.0F, 27.0F, 2.0F),
            ModelTransform.rotation(-0.2094F, -0.6196F, 0.3752F)
        );

        ModelPartData backLeg = tripod.addChild(
            BACK_LEG,
            ModelPartBuilder.create()
                            .uv(0, 26)
                            .cuboid(-1.0F, -24.5F, 6.5F, 2.0F, 27.0F, 2.0F),
            ModelTransform.rotation(0.2618F, 0.0F, 0.0F)
        );

        return TexturedModelData.of(data, 64, 64);
    }

    public static TexturedModelData getSmallTexturedModelData() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();

        ModelPartData holder = root.addChild(
            HOLDER,
            ModelPartBuilder.create()
                            .uv(0, 0)
                            .cuboid(-2.0F, -4.0F, 0.0F, 4.0F, 4.0F, 1.0F),
            ModelTransform.pivot(0.0F, 15.0F, 0.0F)
        );

        ModelPartData spyglass = holder.addChild(SPYGLASS, ModelPartBuilder.create(), ModelTransform.NONE);

        ModelPartData tripod = root.addChild(
            TRIPOD,
            ModelPartBuilder.create(),
            ModelTransform.of(0.0F, 41.0F, 0.0F, 0.0F, 0.0F, 0.0F)
        );

        ModelPartData leftLeg = tripod.addChild(
            LEFT_LEG,
            ModelPartBuilder.create()
                            .uv(0, 26)
                            .cuboid(7.0F, -24.5F, 0.0F, 2.0F, 9.0F, 2.0F),
            ModelTransform.rotation(-0.2094F, 0.6196F, -0.3752F)
        );

        ModelPartData rightLeg = tripod.addChild(
            RIGHT_LEG,
            ModelPartBuilder.create()
                            .uv(0, 26)
                            .cuboid(-9.0F, -24.5F, 0.0F, 2.0F, 9.0F, 2.0F),
            ModelTransform.rotation(-0.2094F, -0.6196F, 0.3752F)
        );

        ModelPartData backLeg = tripod.addChild(
            BACK_LEG,
            ModelPartBuilder.create()
                            .uv(0, 26)
                            .cuboid(-1.0F, -24.5F, 6.0F, 2.0F, 9.0F, 2.0F),
            ModelTransform.rotation(0.2618F, 0.0F, 0.0F)
        );

        return TexturedModelData.of(data, 64, 64);
    }

    public static TexturedModelData getSpyglassTexturedModelData() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();

        ModelPartData holder = root.addChild(HOLDER, ModelPartBuilder.create(), ModelTransform.pivot(0.0F, -2.0F, 0.0F));

        ModelPartData spyglass = holder.addChild(
            SPYGLASS,
            ModelPartBuilder.create()
                            .uv(0, 0)
                            .cuboid(-1.0F, -1.0F, -6.5F, 2.0F, 2.0F, 11.0F)
                            .uv(5, 18)
                            .cuboid(-1.0F, -1.0F, -6.5F, 2.0F, 2.0F, 6.0F, new Dilation(0.2F)),
            ModelTransform.pivot(0.0F, -2.0F, 0.5F)
        );

        ModelPartData tripod = root.addChild(TRIPOD, ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

        ModelPartData leftLeg = tripod.addChild(LEFT_LEG, ModelPartBuilder.create(), ModelTransform.rotation(-0.2094F, 0.6196F, -0.3752F));
        ModelPartData rightLeg = tripod.addChild(RIGHT_LEG, ModelPartBuilder.create(), ModelTransform.rotation(-0.2094F, -0.6196F, 0.3752F));
        ModelPartData backLeg = tripod.addChild(BACK_LEG, ModelPartBuilder.create(), ModelTransform.rotation(0.2618F, 0.0F, 0.0F));

        return TexturedModelData.of(data, 64, 64);
    }

    public static TexturedModelData getSpyglassSmallTexturedModelData() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();

        ModelPartData holder = root.addChild(HOLDER, ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 15.0F, 0.0F));

        ModelPartData spyglass = holder.addChild(
            SPYGLASS,
            ModelPartBuilder.create()
                            .uv(0, 0)
                            .cuboid(-1.0F, -1.0F, -6.5F, 2.0F, 2.0F, 11.0F)
                            .uv(5, 18)
                            .cuboid(-1.0F, -1.0F, -6.5F, 2.0F, 2.0F, 6.0F, new Dilation(0.2F)),
            ModelTransform.pivot(0.0F, -2.0F, 0.0F)
        );

        ModelPartData tripod = root.addChild(TRIPOD, ModelPartBuilder.create(), ModelTransform.of(0.0F, 41.0F, 0.0F, 0.0F, 0.0F, 0.0F));

        ModelPartData leftLeg = tripod.addChild(LEFT_LEG, ModelPartBuilder.create(), ModelTransform.rotation(-0.2094F, 0.6196F, -0.3752F));
        ModelPartData rightLeg = tripod.addChild(RIGHT_LEG, ModelPartBuilder.create(), ModelTransform.rotation(-0.2094F, -0.6196F, 0.3752F));
        ModelPartData backLeg = tripod.addChild(BACK_LEG, ModelPartBuilder.create(), ModelTransform.rotation(0.2618F, 0.0F, 0.0F));

        return TexturedModelData.of(data, 64, 64);
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        this.spyglass.visible = entity.hasSpyglassStack();
    }

    @Override
    public void animateModel(T entity, float limbAngle, float limbDistance, float tickDelta) {
        this.tripod.yaw = entity.getYaw(tickDelta) * DEGREES_TO_RADIANS;
        this.holder.yaw = entity.getSpyglassYaw(tickDelta) * DEGREES_TO_RADIANS;
        this.spyglass.pitch = entity.getSpyglassPitch(tickDelta) * DEGREES_TO_RADIANS;
    }

    @Override
    public ModelPart getPart() {
        return this.root;
    }
}
