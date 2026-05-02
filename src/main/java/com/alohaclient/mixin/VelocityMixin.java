package com.alohaclient.mixin;

import com.alohaclient.module.ModuleManager;
import com.alohaclient.module.combat.Velocity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// takeKnockback is declared in LivingEntity (not PlayerEntity) in yarn 1.16.5.
// We guard with a local-player check so this only affects the client's own player.
@Mixin(LivingEntity.class)
public class VelocityMixin {

    @Inject(method = "takeKnockback(FDD)V", at = @At("HEAD"), cancellable = true)
    private void onTakeKnockback(float strength, double x, double z, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        // Only intercept knockback for the local player
        if (mc.player == null || !((LivingEntity) (Object) this == mc.player)) return;

        if (!ModuleManager.getInstance().isEnabled("Velocity")) return;

        Velocity mod = (Velocity) ModuleManager.getInstance().get("Velocity");
        if (mod == null) return;

        // Multiplier 0.0 → fully cancel; any value > 0 allows vanilla to run
        if (mod.mult.getValue() <= 0.0) {
            ci.cancel();
        }
        // Partial reduction (0 < mult < 1) would require @ModifyArg on the strength arg.
    }
}
