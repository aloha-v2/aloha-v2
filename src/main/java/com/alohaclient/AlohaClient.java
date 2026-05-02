package com.alohaclient;

import com.alohaclient.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.option.ChatVisibility;
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

        // Some users have chatVisibility set to HIDDEN/SYSTEM in their
        // options.txt (often unintentionally). The vanilla server then rejects
        // every chat message with "Cannot send chat message". Whenever we join
        // a world, force chatVisibility back to FULL and re-send the client
        // settings packet so the server's stored value stays in sync.
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (client.options == null) return;
            if (client.options.chatVisibility != ChatVisibility.FULL) {
                client.options.chatVisibility = ChatVisibility.FULL;
                client.options.sendClientSettings();
                client.options.write();
                LOGGER.info("[AlohaClient] Reset chatVisibility to FULL on world join.");
            }
        });
    }
}
