package com.github.teamfusion.spyglassplus.mixin;

import com.github.teamfusion.spyglassplus.item.ISpyglass;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemMixin {
    /**
     * Makes all instances of {@link ISpyglass} enchantable.
     */
    @Inject(method = "isEnchantable", at = @At("HEAD"), cancellable = true)
    private void fixSpyglassIsEnchantable(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        Item that = (Item) (Object) this;
        if (that instanceof ISpyglass item) cir.setReturnValue(item.isSpyglassEnchantable(stack));
    }

    /**
     * Fix {@link ISpyglass} enchantability.
     */
    @Inject(method = "getEnchantability", at = @At("HEAD"), cancellable = true)
    private void fixSpyglassEnchantability(CallbackInfoReturnable<Integer> cir) {
        Item that = (Item) (Object) this;
        if (that instanceof ISpyglass spyglass) cir.setReturnValue(spyglass.getSpyglassEnchantability());
    }
}
