package com.github.teamfusion.spyglassplus.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.SpyglassItem;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class CustomSpyglassItem extends SpyglassItem implements ISpyglass {
    public CustomSpyglassItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        this.playUseSound(world, player, hand);
        player.incrementStat(Stats.USED.getOrCreateStat(this));
        return ItemUsage.consumeHeldItem(world, player, hand);
    }

    public void playUseSound(World world, PlayerEntity player, Hand hand) {
        player.playSound(this.getUseSound(), 1.0f, 1.0f);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity entity, int remainingUseTicks) {
        this.playStopUseSound(stack, world, entity, remainingUseTicks);
    }

    public void playStopUseSound(ItemStack stack, World world, LivingEntity entity, int remainingUseTicks) {
        entity.playSound(this.getStopUsingSound(), 1.0f, 1.0f);
    }
}
