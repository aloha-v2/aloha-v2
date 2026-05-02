package com.alohaclient.mixin;

import com.alohaclient.module.ModuleManager;
import com.alohaclient.module.combat.KillAura;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class ClientTickMixin {

    private int kaTimer = 0;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        MinecraftClient mc = (MinecraftClient) (Object) this;
        if (mc.player == null || mc.world == null) return;

        handleKillAura(mc);
        handleTrails(mc);
        handleFullBright(mc);
    }

    // ── KillAura ──────────────────────────────────────────────────────────────
    private void handleKillAura(MinecraftClient mc) {
        if (!ModuleManager.getInstance().isEnabled("KillAura")) return;

        KillAura ka = (KillAura) ModuleManager.getInstance().get("KillAura");
        if (ka == null) return;

        int ticksPerHit = Math.max(1, (int) Math.ceil(20.0 / ka.cps.getValue()));
        kaTimer++;
        if (kaTimer < ticksPerHit) return;

        double range   = ka.range.getValue();
        double rangeSq = range * range;

        Entity target  = null;
        double closest = rangeSq + 0.1;

        // getEntities() (no-arg) is the ClientWorld API in 1.16.5
        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof LivingEntity)) continue;
            if (e instanceof PlayerEntity && !ka.attackPlayers.getValue()) continue;
            LivingEntity le = (LivingEntity) e;
            if (le.isDead() || le.getHealth() <= 0) continue;
            double d = mc.player.squaredDistanceTo(e);
            if (d < closest) { closest = d; target = e; }
        }

        if (target == null) return;
        kaTimer = 0;

        // Rotations
        if (ka.rotations.getValue()) {
            double dx   = target.getX() - mc.player.getX();
            double dy   = target.getEyeY() - mc.player.getEyeY();
            double dz   = target.getZ() - mc.player.getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);
            float  yaw   = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
            float  pitch = (float) (-Math.toDegrees(Math.atan2(dy, dist)));
            // yarn 1.16.5: yaw/pitch are public fields on Entity
            mc.player.yaw   = yaw;
            mc.player.pitch = pitch;
        }

        // Criticals: give an upward nudge if on ground
        if (ModuleManager.getInstance().isEnabled("Criticals") && mc.player.isOnGround()) {
            Vec3d v = mc.player.getVelocity();
            mc.player.setVelocity(v.x, 0.42, v.z);
        }

        // Attack
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    // ── Trails ────────────────────────────────────────────────────────────────
    private void handleTrails(MinecraftClient mc) {
        if (!ModuleManager.getInstance().isEnabled("Trails")) return;
        Vec3d vel = mc.player.getVelocity();
        if (Math.abs(vel.x) < 0.05 && Math.abs(vel.z) < 0.05) return;

        double x = mc.player.getX() + (Math.random() - 0.5) * 0.4;
        double y = mc.player.getY() + 0.1;
        double z = mc.player.getZ() + (Math.random() - 0.5) * 0.4;
        mc.world.addParticle(ParticleTypes.PORTAL, x, y, z, 0, 0.05, 0);

        if (Math.random() < 0.4) {
            mc.world.addParticle(ParticleTypes.END_ROD,
                mc.player.getX(), mc.player.getY() + 0.05, mc.player.getZ(),
                (Math.random() - 0.5) * 0.1, 0, (Math.random() - 0.5) * 0.1);
        }
    }

    // ── FullBright ────────────────────────────────────────────────────────────
    private void handleFullBright(MinecraftClient mc) {
        if (mc.options == null) return;
        if (ModuleManager.getInstance().isEnabled("FullBright")) {
            mc.options.gamma = 10.0;
        }
    }
}
