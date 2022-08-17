package com.github.teamfusion.spyglassplus.mixin.client;

import com.github.teamfusion.spyglassplus.client.gui.BinocularsOverlayRenderer;
import com.github.teamfusion.spyglassplus.item.SpyglassPlusItems;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("InjectIntoConstructor")
@Environment(EnvType.CLIENT)
@Mixin(ModelLoader.class)
public abstract class ModelLoaderMixin {
    @Shadow protected abstract void addModel(ModelIdentifier modelId);

    /**
     * Adds the inventory model for {@link SpyglassPlusItems#BINOCULARS}.
     */
    @Inject(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/model/ModelLoader;addModel(Lnet/minecraft/client/util/ModelIdentifier;)V",
            ordinal = 3
        )
    )
    private void onInit(ResourceManager manager, BlockColors colors, Profiler profiler, int mipmapLevel, CallbackInfo ci) {
        this.addModel(BinocularsOverlayRenderer.INVENTORY_IN_HAND_MODEL_ID);
    }
}
