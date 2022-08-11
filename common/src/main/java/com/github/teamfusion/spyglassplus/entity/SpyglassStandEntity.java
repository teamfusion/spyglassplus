package com.github.teamfusion.spyglassplus.entity;

import com.github.teamfusion.spyglassplus.item.ISpyglass;
import com.github.teamfusion.spyglassplus.item.SpyglassPlusItems;
import com.github.teamfusion.spyglassplus.sound.SpyglassPlusSoundEvents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity.Type;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public class SpyglassStandEntity extends LivingEntity implements ScopingEntity {
    public static final Predicate<Entity> RIDEABLE_MINECART_PREDICATE =
        entity -> entity instanceof AbstractMinecartEntity minecart && minecart.getMinecartType() == Type.RIDEABLE;

    public static final String
        SMALL_KEY = "Small",
        MARKER_KEY = "Marker",
        INVISIBLE_KEY = "Invisible",
        SPYGLASS_STACK_KEY = "Spyglass",
        USER_KEY = "User",
        SPYGLASS_ROTATION_KEY = "SpyglassRotation";

    public static final EntityDimensions
        MARKER_DIMENSIONS = new EntityDimensions(0.0f, 0.0f, false),
        SMALL_DIMENSIONS = SpyglassPlusEntityType.SPYGLASS_STAND.get().getDimensions().scaled(0.5f);

    /* Tracked Data */

    public static final TrackedData<Boolean>
        SMALL = registerDataTracker(TrackedDataHandlerRegistry.BOOLEAN),
        MARKER = registerDataTracker(TrackedDataHandlerRegistry.BOOLEAN);

    public static final TrackedData<ItemStack> SPYGLASS_STACK = registerDataTracker(TrackedDataHandlerRegistry.ITEM_STACK);
    public static final TrackedData<Optional<UUID>> USER = registerDataTracker(TrackedDataHandlerRegistry.OPTIONAL_UUID);
    public static final TrackedData<Float> SPYGLASS_YAW = registerDataTracker(TrackedDataHandlerRegistry.FLOAT);
    public static final TrackedData<Float> SPYGLASS_PITCH = registerDataTracker(TrackedDataHandlerRegistry.FLOAT);

    protected long lastHitTime;
    protected boolean invisible;

    /**
     * Rotation for user render.
     */
    protected float
        spyglassYaw, spyglassPitch,
        prevSpyglassYaw, prevSpyglassPitch;

    public SpyglassStandEntity(EntityType<? extends LivingEntity> type, World world) {
        super(type, world);
        this.stepHeight = 0.0f;
    }

    public SpyglassStandEntity(World world, double x, double y, double z) {
        this(SpyglassPlusEntityType.SPYGLASS_STAND.get(), world);
        this.setPosition(x, y, z);
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        ItemStack spyglassStack = this.getSpyglassStack();
        if (spyglassStack.isEmpty()) {
            ItemStack stack = player.getStackInHand(hand);
            if (stack.getItem() instanceof ISpyglass spyglass) { // add spyglass stack
                this.setSpyglassStack(stack.copy());
                stack.decrement(1);
                this.playSound(spyglass.getUseSound(), 1.0F, 1.0F);
                return ActionResult.SUCCESS;
            } else if (player.isSneaking()) { // toggle spyglass stand size
                boolean small = !this.isSmall();
                this.setSmall(small);
                this.playSound((small ? SpyglassPlusSoundEvents.ENTITY_SPYGLASS_STAND_SHRINK : SpyglassPlusSoundEvents.ENTITY_SPYGLASS_STAND_ENLARGE).get(), 1.0F, 1.0F);
                return ActionResult.SUCCESS;
            }
        } else {
            if (spyglassStack.getItem() instanceof ISpyglass spyglass) {
                if (player.isSneaking()) { // remove spyglass stack
                    this.dropStack(spyglassStack);
                    this.setSpyglassStack(ItemStack.EMPTY);

                    this.playSound(spyglass.getStopUsingSound(), 1.0F, 1.0F);

                    return ActionResult.SUCCESS;
                } else if (this.getUser().isEmpty()) { // use spyglass stand
                    if (this.isWithinUseRange(player)) {
                        if (this.world.isClient) this.useSpyglassClient(player);
                        this.useSpyglass(player, spyglass);
                        return ActionResult.CONSUME;
                    }
                }
            }
        }

        return super.interact(player, hand);
    }

    @Override
    public void tickMovement() {
        this.prevSpyglassYaw = this.spyglassYaw;
        this.prevSpyglassPitch = this.spyglassPitch;

        super.tickMovement();

        this.getUserPlayer().ifPresent(this::tickUser);
    }

    /**
     * Runs on the using player every tick.
     */
    public void tickUser(PlayerEntity player) {
        ItemStack spyglassStack = this.getSpyglassStack();
        Item spyglassItem = spyglassStack.getItem();

        if (player.isSneaking() || !this.isWithinUseRange(player) || this.doesNotMatch(player)
            || spyglassStack.isEmpty() || !(spyglassItem instanceof ISpyglass)
        ) {
            if (this.world.isClient) this.stopUsingSpyglassClient(player);
            this.stopUsingSpyglass(player, spyglassItem instanceof ISpyglass spyglass ? spyglass : null);
            return;
        }

        // calculate rotations
        float bound = 30;
        float yaw = this.getYaw();
        float pitch = this.getPitch();
        float spyglassYaw = MathHelper.clamp(player.getYaw(), yaw - bound, yaw + bound);
        float spyglassPitch = MathHelper.clamp(player.getPitch(), pitch - bound, pitch + bound);

        // update spyglass stand
        this.setSpyglassYaw(spyglassYaw);
        this.setSpyglassPitch(spyglassPitch);

        // update player
        player.setYaw(spyglassYaw);
        player.setPitch(spyglassPitch);

        // update client
        this.spyglassYaw = spyglassYaw;
        this.spyglassPitch = spyglassPitch;
    }

    /**
     * Attaches a player to this spyglass stand.
     */
    public void useSpyglass(PlayerEntity player, ISpyglass spyglass) {
        ScopingPlayer scopingPlayer = ScopingPlayer.cast(player);
        scopingPlayer.setSpyglassStandEntity(this);
        this.setUserPlayer(player);

        player.setYaw(this.getSpyglassYaw());
        player.setPitch(this.getSpyglassPitch());

        player.setVelocity(Vec3d.ZERO);

        this.playSound(spyglass.getUseSound(), 1.0F, 1.0F);
    }

    @Environment(EnvType.CLIENT)
    public void useSpyglassClient(PlayerEntity player) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == player) client.setCameraEntity(this);
    }

    /**
     * Detaches a player from this spyglass stand.
     */
    public void stopUsingSpyglass(PlayerEntity player, @Nullable ISpyglass spyglass) {
        ScopingPlayer scopingPlayer = ScopingPlayer.cast(player);
        scopingPlayer.setSpyglassStand(null);
        this.setUser(null);

        this.playSound(spyglass != null ? spyglass.getStopUsingSound() : SoundEvents.ITEM_SPYGLASS_STOP_USING, 1.0F, 1.0F);
    }

    @Environment(EnvType.CLIENT)
    public void stopUsingSpyglassClient(PlayerEntity player) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == player) client.setCameraEntity(player);
    }

    public boolean isWithinUseRange(Entity entity) {
        return entity.distanceTo(this) <= 3.0D;
    }

    public boolean doesNotMatch(PlayerEntity player) {
        ScopingPlayer scopingPlayer = ScopingPlayer.cast(player);
        return scopingPlayer.getSpyglassStandEntity().orElse(null) != this;
    }

    @Override
    public ItemStack getScopingStack() {
        return this.getSpyglassStack();
    }

    @Override
    public boolean isScoping() {
        return true;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (this.world.isClient || this.isRemoved()) return false;

        if (DamageSource.OUT_OF_WORLD.equals(source)) {
            this.kill();
            return false;
        }

        if (this.isInvulnerableTo(source) || this.isInvisible() || this.isMarker()) return false;

        if (source.isExplosive()) {
            this.onBreak(source);
            this.kill();
            return false;
        }

        if (DamageSource.IN_FIRE.equals(source)) {
            if (this.isOnFire()) {
                this.updateHealth(source, 0.15f);
            } else this.setOnFireFor(5);
            return false;
        }

        if (DamageSource.ON_FIRE.equals(source) && this.getHealth() > 0.5f) {
            this.updateHealth(source, 4.0f);
            return false;
        }

        boolean isProjectile = source.getSource() instanceof PersistentProjectileEntity;
        boolean pierces = isProjectile && ((PersistentProjectileEntity)source.getSource()).getPierceLevel() > 0;
        boolean isPlayer = "player".equals(source.getName());
        if (!isPlayer && !isProjectile) {
            return false;
        }
        if (source.getAttacker() instanceof PlayerEntity && !((PlayerEntity)source.getAttacker()).getAbilities().allowModifyWorld) {
            return false;
        }
        if (source.isSourceCreativePlayer()) {
            this.playBreakSound();
            this.spawnBreakParticles();
            this.kill();
            return pierces;
        }

        long time = this.world.getTime();
        if (time - this.lastHitTime <= 5L || isProjectile) {
            this.breakAndDropItem(source);
            this.spawnBreakParticles();
            this.kill();
        } else {
            this.world.sendEntityStatus(this, EntityStatuses.HIT_ARMOR_STAND);
            this.emitGameEvent(GameEvent.ENTITY_DAMAGE, source.getAttacker());
            this.lastHitTime = time;
        }

        return true;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void handleStatus(byte status) {
        if (status == EntityStatuses.HIT_ARMOR_STAND) {
            if (this.world.isClient) {
                this.world.playSound(this.getX(), this.getY(), this.getZ(), SpyglassPlusSoundEvents.ENTITY_SPYGLASS_STAND_HIT.get(), this.getSoundCategory(), 0.3f, 1.0f, false);
                this.lastHitTime = this.world.getTime();
            }
        } else super.handleStatus(status);
    }

    @Override
    protected float turnHead(float bodyRotation, float headRotation) {
        this.prevBodyYaw = this.prevYaw;
        this.bodyYaw = this.getYaw();
        return 0.0f;
    }

    @Override
    protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
        return dimensions.height * (this.isBaby() ? 0.5f : 0.9f);
    }

    @Override
    public double getHeightOffset() {
        return this.isMarker() ? 0.0 : (double)0.1f;
    }

    @Override
    public void travel(Vec3d input) {
        if (this.canClip()) super.travel(input);
    }

    protected boolean canClip() {
        return !this.isMarker() && !this.hasNoGravity();
    }

    @Override
    public float getYaw(float tickDelta) {
        if (tickDelta == 1.0f) return this.getYaw();
        return MathHelper.lerp(tickDelta, this.prevYaw, this.getYaw());
    }

    @Override
    public void setBodyYaw(float bodyYaw) {
        this.prevBodyYaw = this.prevYaw = bodyYaw;
        this.prevHeadYaw = this.headYaw = bodyYaw;
    }

    @Override
    public void setHeadYaw(float headYaw) {
        this.prevBodyYaw = this.prevYaw = headYaw;
        this.prevHeadYaw = this.headYaw = headYaw;
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        if (SMALL.equals(data)) this.calculateDimensions();
        if (MARKER.equals(data)) this.intersectionChecked = !this.isMarker();
        super.onTrackedDataSet(data);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SpyglassPlusSoundEvents.ENTITY_SPYGLASS_STAND_HIT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SpyglassPlusSoundEvents.ENTITY_SPYGLASS_STAND_BREAK.get();
    }

    @Override
    public void onStruckByLightning(ServerWorld world, LightningEntity entity) {
    }

    @Override
    public boolean isAffectedBySplashPotions() {
        return false;
    }

    @Override
    public boolean isMobOrPlayer() {
        return false;
    }

    @Override
    public EntityDimensions getDimensions(EntityPose pose) {
        return this.getDimensions(this.isMarker());
    }

    public EntityDimensions getDimensions(boolean marker) {
        return marker ? MARKER_DIMENSIONS : this.isBaby() ? SMALL_DIMENSIONS : this.getType().getDimensions();
    }

    @Override
    public Vec3d getClientCameraPosVec(float tickDelta) {
        if (this.isMarker()) {
            Box box = this.getDimensions(false).getBoxAt(this.getPos());
            BlockPos pos = this.getBlockPos();
            int i = Integer.MIN_VALUE;
            for (BlockPos posx : BlockPos.iterate(new BlockPos(box.minX, box.minY, box.minZ), new BlockPos(box.maxX, box.maxY, box.maxZ))) {
                int j = Math.max(this.world.getLightLevel(LightType.BLOCK, posx), this.world.getLightLevel(LightType.SKY, posx));
                if (j == 15) return Vec3d.ofCenter(posx);
                if (j <= i) continue;
                i = j;
                pos = posx.toImmutable();
            }
            return Vec3d.ofCenter(pos);
        }
        return super.getClientCameraPosVec(tickDelta);
    }

    @Override
    public ItemStack getPickBlockStack() {
        ItemStack stack = this.getSpyglassStack();
        return stack.isEmpty() ? new ItemStack(SpyglassPlusItems.SPYGLASS_STAND.get()) : stack.copy();
    }

    @Override
    public boolean isPartOfGame() {
        return !this.isInvisible() && !this.isMarker();
    }

    public void onBreak(DamageSource source) {
        this.playBreakSound();
        this.drop(source);
    }

    public void breakAndDropItem(DamageSource source) {
        Block.dropStack(this.world, this.getBlockPos(), new ItemStack(SpyglassPlusItems.SPYGLASS_STAND.get()));
        this.onBreak(source);
    }

    @Override
    protected void dropInventory() {
        this.dropStack(this.getSpyglassStack());
    }

    protected void updateHealth(DamageSource source, float amount) {
        float health = this.getHealth();
        if ((health -= amount) <= 0.5f) {
            this.onBreak(source);
            this.kill();
        } else {
            this.setHealth(health);
            this.emitGameEvent(GameEvent.ENTITY_DAMAGE, source.getAttacker());
        }
    }

    public void playBreakSound() {
        this.world.playSound(null, this.getX(), this.getY(), this.getZ(), SpyglassPlusSoundEvents.ENTITY_SPYGLASS_STAND_BREAK.get(), this.getSoundCategory(), 1.0f, 1.0f);
    }

    public void spawnBreakParticles() {
        if (this.world instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(new ItemStackParticleEffect(ParticleTypes.ITEM, new ItemStack(SpyglassPlusItems.SPYGLASS_STAND.get())), this.getX(), this.getBodyY(0.6D), this.getZ(), 10, this.getWidth() / 4.0f, this.getHeight() / 4.0f, this.getWidth() / 4.0f, 0.05);
        }
    }

    @Override
    public void kill() {
        this.remove(RemovalReason.KILLED);
        this.emitGameEvent(GameEvent.ENTITY_DIE);
    }

    @Override
    public boolean handleAttack(Entity attacker) {
        return attacker instanceof PlayerEntity player && !this.world.canPlayerModifyAt(player, this.getBlockPos());
    }

    @Override
    public boolean isBaby() {
        return this.isSmall();
    }

    @Override
    public boolean isImmuneToExplosion() {
        return this.isInvisible();
    }

    @Override
    public FallSounds getFallSounds() {
        return new FallSounds(SpyglassPlusSoundEvents.ENTITY_SPYGLASS_STAND_FALL.get(), SpyglassPlusSoundEvents.ENTITY_SPYGLASS_STAND_FALL.get());
    }

    @Override
    public PistonBehavior getPistonBehavior() {
        return this.isMarker() ? PistonBehavior.IGNORE : super.getPistonBehavior();
    }

    @Override
    public void calculateDimensions() {
        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();
        super.calculateDimensions();
        this.setPosition(x, y, z);
    }

    @Override
    public boolean canMoveVoluntarily() {
        return super.canMoveVoluntarily() && this.canClip();
    }

    @Override
    public boolean canHit() {
        return super.canHit() && !this.isMarker();
    }

    @Override
    protected void tickCramming() {
        List<Entity> list = this.world.getOtherEntities(this, this.getBoundingBox(), RIDEABLE_MINECART_PREDICATE);
        for (Entity entity : list) if (this.squaredDistanceTo(entity) <= 0.2) entity.pushAwayFrom(this);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void pushAway(Entity entity) {
    }

    /* Getters / Setters */

    public boolean isSmall() {
        return this.dataTracker.get(SMALL);
    }

    public void setSmall(boolean small) {
        this.dataTracker.set(SMALL, small);
    }

    public boolean isMarker() {
        return this.dataTracker.get(MARKER);
    }

    public void setMarker(boolean marker) {
        this.dataTracker.set(MARKER, marker);
    }

    public boolean hasSpyglassStack() {
        return !this.getSpyglassStack().isEmpty();
    }

    public ItemStack getSpyglassStack() {
        return this.dataTracker.get(SPYGLASS_STACK);
    }

    public void setSpyglassStack(ItemStack stack) {
        this.dataTracker.set(SPYGLASS_STACK, stack);
    }

    public void setUser(UUID user) {
        this.dataTracker.set(USER, Optional.ofNullable(user));
    }

    public void setUserPlayer(PlayerEntity player) {
        this.dataTracker.set(USER, Optional.of(player).map(PlayerEntity::getUuid));
    }

    public Optional<UUID> getUser() {
        return this.dataTracker.get(USER);
    }

    public Optional<PlayerEntity> getUserPlayer() {
        return this.getUser().map(this.world::getPlayerByUuid);
    }

    public float getSpyglassYaw() {
        return this.dataTracker.get(SPYGLASS_YAW);
    }

    @Environment(EnvType.CLIENT)
    public float getSpyglassYaw(float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (this.doesNotMatch(client.player)) return this.getSpyglassYaw();

        if (tickDelta == 1.0f) return this.spyglassYaw;
        return MathHelper.lerp(tickDelta, this.prevSpyglassYaw, this.spyglassYaw);
    }

    public void setSpyglassYaw(float yaw) {
        this.dataTracker.set(SPYGLASS_YAW, yaw);
    }

    public float getSpyglassPitch() {
        return this.dataTracker.get(SPYGLASS_PITCH);
    }

    @Environment(EnvType.CLIENT)
    public float getSpyglassPitch(float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (this.doesNotMatch(client.player)) return this.getSpyglassPitch();

        if (tickDelta == 1.0f) return this.spyglassPitch;
        return MathHelper.lerp(tickDelta, this.prevSpyglassPitch, this.spyglassPitch);
    }

    public void setSpyglassPitch(float pitch) {
        this.dataTracker.set(SPYGLASS_PITCH, pitch);
    }

    @Override
    protected void updatePotionVisibility() {
        this.setInvisible(this.invisible);
    }

    @Override
    public void setInvisible(boolean invisible) {
        this.invisible = invisible;
        super.setInvisible(invisible);
    }

    public long getLastHitTime() {
        return this.lastHitTime;
    }

    /* Living Entity */

    @Override
    public Iterable<ItemStack> getArmorItems() {
        return Collections.emptyList();
    }

    @Override
    public ItemStack getEquippedStack(EquipmentSlot slot) {
        return this.getSpyglassStack();
    }

    @Override
    public void equipStack(EquipmentSlot slot, ItemStack stack) {
        this.setSpyglassStack(stack);
    }

    @Override
    public Arm getMainArm() {
        return Arm.RIGHT;
    }

    /* Data */

    protected static <T> TrackedData<T> registerDataTracker(TrackedDataHandler<T> handler) {
        return DataTracker.registerData(SpyglassStandEntity.class, handler);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(SMALL, false);
        this.dataTracker.startTracking(MARKER, false);
        this.dataTracker.startTracking(SPYGLASS_STACK, ItemStack.EMPTY);
        this.dataTracker.startTracking(USER, Optional.empty());
        this.dataTracker.startTracking(SPYGLASS_YAW, 0.0f);
        this.dataTracker.startTracking(SPYGLASS_PITCH, 0.0f);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);

        nbt.putBoolean(SMALL_KEY, this.isSmall());
        nbt.putBoolean(MARKER_KEY, this.isMarker());
        nbt.putBoolean(INVISIBLE_KEY, this.isInvisible());
        nbt.put(SPYGLASS_STACK_KEY, this.getSpyglassStack().writeNbt(new NbtCompound()));
        this.getUser().ifPresent(uuid -> nbt.putUuid(USER_KEY, uuid));
        nbt.put(SPYGLASS_ROTATION_KEY, this.toNbtList(this.getSpyglassYaw(), this.getSpyglassPitch()));
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);

        this.setSmall(nbt.getBoolean(SMALL_KEY));
        this.setMarker(nbt.getBoolean(MARKER_KEY));
        this.setInvisible(nbt.getBoolean(INVISIBLE_KEY));
        this.setSpyglassStack(ItemStack.fromNbt(nbt.getCompound(SPYGLASS_STACK_KEY)));

        NbtList spyglassRotation = nbt.getList(SPYGLASS_ROTATION_KEY, NbtElement.FLOAT_TYPE);
        this.setSpyglassYaw(spyglassRotation.getFloat(0));
        this.setSpyglassPitch(spyglassRotation.getFloat(1));
    }
}
