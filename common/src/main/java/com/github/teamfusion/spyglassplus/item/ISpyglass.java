package com.github.teamfusion.spyglassplus.item;

import com.github.teamfusion.spyglassplus.SpyglassPlus;
import com.github.teamfusion.spyglassplus.client.entity.LivingEntityClientAccess;
import com.github.teamfusion.spyglassplus.enchantment.SpyglassPlusEnchantments;
import com.github.teamfusion.spyglassplus.entity.ScopingPlayer;
import com.github.teamfusion.spyglassplus.sound.SpyglassPlusSoundEvents;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface ISpyglass {
    String
        LOCAL_SCRUTINY_LEVEL_KEY = "LocalScrutinyLevel",
        EFFECTS_KEY = "Effects";

    Identifier
        LOCAL_SCRUTINY_PACKET_ID = new Identifier(SpyglassPlus.MOD_ID, "update_local_scrutiny"),
        EFFECTS_UPDATE_PACKET_ID = new Identifier(SpyglassPlus.MOD_ID, "update_active_effects");

    default boolean isSpyglassEnchantable(ItemStack stack) {
        return true;
    }

    default int getSpyglassEnchantability() {
        return 1;
    }

    default SoundEvent getUseSound() {
        return SoundEvents.ITEM_SPYGLASS_USE;
    }

    default SoundEvent getStopUsingSound() {
        return SoundEvents.ITEM_SPYGLASS_STOP_USING;
    }

    default SoundEvent getAdjustSound() {
        return SpyglassPlusSoundEvents.ITEM_SPYGLASS_ADJUST.get();
    }

    default SoundEvent getResetAdjustSound() {
        return SpyglassPlusSoundEvents.ITEM_SPYGLASS_RESET_ADJUST.get();
    }

    /**
     * Modifies the local scrutiny level based on scroll delta.
     */
    default int adjustScrutiny(ItemStack stack, int level, int delta) {
        NbtCompound nbt = stack.getOrCreateNbt();
        int local = nbt.contains(LOCAL_SCRUTINY_LEVEL_KEY) ? nbt.getInt(LOCAL_SCRUTINY_LEVEL_KEY) : level;
        int adjusted = delta == 0 ? level : MathHelper.clamp(local + delta, 0, level);
        nbt.putInt(LOCAL_SCRUTINY_LEVEL_KEY, adjusted);
        return adjusted;
    }

    static boolean hasLocalScrutinyLevel(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        return nbt != null && nbt.contains(LOCAL_SCRUTINY_LEVEL_KEY);
    }

    static int getLocalScrutinyLevel(ItemStack stack) {
        Enchantment enchantment = SpyglassPlusEnchantments.SCRUTINY.get();
        int level = hasLocalScrutinyLevel(stack)
            ? stack.getNbt().getInt(LOCAL_SCRUTINY_LEVEL_KEY)
            : EnchantmentHelper.getLevel(enchantment, stack);
        return MathHelper.clamp(level, 0, enchantment.getMaxLevel());
    }

    /* Events */

    @Environment(EnvType.CLIENT)
    static void appendLocalScrutinyLevelTooltip(ItemStack stack, List<Text> list, TooltipContext context) {
        Item item = stack.getItem();
        if (item instanceof ISpyglass) {
            int level = EnchantmentHelper.getLevel(SpyglassPlusEnchantments.SCRUTINY.get(), stack);
            if (level > 0) {
                int adjustment = level - getLocalScrutinyLevel(stack);
                if (adjustment != 0) {
                    String key = "%s.%s.local_scrutiny_level_tooltip".formatted(item.getTranslationKey(), SpyglassPlus.MOD_ID);
                    list.add(1 + stack.getEnchantments().size(), Text.translatable(key, adjustment).formatted(Formatting.DARK_GRAY));
                }
            }
        }
    }

    /**
     * Receives a local scrutiny update from the client and updates the server.
     */
    static void updateLocalScrutinyFromClient(PacketByteBuf buf, PacketContext context) {
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

    @Environment(EnvType.CLIENT)
    static void updateDiscoveryEntityEffectsFromServer(PacketByteBuf buf, PacketContext context) {
        int id = buf.readInt();
        NbtCompound nbt = buf.readNbt();
        MinecraftClient client = MinecraftClient.getInstance();
        client.execute(() -> {
            if (client.world.getEntityById(id) instanceof LivingEntity livingEntity) {
                NbtList nbtEffects = nbt.getList(EFFECTS_KEY, NbtElement.COMPOUND_TYPE);
                List<StatusEffectInstance> list = new ArrayList<>();
                nbtEffects.stream()
                          .filter(NbtCompound.class::isInstance)
                          .map(NbtCompound.class::cast)
                          .map(StatusEffectInstance::fromNbt)
                          .forEach(list::add);
                ((LivingEntityClientAccess) livingEntity).setEffects(Collections.unmodifiableList(list));
            }
        });
    }
}
