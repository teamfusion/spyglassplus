package com.github.teamfusion.spyglassplus.mixin.client;

import com.github.teamfusion.spyglassplus.client.gui.BinocularsOverlayRenderer;
import com.github.teamfusion.spyglassplus.item.BinocularsItem;
import com.github.teamfusion.spyglassplus.item.SpyglassPlusItems;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(ItemModels.class)
public abstract class ItemModelsMixin {
    @Shadow public abstract BakedModelManager getModelManager();

    /**
     * Replaces the inventory model for {@link SpyglassPlusItems#BINOCULARS}.
     */
    @Inject(
        method = "getModel(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/client/render/model/BakedModel;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onGetModel(ItemStack stack, CallbackInfoReturnable<BakedModel> cir) {
        if (stack.getItem() instanceof BinocularsItem) {
            BakedModelManager models = this.getModelManager();
            cir.setReturnValue(models.getModel(BinocularsOverlayRenderer.INVENTORY_IN_HAND_MODEL_ID));
        }
    }
}
