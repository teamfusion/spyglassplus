package com.github.teamfusion.spyglassplus.item;

import com.github.teamfusion.spyglassplus.sound.SpyglassPlusSoundEvents;
import net.minecraft.item.Item;
import net.minecraft.sound.SoundEvent;

public class BinocularsItem extends CustomSpyglassItem {
    public BinocularsItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public SoundEvent getUseSound() {
        return SpyglassPlusSoundEvents.ITEM_BINOCULARS_USE.get();
    }

    @Override
    public SoundEvent getStopUsingSound() {
        return SpyglassPlusSoundEvents.ITEM_BINOCULARS_STOP_USING.get();
    }

    @Override
    public SoundEvent getAdjustSound() {
        return SpyglassPlusSoundEvents.ITEM_BINOCULARS_ADJUST.get();
    }

    @Override
    public SoundEvent getResetAdjustSound() {
        return SpyglassPlusSoundEvents.ITEM_BINOCULARS_RESET_ADJUST.get();
    }
}
