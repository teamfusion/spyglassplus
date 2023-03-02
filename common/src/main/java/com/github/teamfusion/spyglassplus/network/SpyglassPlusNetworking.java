package com.github.teamfusion.spyglassplus.network;

import com.github.teamfusion.spyglassplus.SpyglassPlus;
import com.github.teamfusion.spyglassplus.enchantment.SpyglassPlusEnchantments;
import com.github.teamfusion.spyglassplus.entity.ScopingPlayer;
import com.github.teamfusion.spyglassplus.item.ISpyglass;
import com.github.teamfusion.spyglassplus.mixin.access.FoxEntityInvoker;
import com.github.teamfusion.spyglassplus.tag.SpyglassPlusEntityTypeTags;
import com.github.teamfusion.spyglassplus.world.SpyglassRaycasting;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.NetworkManager.PacketContext;
import dev.architectury.networking.NetworkManager.Side;
import io.netty.buffer.Unpooled;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.EntityView;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public interface SpyglassPlusNetworking {
    Identifier
        LOCAL_SCRUTINY_PACKET_ID = new Identifier(SpyglassPlus.MOD_ID, "update_local_scrutiny"),
        COMMAND_TRIGGERED_PACKET_ID = new Identifier(SpyglassPlus.MOD_ID, "command_triggered"),
        EFFECTS_UPDATE_PACKET_ID = new Identifier(SpyglassPlus.MOD_ID, "update_active_effects"),
        INDICATE_UPDATE_PACKET_ID = new Identifier(SpyglassPlus.MOD_ID, "indicate_update"),
        COMMAND_UPDATE_PACKET_ID = new Identifier(SpyglassPlus.MOD_ID, "command_update"),
        COMMAND_TARGETED_PACKET_ID = new Identifier(SpyglassPlus.MOD_ID, "command_targeted");

    static void registerReceivers() {
        NetworkManager.registerReceiver(Side.C2S, LOCAL_SCRUTINY_PACKET_ID, SpyglassPlusNetworking::onLocalScrutinyUpdate);
        NetworkManager.registerReceiver(Side.C2S, COMMAND_TRIGGERED_PACKET_ID, SpyglassPlusNetworking::onCommandTriggered);
    }

    /**
     * Receives a local scrutiny update from the client and updates the server.
     */
    static void onLocalScrutinyUpdate(PacketByteBuf buf, PacketContext context) {
        PlayerEntity player = context.getPlayer();
        ScopingPlayer scopingPlayer = ScopingPlayer.cast(player);
        if (scopingPlayer.isScoping()) {
            ItemStack stack = scopingPlayer.getScopingStack();
            if (stack.getItem() instanceof ISpyglass item) {
                int level = EnchantmentHelper.getLevel(SpyglassPlusEnchantments.SCRUTINY.get(), stack);
                if (level > 0) {
                    int delta = buf.readInt();
                    item.adjustScrutiny(stack, level, delta);
                }
            }
        }
    }

    /**
     * Received when a client triggers {@link SpyglassPlusEnchantments#COMMAND}.
     */
    static void onCommandTriggered(PacketByteBuf buf, PacketContext context) {
        boolean target = buf.readBoolean();

        PlayerEntity player = context.getPlayer();
        ScopingPlayer scopingPlayer = ScopingPlayer.cast(player);
        if (scopingPlayer.isScoping()) {
            ItemStack stack = scopingPlayer.getScopingStack();
            if (EnchantmentHelper.getLevel(SpyglassPlusEnchantments.COMMAND.get(), stack) > 0) {
                if (target) {
                    Entity entity = SpyglassRaycasting.raycast(player, e -> e instanceof MobEntity mobEntity && !isCommandAllyTo(player, mobEntity));
                    if (entity instanceof MobEntity mobEntity) {
                        executeIfCommandAllies(player, mobEntity, player.getBlockPos(), player.getWorld(), entities -> {
                            entities.forEach(e -> e.setTarget(mobEntity));

                            if (player instanceof ServerPlayerEntity serverPlayer) {
                                sendCommandTargeted(mobEntity, serverPlayer);
                            }
                        });
                    }
                } else {
                    executeIfCommandAllies(player, null, player.getBlockPos(), player.getWorld(), entities -> {
                        entities.forEach(e -> e.setTarget(null));
                        if (player instanceof ServerPlayerEntity serverPlayer) {
                            sendCommandTargeted(null, serverPlayer);
                        }
                    });
                }
            }
        }
    }

    static void executeIfCommandAllies(PlayerEntity source, @Nullable MobEntity targeted, BlockPos pos, EntityView world, Consumer<List<MobEntity>> entities) {
        Box box = new Box(pos).expand(64.0D);
        List<MobEntity> nearbyAllies = world.getNonSpectatingEntities(MobEntity.class, box).stream().filter(e -> isCommandAllyTo(source, e)).toList();

        if (!nearbyAllies.isEmpty() && !nearbyAllies.contains(targeted)) {
            entities.accept(nearbyAllies);
        }
    }

    static boolean isCommandAllyTo(PlayerEntity player, MobEntity entity) {
        if (!entity.isAlive()) {
            return false;
        }

        if (entity instanceof TameableEntity tameable) {
            if (tameable.isOwner(player)) {
                return true;
            }
        }

        if (entity instanceof IronGolemEntity golem) {
            if (golem.isPlayerCreated()) {
                return true;
            }
        }

        if (entity instanceof FoxEntity foxEntity) {
            List<UUID> trusted = ((FoxEntityInvoker) foxEntity).invokeGetTrustedUuids();
            if (trusted.contains(player.getUuid())) {
                return true;
            }
        }

        if (entity.getType().isIn(SpyglassPlusEntityTypeTags.CHECKS_NBT_COMMAND)) {
            NbtCompound nbt = new NbtCompound();
            entity.writeCustomDataToNbt(nbt);
            UUID uuid = tryGetUuid(nbt, "Owner").orElseGet(() -> tryGetUuid(nbt, "OwnerUUID").orElse(null));
            //noinspection RedundantIfStatement
            if (uuid != null && uuid.equals(player.getUuid())) {
                return true;
            }
        }

        return false;
    }

    static Optional<UUID> tryGetUuid(NbtCompound nbt, String key) {
        return nbt.containsUuid(key) ? Optional.of(nbt.getUuid(key)) : Optional.empty();
    }

    static void sendCommandTargeted(@Nullable Entity targetedEntity, ServerPlayerEntity player) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(targetedEntity == null ? -1 : targetedEntity.getId());
        NetworkManager.sendToPlayer(player, COMMAND_TARGETED_PACKET_ID, buf);
    }
}
