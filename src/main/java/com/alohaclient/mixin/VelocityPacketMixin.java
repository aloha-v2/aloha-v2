package com.alohaclient.mixin;

import com.alohaclient.module.ModuleManager;
import com.alohaclient.module.combat.Velocity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// In 1.16.5, PvP knockback is delivered through EntityVelocityUpdateS2CPacket
// (not LivingEntity.takeKnockback). Cancelling that handler prevents the
// server-pushed knockback from being applied to the local player.
@Mixin(ClientPlayNetworkHandler.class)
public class VelocityPacketMixin {

    @Inject(method = "onVelocityUpdate", at = @At("HEAD"), cancellable = true)
    private void aloha$onVelocityUpdate(EntityVelocityUpdateS2CPacket packet, CallbackInfo ci) {
        if (!ModuleManager.getInstance().isEnabled("Velocity")) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        if (packet.getId() != mc.player.getEntityId()) return;

        Velocity mod = (Velocity) ModuleManager.getInstance().get("Velocity");
        if (mod == null) return;

        // Multiplier 0.0 → fully cancel; > 0 lets vanilla knockback through.
        if (mod.mult.getValue() <= 0.0) {
            ci.cancel();
        }
    }
}
