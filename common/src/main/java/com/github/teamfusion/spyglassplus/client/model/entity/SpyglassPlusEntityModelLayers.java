package com.github.teamfusion.spyglassplus.client.model.entity;

import com.github.teamfusion.spyglassplus.SpyglassPlus;
import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public interface SpyglassPlusEntityModelLayers {
    EntityModelLayer SPYGLASS_STAND                = registerSpyglass("main", SpyglassStandEntityModel::getTexturedModelData);
    EntityModelLayer SPYGLASS_STAND_SMALL          = registerSpyglass("small", SpyglassStandEntityModel::getSmallTexturedModelData);

    EntityModelLayer SPYGLASS_STAND_SPYGLASS       = registerSpyglass("spyglass", SpyglassStandEntityModel::getSpyglassTexturedModelData);
    EntityModelLayer SPYGLASS_STAND_SPYGLASS_SMALL = registerSpyglass("spyglass_small", SpyglassStandEntityModel::getSpyglassSmallTexturedModelData);

    private static EntityModelLayer register(String id, String name, Supplier<TexturedModelData> provider) {
        EntityModelLayer layer = new EntityModelLayer(new Identifier(SpyglassPlus.MOD_ID, id), name);
        EntityModelLayerRegistry.register(layer, provider);
        return layer;
    }

    private static EntityModelLayer registerSpyglass(String id, Supplier<TexturedModelData> provider) {
        return register("spyglass", id, provider);
    }

    private static EntityModelLayer main(String id, Supplier<TexturedModelData> provider) {
        return register(id, "main", provider);
    }
}
