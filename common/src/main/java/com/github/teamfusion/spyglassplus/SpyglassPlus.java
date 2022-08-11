package com.github.teamfusion.spyglassplus;

import com.github.teamfusion.spyglassplus.client.SpyglassPlusClient;
import com.github.teamfusion.spyglassplus.enchantment.SpyglassPlusEnchantments;
import com.github.teamfusion.spyglassplus.enchantment.target.SpyglassPlusEnchantmentTargets;
import com.github.teamfusion.spyglassplus.entity.SpyglassPlusEntityType;
import com.github.teamfusion.spyglassplus.item.ISpyglass;
import com.github.teamfusion.spyglassplus.item.SpyglassPlusItems;
import com.github.teamfusion.spyglassplus.sound.SpyglassPlusSoundEvents;
import com.google.common.reflect.Reflection;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.NetworkManager.Side;
import dev.architectury.utils.EnvExecutor;
import net.fabricmc.api.EnvType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("UnstableApiUsage")
public interface SpyglassPlus {
    String MOD_ID = "spyglassplus";
    String MOD_NAME = "Spyglass+";
    Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    static void commonInitialize() {
        LOGGER.info("Initializing {}", MOD_NAME);

        Reflection.initialize(SpyglassPlusEnchantmentTargets.class);

        SpyglassPlusItems.REGISTER.register();
        SpyglassPlusEntityType.REGISTER.register();
        SpyglassPlusEnchantments.REGISTER.register();
        SpyglassPlusSoundEvents.REGISTER.register();

        SpyglassPlusEntityType.postRegister();

        NetworkManager.registerReceiver(Side.C2S, ISpyglass.UPDATE_LOCAL_SCRUTINY_PACKET, ISpyglass::updateLocalScrutinyServer);
        EnvExecutor.runInEnv(EnvType.CLIENT, () -> SpyglassPlusClient::commonClientInitialize);
    }
}
