package com.github.teamfusion.spyglassplus.datagen;

import com.github.teamfusion.spyglassplus.SpyglassPlus;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;

public final class SpyglassPlusDataGenerator implements SpyglassPlus, DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator gen) {
        gen.addProvider(ModelProvider::new);

        FabricTagProvider.BlockTagProvider blockTagProvider = null;
        gen.addProvider(g -> new ItemTagProvider(g, blockTagProvider));
        gen.addProvider(EntityTypeTagProvider::new);
    }
}
