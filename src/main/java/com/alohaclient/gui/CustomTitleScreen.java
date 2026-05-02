package com.alohaclient.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
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

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Custom AlohaClient title screen.
 *
 * Layout:
 *   - dark animated background with a subtle vertical particle field
 *   - large centered "AlohaClient" logo with a soft pulsing glow line
 *   - vertical stack of 4 styled buttons (Singleplayer / Multiplayer /
 *     Settings / Quit)
 *   - bottom-left footer with version info, bottom-right user card
 *
 * All visuals are drawn with {@link Tessellator} quads — no texture assets.
 */
public class CustomTitleScreen extends Screen {

    // ── Color palette (matches the new ClickGUI) ─────────────────────────────
    private static final int COL_BG         = 0xFF06080E;
    private static final int COL_BG_HI      = 0xFF0C111E;
    private static final int COL_PARTICLE   = 0x882C3F7A;
    private static final int COL_PARTICLE_2 = 0x556E8BFF;
    private static final int COL_LOGO       = 0xFFE6ECFF;
    private static final int COL_LOGO_DIM   = 0x886E8BFF;
    private static final int COL_ACCENT     = 0xFF6E8BFF;
    private static final int COL_ACCENT_DIM = 0xFF394676;
    private static final int COL_PANEL      = 0xCC0E1424;
    private static final int COL_PANEL_LINE = 0xFF222B41;
    private static final int COL_TEXT_HI    = 0xFFE6ECFF;
    private static final int COL_TEXT_MID   = 0xFFB6BFD4;
    private static final int COL_TEXT_DIM   = 0xFF7C849A;
    private static final int COL_TEXT_FAINT = 0xFF4E556B;

    // ── Particle field ───────────────────────────────────────────────────────
    private static final int PARTICLES = 80;
    private final float[] px   = new float[PARTICLES];
    private final float[] py   = new float[PARTICLES];
    private final float[] pspd = new float[PARTICLES];
    private final float[] psz  = new float[PARTICLES];
    private final int  [] pcol = new int  [PARTICLES];
    private boolean pinit = false;

    public CustomTitleScreen() {
        super(new LiteralText("AlohaClient"));
    }

    @Override
    protected void init() {
        super.init();
        if (!pinit) {
            for (int i = 0; i < PARTICLES; i++) {
                px[i]   = (float) (Math.random() * this.width);
                py[i]   = (float) (Math.random() * this.height);
                pspd[i] = (float) (0.10 + Math.random() * 0.35);
                psz[i]  = (float) (1 + Math.random() * 1.6);
                pcol[i] = (i % 5 == 0) ? COL_PARTICLE_2 : COL_PARTICLE;
            }
            pinit = true;
        }

        // Buttons
        int bw  = 220;
        int bh  = 28;
        int gap = 8;
        int cx  = this.width  / 2;
        int by  = this.height / 2 - 4;

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
        renderBackdrop(m);
        renderParticles(m);
        renderLogo(m);
        renderUserCard(m);
        renderFooter(m);
        super.render(m, mouseX, mouseY, delta);
    }

    // ── Background ───────────────────────────────────────────────────────────
    private void renderBackdrop(MatrixStack m) {
        // Two solid bands give a hint of depth without using fillGradient
        // (which is buggy on some shader setups).
        fillRect(m, 0, 0,                this.width, this.height / 2, COL_BG_HI);
        fillRect(m, 0, this.height / 2,  this.width, this.height,     COL_BG);

        // Faint horizontal scanline near vertical center
        int lineY = this.height / 2;
        fillRect(m, 0, lineY, this.width, lineY + 1, COL_PANEL_LINE);
    }

    private void renderParticles(MatrixStack m) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        for (int i = 0; i < PARTICLES; i++) {
            py[i] -= pspd[i];
            if (py[i] < -psz[i]) {
                py[i] = this.height + psz[i];
                px[i] = (float) (Math.random() * this.width);
            }
            int x1 = (int) px[i];
            int y1 = (int) py[i];
            int x2 = x1 + (int) Math.max(1, psz[i]);
            int y2 = y1 + (int) Math.max(1, psz[i]);
            fillRect(m, x1, y1, x2, y2, pcol[i]);
        }
        RenderSystem.disableBlend();
    }

    // ── Logo ─────────────────────────────────────────────────────────────────
    private void renderLogo(MatrixStack m) {
        String logo  = "AlohaClient";
        float  scale = 4.0f;
        int    tw    = this.textRenderer.getWidth(logo);
        float  lw    = tw * scale;
        float  lx    = this.width / 2f - lw / 2f;
        float  ly    = this.height / 2f - 90;

        // Soft glow rectangle behind the logo
        int glowH = (int) (this.textRenderer.fontHeight * scale + 20);
        int glowW = (int) (lw + 60);
        int gx1   = (int) (this.width / 2f - glowW / 2f);
        int gy1   = (int) (ly - 10);
        for (int i = 6; i > 0; i--) {
            int alpha = 0x10 * i;
            int color = (alpha << 24) | (COL_ACCENT & 0x00FFFFFF);
            fillRect(m, gx1 - i * 2, gy1 - i, gx1 + glowW + i * 2, gy1 + glowH + i, color);
        }

        m.push();
        m.translate(lx, ly, 0);
        m.scale(scale, scale, 1f);
        // Soft drop-shadow
        this.textRenderer.draw(m, logo, 1, 1, 0x33060810);
        // Foreground
        this.textRenderer.draw(m, logo, 0, 0, COL_LOGO);
        m.pop();

        // Pulsing accent line under the logo
        int   cxi   = this.width / 2;
        int   lineY = (int) (ly + this.textRenderer.fontHeight * scale + 6);
        long  t     = System.currentTimeMillis();
        float pulse = (float) (0.5 + 0.5 * Math.sin(t / 600.0));
        int   half  = (int) (90 + 40 * pulse);
        fillRect(m, cxi - half, lineY, cxi + half, lineY + 2, COL_ACCENT);
        // Side gradients fade out
        for (int i = 1; i <= 6; i++) {
            int alpha = (int) (0x40 * (1.0 - i / 7.0));
            int color = (alpha << 24) | (COL_ACCENT & 0x00FFFFFF);
            fillRect(m, cxi - half - i * 8, lineY, cxi - half - (i - 1) * 8, lineY + 2, color);
            fillRect(m, cxi + half + (i - 1) * 8, lineY, cxi + half + i * 8, lineY + 2, color);
        }

        // Tagline under the line
        String tag = "Minecraft 1.16.5  ·  Fabric Client";
        int    tagW = this.textRenderer.getWidth(tag);
        this.textRenderer.draw(m, tag,
                this.width / 2f - tagW / 2f, lineY + 8, COL_TEXT_DIM);
    }

    // ── User card (bottom right) ─────────────────────────────────────────────
    private void renderUserCard(MatrixStack m) {
        int w = 170, h = 56;
        int x = this.width - w - 16;
        int y = this.height - h - 16;

        fillRect(m, x, y, x + w, y + h, COL_PANEL);
        fillRect(m, x, y, x + w, y + 1, COL_PANEL_LINE);
        fillRect(m, x, y + h - 1, x + w, y + h, COL_PANEL_LINE);
        fillRect(m, x, y, x + 1, y + h, COL_PANEL_LINE);
        fillRect(m, x + w - 1, y, x + w, y + h, COL_PANEL_LINE);

        // Avatar circle
        drawCircle(m, x + 18, y + h / 2, 12, COL_ACCENT_DIM);
        drawCircle(m, x + 18, y + h / 2, 9,  COL_ACCENT);

        String user = MinecraftClient.getInstance().getSession() != null
                ? MinecraftClient.getInstance().getSession().getUsername()
                : "Player";
        this.textRenderer.drawWithShadow(m, user, x + 36, y + 12, COL_TEXT_HI);

        SimpleDateFormat fmt = new SimpleDateFormat("dd.MM.yyyy");
        this.textRenderer.draw(m, fmt.format(new Date()), x + 36, y + 28, COL_TEXT_DIM);

        this.textRenderer.draw(m, "online", x + 36, y + 40, COL_ACCENT);
    }

    // ── Footer ───────────────────────────────────────────────────────────────
    private void renderFooter(MatrixStack m) {
        this.textRenderer.draw(m, "AlohaClient v1.0",      8, this.height - 20, COL_TEXT_MID);
        this.textRenderer.draw(m, "Fabric Loader 0.13.3", 8, this.height - 10, COL_TEXT_FAINT);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    static void fillRect(MatrixStack matrices, int x1, int y1, int x2, int y2, int color) {
        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >>  8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        Matrix4f      mat  = matrices.peek().getModel();
        Tessellator   tess = Tessellator.getInstance();
        BufferBuilder buf  = tess.getBuffer();

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

    private static void drawCircle(MatrixStack m, int cx, int cy, int r, int color) {
        for (int dy = -r; dy <= r; dy++) {
            int dx = (int) Math.round(Math.sqrt(Math.max(0, r * r - dy * dy)));
            fillRect(m, cx - dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
        }
    }
}
