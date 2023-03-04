package com.github.teamfusion.spyglassplus.datagen;

import com.github.teamfusion.spyglassplus.SpyglassPlus;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public final class SpyglassPlusDataGenerator implements SpyglassPlus, DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator gen) {
        FabricDataGenerator.Pack pack = gen.createPack();
        pack.addProvider(ModelProvider::new);
        pack.addProvider(ItemTagProvider::new);
        pack.addProvider(EntityTypeTagProvider::new);
    }
}
