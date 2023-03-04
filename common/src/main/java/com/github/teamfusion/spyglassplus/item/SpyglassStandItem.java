package com.github.teamfusion.spyglassplus.item;

import com.github.teamfusion.spyglassplus.entity.SpyglassPlusEntityType;
import com.github.teamfusion.spyglassplus.entity.SpyglassStandEntity;
import com.github.teamfusion.spyglassplus.sound.SpyglassPlusSoundEvents;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class SpyglassStandItem extends Item {
    public SpyglassStandItem(Settings settings) {
        super(settings);
        DispenserBlock.registerBehavior(this, new SpyglassStandDispenserBehavior());
    }

    public static boolean isSmall(ItemStack stack) {
        NbtCompound nbtEntityTag = stack.getSubNbt(EntityType.ENTITY_TAG_KEY);
        return nbtEntityTag != null && nbtEntityTag.getBoolean(SpyglassStandEntity.SMALL_KEY);
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        String key = super.getTranslationKey(stack);
        return isSmall(stack) ? key + ".small" : key;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext usage) {
        if (usage.getSide() == Direction.DOWN) {
            return ActionResult.FAIL;
        }

        EntityType<SpyglassStandEntity> type = SpyglassPlusEntityType.SPYGLASS_STAND.get();
        ItemPlacementContext placement = new ItemPlacementContext(usage);
        BlockPos pos = placement.getBlockPos();

        World world = usage.getWorld();
        Vec3d vec = Vec3d.ofBottomCenter(pos);
        Box box = type.getDimensions().getBoxAt(vec.getX(), vec.getY(), vec.getZ());
        if (!world.isSpaceEmpty(null, box) || !world.getOtherEntities(null, box).isEmpty()) {
            return ActionResult.FAIL;
        }

        ItemStack stack = usage.getStack();
        if (world instanceof ServerWorld serverWorld) {
            SpyglassStandEntity entity = type.create(serverWorld, stack.getNbt(), null, pos, SpawnReason.SPAWN_EGG, true, true);
            if (entity == null) {
                return ActionResult.FAIL;
            }

            float yaw = (float) MathHelper.floor((MathHelper.wrapDegrees(usage.getPlayerYaw()) + 22.5f) / 45.0f) * 45.0f;
            entity.setSpyglassYaw(yaw);
            entity.refreshPositionAndAngles(entity.getX(), entity.getY(), entity.getZ(), yaw, 0.0f);
            serverWorld.spawnEntityAndPassengers(entity);

            world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SpyglassPlusSoundEvents.ENTITY_SPYGLASS_STAND_PLACE.get(), SoundCategory.BLOCKS, 0.75f, 0.8f);
            entity.emitGameEvent(GameEvent.ENTITY_PLACE, usage.getPlayer());
        }
        stack.decrement(1);
        return ActionResult.success(world.isClient);
    }

    public static class SpyglassStandDispenserBehavior extends ItemDispenserBehavior {
        @Override
        public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
            Direction direction = pointer.getBlockState().get(DispenserBlock.FACING);
            BlockPos pos = pointer.getPos().offset(direction);
            ServerWorld world = pointer.getWorld();
            SpyglassStandEntity entity = new SpyglassStandEntity(world, (double) pos.getX() + 0.5, pos.getY(), (double) pos.getZ() + 0.5);
            EntityType.loadFromEntityNbt(world, null, entity, stack.getNbt());
            entity.setYaw(direction.asRotation());
            world.spawnEntity(entity);
            stack.decrement(1);
            return stack;
        }
    }
}
