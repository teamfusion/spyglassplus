package com.github.teamfusion.spyglassplus.datagen;

import com.github.teamfusion.spyglassplus.item.SpyglassPlusItems;
import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Models;
import net.moddingplayground.frame.api.toymaker.v0.model.uploader.ItemModelUploader;

import java.util.stream.Stream;

public final class ModelProvider extends FabricModelProvider {
    public ModelProvider(FabricDataGenerator gen) {
        super(gen);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator gen) {
    }

    @Override
    public void generateItemModels(ItemModelGenerator gen) {
        ItemModelUploader uploader = ItemModelUploader.of(gen);

        Stream.of(
            SpyglassPlusItems.BINOCULARS
        ).map(RegistrySupplier::get).forEach(item -> uploader.register(Models.GENERATED, item));

        gen.register(SpyglassPlusItems.SPYGLASS_STAND.get(), "_small", Models.GENERATED);
    }
}
