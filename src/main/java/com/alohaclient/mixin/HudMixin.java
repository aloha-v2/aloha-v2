package com.alohaclient.mixin;

import com.alohaclient.module.Module;
import com.alohaclient.module.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.BufferBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Mixin(InGameHud.class)
public class HudMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (!ModuleManager.getInstance().isEnabled("HUD")) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options.debugEnabled) return;

        List<Module> active = new ArrayList<>();
        for (Module m : ModuleManager.getInstance().getModules()) {
            if (m.isEnabled() && !m.getName().equals("HUD")) active.add(m);
        }
        // Sort by text width descending so wider names sit at top
        active.sort(Comparator.comparingInt(m -> -mc.textRenderer.getWidth(m.getName())));

        int sw = mc.getWindow().getScaledWidth();
        int y  = 2;

        for (Module mod : active) {
            String name = mod.getName();
            int tw = mc.textRenderer.getWidth(name);
            int x  = sw - tw - 4;

            // Background bar
            fillRect(matrices, x - 3, y - 1, sw, y + 9, 0x77050810);
            // Category accent strip on the left
            fillRect(matrices, x - 3, y - 1, x - 1, y + 9, getCatColor(mod.getCategory()));
            // Module name
            mc.textRenderer.drawWithShadow(matrices, name, x, y, 0xFFCCDDFF);

            y += 11;
        }
    }

    private static int getCatColor(String cat) {
        if ("Combat".equals(cat))   return 0xFFCC3333;
        if ("Movement".equals(cat)) return 0xFF33CC77;
        if ("Visual".equals(cat))   return 0xFF3388CC;
        return 0xFF886699;
    }

    private static void fillRect(MatrixStack matrices, int x1, int y1, int x2, int y2, int color) {
        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >>  8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        Matrix4f mat  = matrices.peek().getModel();
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.color4f(r, g, b, a);

        buf.begin(7, VertexFormats.POSITION);
        buf.vertex(mat, x1, y2, 0f).next();
        buf.vertex(mat, x2, y2, 0f).next();
        buf.vertex(mat, x2, y1, 0f).next();
        buf.vertex(mat, x1, y1, 0f).next();
        tess.draw();

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }
}
