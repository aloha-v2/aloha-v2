package com.alohaclient.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.Matrix4f;

public class CustomTitleScreen extends Screen {

    // Floating particles
    private final float[] px   = new float[50];
    private final float[] py   = new float[50];
    private final float[] pspd = new float[50];
    private boolean pinit = false;

    public CustomTitleScreen() {
        super(new LiteralText("AlohaClient"));
    }

    @Override
    protected void init() {
        super.init();

        if (!pinit) {
            for (int i = 0; i < px.length; i++) {
                px[i]   = (float) (Math.random() * this.width);
                py[i]   = (float) (Math.random() * this.height);
                pspd[i] = (float) (0.1 + Math.random() * 0.3);
            }
            pinit = true;
        }

        int cx = this.width  / 2;
        int bw = 200, bh = 20, gap = 5;
        int by = this.height / 2 - (bh + gap) * 2 + 10;

        this.addButton(new GlowButton(cx - bw / 2, by,                  bw, bh,
                new LiteralText("Singleplayer"),
                btn -> this.client.openScreen(new SelectWorldScreen(this))));
        this.addButton(new GlowButton(cx - bw / 2, by + (bh + gap),     bw, bh,
                new LiteralText("Multiplayer"),
                btn -> this.client.openScreen(new MultiplayerScreen(this))));
        this.addButton(new GlowButton(cx - bw / 2, by + (bh + gap) * 2, bw, bh,
                new LiteralText("Settings"),
                btn -> this.client.openScreen(new OptionsScreen(this, this.client.options))));
        this.addButton(new GlowButton(cx - bw / 2, by + (bh + gap) * 3, bw, bh,
                new LiteralText("Quit"),
                btn -> this.client.scheduleStop()));
    }

    @Override
    public void render(MatrixStack m, int mouseX, int mouseY, float delta) {
        // Solid dark background — no gradient, no artifacts
        fill(m, 0, 0, this.width, this.height, 0xFF06080E);

        renderParticles(m);
        renderLogo(m);

        // Footer
        this.textRenderer.drawWithShadow(m,
            "AlohaClient 1.0  |  Fabric 1.16.5  |  1.16.5",
            4, this.height - 10, 0x22445566);

        super.render(m, mouseX, mouseY, delta);
    }

    private void renderParticles(MatrixStack m) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        for (int i = 0; i < px.length; i++) {
            py[i] -= pspd[i];
            if (py[i] < -3) {
                py[i] = this.height + 3;
                px[i] = (float) (Math.random() * this.width);
            }
            int a     = 30 + (i % 4) * 15;
            int color = (a << 24) | 0x2244AA;
            drawQuad(m, (int) px[i], (int) py[i], (int) px[i] + 2, (int) py[i] + 2, color);
        }
        RenderSystem.disableBlend();
    }

    private void renderLogo(MatrixStack m) {
        String logo  = "AlohaClient";
        float  scale = 3.5f;
        int    tw    = this.textRenderer.getWidth(logo);
        float  lx    = this.width / 2f - tw * scale / 2f;
        float  ly    = 20f;

        m.push();
        m.translate(lx, ly, 0);
        m.scale(scale, scale, 1f);
        // Soft shadow layer
        this.textRenderer.draw(m, logo, 1, 1, 0x22336699);
        // Main text
        this.textRenderer.drawWithShadow(m, logo, 0, 0, 0xFFCCDDFF);
        m.pop();

        // Separator line — solid, no gradient, no artifacts
        int lineY = (int) (ly + 10 * scale + 5);
        int cx    = this.width / 2;
        fill(m, cx - 65, lineY, cx + 65, lineY + 1, 0x551A2A6C);
    }

    /**
     * Draw a filled quad using Tessellator; used for particles.
     * Avoids Screen.fill() which calls fillGradient internally in some versions.
     */
    private static void drawQuad(MatrixStack matrices, int x1, int y1, int x2, int y2, int color) {
        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >>  8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        Matrix4f     mat  = matrices.peek().getModel();
        Tessellator  tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        RenderSystem.disableTexture();
        RenderSystem.color4f(r, g, b, a);

        buf.begin(7, VertexFormats.POSITION);
        buf.vertex(mat, x1, y2, 0f).next();
        buf.vertex(mat, x2, y2, 0f).next();
        buf.vertex(mat, x2, y1, 0f).next();
        buf.vertex(mat, x1, y1, 0f).next();
        tess.draw();

        RenderSystem.enableTexture();
    }

    @Override
    public boolean shouldCloseOnEsc() { return false; }
}
