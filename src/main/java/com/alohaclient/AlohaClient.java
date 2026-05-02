package com.alohaclient;

import com.alohaclient.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class AlohaClient implements ClientModInitializer {

    public static final String MOD_ID   = "alohaclient";
    public static final String MOD_NAME = "AlohaClient";
    public static final Logger LOGGER   = LogManager.getLogger(MOD_NAME);

    @Override
    public void onInitializeClient() {
        LOGGER.info("[AlohaClient] Initialized! Modules: {}",
                ModuleManager.getInstance().getModules().size());
        // Chat-visibility recovery is handled by JoinFixMixin (vanilla mixin,
        // no fabric-api event needed).
    }
}
