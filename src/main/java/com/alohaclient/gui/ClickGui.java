package com.alohaclient.gui;

import com.alohaclient.module.BooleanSetting;
import com.alohaclient.module.Module;
import com.alohaclient.module.ModuleManager;
import com.alohaclient.module.NumberSetting;
import com.alohaclient.module.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.BufferBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.Matrix4f;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClickGui extends Screen {

    private final List<Panel> panels = new ArrayList<>();

    public ClickGui() {
        super(new LiteralText("AlohaClient"));
    }

    @Override
    protected void init() {
        super.init();
        panels.clear();
        int x = 10;
        for (String cat : ModuleManager.getInstance().getCategories()) {
            panels.add(new Panel(cat, ModuleManager.getInstance().getByCategory(cat), x, 22));
            x += 120;
        }
    }

    @Override
    public void render(MatrixStack m, int mx, int my, float delta) {
        // Translucent overlay
        fillRect(m, 0, 0, this.width, this.height, 0xAA050810);

        // Top bar
        fillRect(m, 0, 0, this.width, 18, 0xFF070B18);
        fillRect(m, 0, 16, this.width, 17, 0xFF1A2A6C);
        this.textRenderer.drawWithShadow(m, "AlohaClient  |  INSERT to close", 6, 5, 0xFF4466AA);

        for (Panel p : panels) p.render(m, mx, my, this.textRenderer);
        super.render(m, mx, my, delta);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        for (Panel p : panels) if (p.mouseClicked((int) mx, (int) my, btn)) return true;
        return super.mouseClicked(mx, my, btn);
    }

    @Override public boolean isPauseScreen()    { return false; }
    @Override public boolean shouldCloseOnEsc() { return true;  }

    // ── Panel ─────────────────────────────────────────────────────────────────

    private static class Panel {
        private final String       title;
        private final List<Module> mods;
        private final int          x;
        private final int          y;
        private boolean            expanded     = true;
        private final Set<String>  expandedMods = new HashSet<>();

        // Layout constants
        private static final int W  = 112; // panel width
        private static final int HH = 14;  // header height
        private static final int MH = 14;  // module row height
        private static final int SH = 13;  // setting row height

        Panel(String title, List<Module> mods, int x, int y) {
            this.title = title;
            this.mods  = mods;
            this.x     = x;
            this.y     = y;
        }

        void render(MatrixStack m, int mx, int my, TextRenderer tr) {
            int accent = getCategoryColor(title);

            // ── Header ──────────────────────────────────────────────────────
            boolean hov = hover(mx, my, x, y, x + W, y + HH);
            fillRect(m, x, y, x + W, y + HH, hov ? 0xFF0E1830 : 0xFF080C1E);
            fillRect(m, x, y, x + W, y + 1,  accent);          // top border
            fillRect(m, x, y, x + 2, y + HH, accent);          // left accent
            tr.drawWithShadow(m, title, x + 5, y + 3, 0xFFAABBCC);
            tr.drawWithShadow(m, expanded ? "-" : "+", x + W - 9, y + 3, 0xFF445577);

            if (!expanded) {
                fillRect(m, x, y + HH, x + W, y + HH + 1, 0xFF0A1020);
                return;
            }

            int ry = y + HH;

            for (Module mod : mods) {
                boolean mhov = hover(mx, my, x, ry, x + W, ry + MH);

                // Module row background
                fillRect(m, x, ry, x + W, ry + MH, mhov ? 0xFF0C1428 : 0xFF080D1C);
                if (mod.isEnabled()) fillRect(m, x, ry, x + 2, ry + MH, accent);

                // Module name + ON/OFF indicator
                int tc = mod.isEnabled() ? 0xFF88BBFF : 0xFF445566;
                tr.drawWithShadow(m, mod.getName(), x + 5, ry + 3, tc);

                String ind = mod.isEnabled() ? "ON" : "OFF";
                int    ic  = mod.isEnabled() ? 0xFF44BB66 : 0xFF774444;
                tr.drawWithShadow(m, ind, x + W - tr.getWidth(ind) - 3, ry + 3, ic);

                // Settings expand arrow
                if (mod.hasSettings()) {
                    String arrow = expandedMods.contains(mod.getName()) ? "v" : ">";
                    int arrowX = x + W - tr.getWidth(ind) - tr.getWidth(arrow) - 7;
                    tr.drawWithShadow(m, arrow, arrowX, ry + 3, 0xFF334455);
                }

                fillRect(m, x, ry + MH - 1, x + W, ry + MH, 0xFF06090F); // divider
                ry += MH;

                // ── Settings rows ────────────────────────────────────────────
                if (expandedMods.contains(mod.getName())) {
                    for (Setting s : mod.getSettings()) {
                        boolean shov = hover(mx, my, x, ry, x + W, ry + SH);
                        fillRect(m, x, ry, x + W, ry + SH, shov ? 0xFF0A1224 : 0xFF070A18);
                        fillRect(m, x, ry, x + 3, ry + SH, 0xFF1A2840); // indent strip

                        if (s instanceof NumberSetting) {
                            NumberSetting ns = (NumberSetting) s;
                            tr.drawWithShadow(m, s.getName(), x + 5, ry + 2, 0xFF667799);
                            String val = ns.getDisplay();
                            tr.drawWithShadow(m, val,
                                x + W / 2 - tr.getWidth(val) / 2, ry + 2, 0xFF88AACC);
                            tr.drawWithShadow(m, "-", x + W - 22, ry + 2, 0xFFCC4444);
                            tr.drawWithShadow(m, "+", x + W - 10, ry + 2, 0xFF44CC88);

                        } else if (s instanceof BooleanSetting) {
                            BooleanSetting bs = (BooleanSetting) s;
                            tr.drawWithShadow(m, s.getName(), x + 5, ry + 2, 0xFF667799);
                            String bval = bs.getValue() ? "ON" : "OFF";
                            int    bc   = bs.getValue() ? 0xFF44BB66 : 0xFF774444;
                            tr.drawWithShadow(m, bval, x + W - tr.getWidth(bval) - 3, ry + 2, bc);
                        }

                        fillRect(m, x, ry + SH - 1, x + W, ry + SH, 0xFF050810); // divider
                        ry += SH;
                    }
                }
            }

            // Bottom edge
            fillRect(m, x, ry, x + W, ry + 1, 0xFF0A1428);
        }

        boolean mouseClicked(int mx, int my, int btn) {
            // ── Header click
            if (hover(mx, my, x, y, x + W, y + HH)) {
                if (btn == 0) expanded = !expanded;
                return true;
            }
            if (!expanded) return false;

            int ry = y + HH;
            for (Module mod : mods) {
                // ── Module row click
                if (hover(mx, my, x, ry, x + W, ry + MH)) {
                    if (btn == 0) mod.toggle();
                    if (btn == 1 && mod.hasSettings()) {
                        if (expandedMods.contains(mod.getName()))
                            expandedMods.remove(mod.getName());
                        else
                            expandedMods.add(mod.getName());
                    }
                    return true;
                }
                ry += MH;

                // ── Setting row clicks
                if (expandedMods.contains(mod.getName())) {
                    for (Setting s : mod.getSettings()) {
                        if (hover(mx, my, x, ry, x + W, ry + SH)) {
                            if (s instanceof NumberSetting) {
                                NumberSetting ns = (NumberSetting) s;
                                // "-" button occupies x+W-22 .. x+W-15
                                if (mx >= x + W - 22 && mx <= x + W - 15) ns.decrement();
                                // "+" button occupies x+W-10 .. x+W-3
                                else if (mx >= x + W - 10 && mx <= x + W - 3) ns.increment();
                            } else if (s instanceof BooleanSetting) {
                                ((BooleanSetting) s).toggle();
                            }
                            return true;
                        }
                        ry += SH;
                    }
                }
            }
            return false;
        }

        private static boolean hover(int mx, int my, int x1, int y1, int x2, int y2) {
            return mx >= x1 && mx <= x2 && my >= y1 && my <= y2;
        }
    }

    // ── Shared utilities ──────────────────────────────────────────────────────

    private static int getCategoryColor(String cat) {
        if ("Combat".equals(cat))   return 0xFFCC3333;
        if ("Movement".equals(cat)) return 0xFF33CC77;
        if ("Visual".equals(cat))   return 0xFF3388CC;
        return 0xFF886699;
    }

    static void fillRect(MatrixStack matrices, int x1, int y1, int x2, int y2, int color) {
        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >>  8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        Matrix4f     mat  = matrices.peek().getModel();
        Tessellator  tess = Tessellator.getInstance();
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
