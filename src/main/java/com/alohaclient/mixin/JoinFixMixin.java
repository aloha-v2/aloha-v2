package com.alohaclient.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.option.ChatVisibility;
import net.minecraft.client.option.GameOptions;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Forces ChatVisibility back to FULL whenever the local player joins a world.
 *
 * Some users have {@code chatVisibility:hidden} or {@code system} stuck in
 * their {@code options.txt}; the vanilla server then refuses every chat
 * message with "Cannot send chat message". After this mixin fires once on
 * world join, the value is normalized in-memory, the updated client settings
 * are sent to the server and the change is persisted to disk.
 *
 * Lives in a vanilla mixin (no fabric-api dependency) so that the mod runs
 * even on installations that don't ship Fabric API.
 */
@Mixin(ClientPlayNetworkHandler.class)
public class JoinFixMixin {

    @Inject(method = "onGameJoin", at = @At("TAIL"))
    private void aloha$onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        GameOptions     opt = mc.options;
        if (opt == null) return;

        if (opt.chatVisibility != ChatVisibility.FULL) {
            opt.chatVisibility = ChatVisibility.FULL;
            opt.sendClientSettings();
            opt.write();
        }
    }
}
