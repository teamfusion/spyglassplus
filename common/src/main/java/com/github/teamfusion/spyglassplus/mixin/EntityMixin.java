package com.github.teamfusion.spyglassplus.mixin;

import com.github.teamfusion.spyglassplus.enchantment.SpyglassPlusEnchantments;
import com.github.teamfusion.spyglassplus.entity.ScopingEntity;
import com.github.teamfusion.spyglassplus.entity.SpyglassStandEntity;
import com.github.teamfusion.spyglassplus.item.ISpyglass;
import com.github.teamfusion.spyglassplus.util.CommonPlayerLookup;
import com.github.teamfusion.spyglassplus.world.SpyglassRaycasting;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.github.teamfusion.spyglassplus.network.SpyglassPlusNetworking.*;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow public World world;
    @Shadow public abstract BlockPos getBlockPos();

    @Unique private int lastIndicateEntityId = -1;
    @Unique private int commandTicks;

    /**
     * Tracks and updates entities this is indicating.
     */
    @Inject(method = "tick", at = @At("TAIL"))
    private void tickIndicate(CallbackInfo ci) {
        if (this.world.isClient) {
            return;
        }

        Entity that = (Entity) (Object) this;
        if (that instanceof ScopingEntity scoping && !(that instanceof SpyglassStandEntity)) {
            if (scoping.isScoping()) {
                ItemStack stack = scoping.getScopingStack();
                if (stack.hasEnchantments()) {
                    if (EnchantmentHelper.getLevel(SpyglassPlusEnchantments.INDICATE.get(), stack) > 0) {
                        Entity entity = SpyglassRaycasting.raycast(that);
                        this.sendIndicateUpdate(entity, CommonPlayerLookup.tracking(that));
                    }

                    if (EnchantmentHelper.getLevel(SpyglassPlusEnchantments.COMMAND.get(), stack) > 0) {
                        Entity entity = SpyglassRaycasting.raycast(that, entityx -> entityx instanceof MobEntity mobEntity && !this.isAlly(mobEntity));
                        if (entity != null) {
                            this.commandTicks++;

                            if (this.commandTicks > ISpyglass.MAX_COMMAND_TICKS) {
                                if (entity instanceof MobEntity target) {
                                    Box box = new Box(this.getBlockPos()).expand(64.0D);
                                    List<MobEntity> nearbyAllies = this.world.getNonSpectatingEntities(MobEntity.class, box).stream().filter(this::isAlly).toList();

                                    if (!nearbyAllies.isEmpty() && !nearbyAllies.contains(target)) {
                                        nearbyAllies.forEach(entityx -> entityx.setTarget(target));

                                        if (that instanceof ServerPlayerEntity player) {
                                            this.sendCommandTargeted(target, player);
                                        }
                                    }
                                }

                                this.commandTicks = 0;
                            }
                        } else {
                            this.commandTicks = 0;
                        }

                        if (that instanceof ServerPlayerEntity player) {
                            this.sendCommandUpdate(entity, this.commandTicks, player);
                        }
                    }
                }
            } else {
                if (this.lastIndicateEntityId != -1) {
                    this.sendIndicateUpdate(CommonPlayerLookup.tracking(that), -1);
                }

                this.commandTicks = 0;
            }
        }
    }

    @Unique
    private boolean isAlly(MobEntity entity) {
        if (!entity.isAlive()) {
            return false;
        }

        if (entity instanceof TameableEntity tameable) {
            Entity that = (Entity) (Object) this;
            if (that instanceof LivingEntity livingEntity) {
                if (tameable.isOwner(livingEntity)) {
                    return true;
                }
            }
        }

        if (entity instanceof IronGolemEntity golem) {
            //noinspection RedundantIfStatement
            if (golem.isPlayerCreated()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Sends indicated entity to new trackers.
     */
    @Inject(method = "onStartedTrackingBy", at = @At("TAIL"))
    private void onOnStartedTrackingBy(ServerPlayerEntity player, CallbackInfo ci) {
        this.sendIndicateUpdate(Collections.singleton(player), this.lastIndicateEntityId);
    }

    /**
     * Removes indicated entity from old trackers.
     */
    @Inject(method = "onStoppedTrackingBy", at = @At("TAIL"))
    private void onOnStoppedTrackingBy(ServerPlayerEntity player, CallbackInfo ci) {
        this.sendIndicateUpdate(Collections.singleton(player), -1);
    }

    @Unique
    private int sendIndicateUpdate(@Nullable Entity entity, Collection<ServerPlayerEntity> tracking) {
        int id = entity == null ? -1 : entity.getId();
        if (this.lastIndicateEntityId != id) {
            this.sendIndicateUpdate(tracking, id);
        }
        return id;
    }

    @Unique
    private void sendIndicateUpdate(Collection<ServerPlayerEntity> tracking, int id) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(this.lastIndicateEntityId);
        buf.writeInt(id);
        NetworkManager.sendToPlayers(tracking, INDICATE_UPDATE_PACKET_ID, buf);

        this.lastIndicateEntityId = id;
    }

    @Unique
    private void sendCommandUpdate(@Nullable Entity entity, int commandTicks, ServerPlayerEntity player) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(entity == null ? -1 : entity.getId());
        buf.writeInt(commandTicks);
        NetworkManager.sendToPlayer(player, COMMAND_UPDATE_PACKET_ID, buf);
    }

    @Unique
    private void sendCommandTargeted(@NotNull Entity targetedEntity, ServerPlayerEntity player) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(targetedEntity.getId());
        NetworkManager.sendToPlayer(player, COMMAND_TARGETED_PACKET_ID, buf);
    }
}
