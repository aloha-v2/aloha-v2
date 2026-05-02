package com.alohaclient.mixin;

import com.alohaclient.module.ModuleManager;
import com.alohaclient.module.combat.KillAura;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class ClientTickMixin {

    private int kaTimer = 0;
    private boolean fbActive = false;
    private double  fbPrevGamma = 1.0;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        MinecraftClient mc = (MinecraftClient) (Object) this;
        if (mc.player == null || mc.world == null) {
            // Restore gamma if FullBright was on while we exit world
            if (fbActive && mc.options != null) {
                mc.options.gamma = fbPrevGamma;
                fbActive = false;
            }
            return;
        }

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

        double range = ka.range.getValue();

        Entity target  = null;
        double closest = range + 0.1;

        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof LivingEntity)) continue;
            if (e == mc.player) continue;                                    // never attack self
            if (!e.isAlive()) continue;
            if (e instanceof PlayerEntity && !ka.attackPlayers.getValue()) continue;
            LivingEntity le = (LivingEntity) e;
            if (le.isSpectator() || le.getHealth() <= 0) continue;

            double d = bboxDistance(mc, e);
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
            mc.player.yaw   = yaw;
            mc.player.pitch = pitch;
        }

        // Packet criticals: spoof a tiny fall to the server before the swing
        if (ModuleManager.getInstance().isEnabled("Criticals")
                && mc.player.isOnGround()
                && !mc.player.isClimbing()
                && !mc.player.isTouchingWater()
                && !mc.player.hasVehicle()) {
            sendPacketCrit(mc);
        }

        // Attack
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private static double bboxDistance(MinecraftClient mc, Entity e) {
        Vec3d eye = mc.player.getCameraPosVec(1.0F);
        Box   box = e.getBoundingBox();
        double cx = MathHelper.clamp(eye.x, box.minX, box.maxX);
        double cy = MathHelper.clamp(eye.y, box.minY, box.maxY);
        double cz = MathHelper.clamp(eye.z, box.minZ, box.maxZ);
        return eye.distanceTo(new Vec3d(cx, cy, cz));
    }

    private static void sendPacketCrit(MinecraftClient mc) {
        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();
        // Send small upward offset, then back, both with onGround=false
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(x, y + 0.0625D, z, false));
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(x, y,            z, false));
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
    // Track activation transitions so we can restore the user's previous gamma
    // when the module is disabled. Without this, gamma would stay at 10.0.
    private void handleFullBright(MinecraftClient mc) {
        if (mc.options == null) return;
        boolean enabled = ModuleManager.getInstance().isEnabled("FullBright");

        if (enabled && !fbActive) {
            fbPrevGamma = mc.options.gamma;
            fbActive    = true;
        }

        if (enabled) {
            mc.options.gamma = 10.0;
        } else if (fbActive) {
            mc.options.gamma = fbPrevGamma;
            fbActive = false;
        }
    }
}
