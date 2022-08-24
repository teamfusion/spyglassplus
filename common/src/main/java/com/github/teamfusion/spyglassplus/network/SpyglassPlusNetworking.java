package com.github.teamfusion.spyglassplus.network;

import com.github.teamfusion.spyglassplus.SpyglassPlus;
import com.github.teamfusion.spyglassplus.enchantment.SpyglassPlusEnchantments;
import com.github.teamfusion.spyglassplus.entity.ScopingPlayer;
import com.github.teamfusion.spyglassplus.item.ISpyglass;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.NetworkManager.Side;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public interface SpyglassPlusNetworking {
    Identifier
        LOCAL_SCRUTINY_PACKET_ID = new Identifier(SpyglassPlus.MOD_ID, "update_local_scrutiny"),
        EFFECTS_UPDATE_PACKET_ID = new Identifier(SpyglassPlus.MOD_ID, "update_active_effects"),
        INDICATE_UPDATE_PACKET_ID = new Identifier(SpyglassPlus.MOD_ID, "indicate_update_packet_id");

    static void registerReceivers() {
        NetworkManager.registerReceiver(Side.C2S, LOCAL_SCRUTINY_PACKET_ID, SpyglassPlusNetworking::onLocalScrutinyUpdate);
    }

    /**
     * Receives a local scrutiny update from the client and updates the server.
     */
    static void onLocalScrutinyUpdate(PacketByteBuf buf, NetworkManager.PacketContext context) {
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
}
