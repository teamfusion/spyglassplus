package com.github.teamfusion.spyglassplus.datagen;

import com.github.teamfusion.spyglassplus.item.SpyglassPlusItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Models;

public final class ModelProvider extends FabricModelProvider {
    public ModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator gen) {
    }

    @Override
    public void generateItemModels(ItemModelGenerator gen) {
        gen.register(SpyglassPlusItems.BINOCULARS.get(), Models.GENERATED);
        gen.register(SpyglassPlusItems.SPYGLASS_STAND.get(), "_small", Models.GENERATED);
    }
}
