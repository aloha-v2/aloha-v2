package com.alohaclient.mixin;

import com.alohaclient.module.ModuleManager;
import com.alohaclient.module.movement.Speed;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class PlayerTickMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void onPlayerTick(CallbackInfo ci) {
        ClientPlayerEntity self = (ClientPlayerEntity) (Object) this;

        handleSpider(self);
        handleSpeed(self);
        handleNoFall(self);
    }

    // ── Spider ────────────────────────────────────────────────────────────────
    private void handleSpider(ClientPlayerEntity self) {
        if (!ModuleManager.getInstance().isEnabled("Spider")) return;
        if (self.abilities.flying) return;
        if (!self.horizontalCollision) return;

        Vec3d vel = self.getVelocity();
        double ny = vel.y < 0 ? 0 : vel.y;
        if (self.input.movementForward > 0.5f || self.input.jumping) ny = 0.2;
        self.setVelocity(vel.x, ny, vel.z);
    }

    // ── Speed ─────────────────────────────────────────────────────────────────
    private void handleSpeed(ClientPlayerEntity self) {
        if (!ModuleManager.getInstance().isEnabled("Speed")) return;
        if (self.abilities.flying) return;
        if (!self.isOnGround()) return;

        Speed mod = (Speed) ModuleManager.getInstance().get("Speed");
        if (mod == null) return;

        Vec3d vel = self.getVelocity();
        if (Math.abs(vel.x) < 0.01 && Math.abs(vel.z) < 0.01) return;

        double mult = mod.multiplier.getValue();
        self.setVelocity(vel.x * mult, vel.y, vel.z * mult);
    }

    // ── NoFall ────────────────────────────────────────────────────────────────
    private void handleNoFall(ClientPlayerEntity self) {
        if (!ModuleManager.getInstance().isEnabled("NoFall")) return;
        self.fallDistance = 0;
    }
}
