package com.alohaclient.gui;

import com.alohaclient.module.BooleanSetting;
import com.alohaclient.module.Module;
import com.alohaclient.module.ModuleManager;
import com.alohaclient.module.NumberSetting;
import com.alohaclient.module.Setting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * AlohaClient ClickGUI — sidebar + module cards layout.
 *
 * Visual structure:
 *   - top bar with logo, title, current category and a search field
 *   - left sidebar with category buttons and a user card at the bottom
 *   - main area showing a two-column grid of module cards
 *
 * Everything is drawn with fillRect / Tessellator quads — no external textures
 * are required, so the GUI works in any environment.
 */
public class ClickGui extends Screen {

    // ── Color palette ────────────────────────────────────────────────────────
    private static final int COL_BG_OVERLAY  = 0xCC050810;
    private static final int COL_PANEL       = 0xFF111726;
    private static final int COL_PANEL_HI    = 0xFF1A2236;
    private static final int COL_PANEL_LINE  = 0xFF263049;
    private static final int COL_CARD        = 0xFF161D2F;
    private static final int COL_CARD_HEAD   = 0xFF1B2336;
    private static final int COL_CARD_LINE   = 0xFF222B41;
    private static final int COL_TOPBAR      = 0xFF0C111E;
    private static final int COL_TOPBAR_LINE = 0xFF1B2436;
    private static final int COL_ACCENT      = 0xFF6E8BFF;   // soft indigo
    private static final int COL_ACCENT_DIM  = 0xFF394676;
    private static final int COL_TOGGLE_ON   = 0xFF6E8BFF;
    private static final int COL_TOGGLE_OFF  = 0xFF2A3148;
    private static final int COL_TEXT_HI     = 0xFFE6ECFF;
    private static final int COL_TEXT_MID    = 0xFFB6BFD4;
    private static final int COL_TEXT_DIM    = 0xFF7C849A;
    private static final int COL_TEXT_FAINT  = 0xFF4E556B;

    // ── Layout constants ─────────────────────────────────────────────────────
    private static final int TOPBAR_H   = 44;
    private static final int SIDEBAR_W  = 140;
    private static final int PADDING    = 12;
    private static final int CARD_HEAD  = 30;
    private static final int CARD_GAP   = 10;
    private static final int SETTING_H  = 22;
    private static final int USER_CARD_H = 56;

    // ── State ────────────────────────────────────────────────────────────────
    private final List<String> categoryOrder = new ArrayList<>();
    private String currentCategory = null;
    private String search          = "";
    private boolean searchFocused  = false;

    // Drag state for sliders
    private NumberSetting draggedSetting = null;

    public ClickGui() {
        super(new LiteralText("AlohaClient"));
    }

    @Override
    protected void init() {
        super.init();
        categoryOrder.clear();
        categoryOrder.addAll(ModuleManager.getInstance().getCategories());
        if (currentCategory == null && !categoryOrder.isEmpty()) {
            currentCategory = categoryOrder.get(0);
        }
    }

    // ── Render ───────────────────────────────────────────────────────────────
    @Override
    public void render(MatrixStack m, int mx, int my, float delta) {
        // Background overlay over the game world
        fillRect(m, 0, 0, this.width, this.height, COL_BG_OVERLAY);

        renderTopBar(m, mx, my);
        renderSidebar(m, mx, my);
        renderContent(m, mx, my);

        super.render(m, mx, my, delta);
    }

    private void renderTopBar(MatrixStack m, int mx, int my) {
        fillRect(m, 0, 0, this.width, TOPBAR_H, COL_TOPBAR);
        fillRect(m, 0, TOPBAR_H - 1, this.width, TOPBAR_H, COL_TOPBAR_LINE);

        // Logo glyph — a simple diamond accent
        drawDiamond(m, 18, TOPBAR_H / 2, 6, COL_ACCENT);
        this.textRenderer.drawWithShadow(m, "AlohaClient", 32, TOPBAR_H / 2 - 4, COL_TEXT_HI);

        // Current category centered
        if (currentCategory != null) {
            int tw = this.textRenderer.getWidth(currentCategory);
            this.textRenderer.drawWithShadow(m, currentCategory,
                    this.width / 2 - tw / 2, TOPBAR_H / 2 - 4, COL_TEXT_HI);
        }

        // Language pill
        int langX = this.width - 220;
        fillRect(m, langX, 12, langX + 38, TOPBAR_H - 12, COL_PANEL);
        fillRect(m, langX, 12, langX + 38, 13,            COL_PANEL_LINE);
        this.textRenderer.drawWithShadow(m, "ENG", langX + 8, TOPBAR_H / 2 - 4, COL_TEXT_MID);

        // Search box
        int sx = this.width - 175, sw = 160;
        int sy = 12,                sh = TOPBAR_H - 24;
        int border = searchFocused ? COL_ACCENT : COL_PANEL_LINE;
        fillRect(m, sx,         sy,         sx + sw, sy + sh, COL_PANEL);
        fillRect(m, sx,         sy,         sx + sw, sy + 1,  border);
        fillRect(m, sx,         sy + sh - 1, sx + sw, sy + sh, border);
        fillRect(m, sx,         sy,         sx + 1,  sy + sh, border);
        fillRect(m, sx + sw - 1, sy,         sx + sw, sy + sh, border);

        String shown = search.isEmpty() && !searchFocused ? "Search" : search;
        int    sCol  = search.isEmpty() && !searchFocused ? COL_TEXT_FAINT : COL_TEXT_HI;
        this.textRenderer.draw(m, shown, sx + 8, sy + sh / 2 - 3, sCol);
        if (searchFocused && (System.currentTimeMillis() / 500) % 2 == 0) {
            int caretX = sx + 8 + this.textRenderer.getWidth(search);
            fillRect(m, caretX, sy + 4, caretX + 1, sy + sh - 4, COL_TEXT_HI);
        }
    }

    private void renderSidebar(MatrixStack m, int mx, int my) {
        int x = PADDING;
        int y = TOPBAR_H + PADDING;
        int h = this.height - TOPBAR_H - PADDING * 2;

        // Sidebar background
        fillRect(m, x, y, x + SIDEBAR_W, y + h, COL_PANEL);
        fillRect(m, x, y, x + SIDEBAR_W, y + 1, COL_PANEL_LINE);
        fillRect(m, x, y + h - 1, x + SIDEBAR_W, y + h, COL_PANEL_LINE);
        fillRect(m, x, y, x + 1, y + h, COL_PANEL_LINE);
        fillRect(m, x + SIDEBAR_W - 1, y, x + SIDEBAR_W, y + h, COL_PANEL_LINE);

        // Category buttons
        int by = y + 10;
        for (String cat : categoryOrder) {
            int bx = x + 8;
            int bw = SIDEBAR_W - 16;
            int bh = 28;
            boolean selected = cat.equals(currentCategory);
            boolean hover    = inside(mx, my, bx, by, bx + bw, by + bh);

            int bg = selected ? COL_PANEL_HI : (hover ? COL_PANEL_HI : COL_PANEL);
            fillRect(m, bx, by, bx + bw, by + bh, bg);
            if (selected) {
                fillRect(m, bx, by, bx + 2, by + bh, COL_ACCENT);
            }

            // Category icon glyph (a small filled square + label)
            int gx = bx + 10;
            int gy = by + bh / 2 - 4;
            fillRect(m, gx, gy, gx + 8, gy + 8, getCategoryColor(cat));

            int textCol = selected ? COL_TEXT_HI : COL_TEXT_MID;
            this.textRenderer.drawWithShadow(m, cat, bx + 26, by + bh / 2 - 4, textCol);

            by += bh + 4;
        }

        // User card at the bottom of the sidebar
        renderUserCard(m, x + 8, y + h - USER_CARD_H - 8, SIDEBAR_W - 16, USER_CARD_H);
    }

    private void renderUserCard(MatrixStack m, int x, int y, int w, int h) {
        fillRect(m, x, y, x + w, y + h, COL_PANEL_HI);
        fillRect(m, x, y, x + w, y + 1, COL_PANEL_LINE);

        // Avatar circle
        drawCircle(m, x + 16, y + h / 2, 10, COL_ACCENT_DIM);
        drawCircle(m, x + 16, y + h / 2, 8,  COL_ACCENT);

        String mcUser = MinecraftClient.getInstance().getSession() != null
                ? MinecraftClient.getInstance().getSession().getUsername()
                : "Player";
        this.textRenderer.drawWithShadow(m, mcUser, x + 32, y + 12, COL_TEXT_HI);

        java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("dd.MM.yyyy");
        this.textRenderer.draw(m, fmt.format(new java.util.Date()),
                x + 32, y + 28, COL_TEXT_DIM);
    }

    private void renderContent(MatrixStack m, int mx, int my) {
        if (currentCategory == null) return;

        int contentX = PADDING * 2 + SIDEBAR_W;
        int contentY = TOPBAR_H + PADDING;
        int contentW = this.width  - contentX - PADDING;
        int contentH = this.height - contentY - PADDING;

        // Two-column grid
        int gap     = CARD_GAP;
        int cardW   = (contentW - gap) / 2;
        int colX[]  = new int[]{ contentX, contentX + cardW + gap };
        int colY[]  = new int[]{ contentY, contentY };

        List<Module> mods = ModuleManager.getInstance().getByCategory(currentCategory);
        for (Module mod : mods) {
            if (!matchesSearch(mod)) continue;
            int col = colY[0] <= colY[1] ? 0 : 1;
            int x   = colX[col];
            int y   = colY[col];
            int h   = renderCard(m, mod, x, y, cardW, mx, my);
            colY[col] = y + h + gap;
        }
    }

    private int renderCard(MatrixStack m, Module mod, int x, int y, int w, int mx, int my) {
        int settingsCount = mod.getSettings().size();
        int h = CARD_HEAD + (settingsCount > 0 ? settingsCount * SETTING_H + 4 : 0);

        // Card background
        fillRect(m, x, y, x + w, y + h, COL_CARD);
        fillRect(m, x, y, x + w, y + 1, COL_CARD_LINE);
        fillRect(m, x, y + h - 1, x + w, y + h, COL_CARD_LINE);
        fillRect(m, x, y, x + 1, y + h, COL_CARD_LINE);
        fillRect(m, x + w - 1, y, x + w, y + h, COL_CARD_LINE);

        // Header
        fillRect(m, x + 1, y + 1, x + w - 1, y + CARD_HEAD, COL_CARD_HEAD);
        fillRect(m, x + 1, y + CARD_HEAD - 1, x + w - 1, y + CARD_HEAD, COL_CARD_LINE);

        // Module icon (category-colored square)
        fillRect(m, x + 10, y + CARD_HEAD / 2 - 5, x + 20, y + CARD_HEAD / 2 + 5,
                getCategoryColor(mod.getCategory()));
        this.textRenderer.drawWithShadow(m, mod.getName(),
                x + 26, y + CARD_HEAD / 2 - 4, COL_TEXT_HI);

        // Toggle on the right
        int tx = x + w - 36, ty = y + 8, tw = 26, th = CARD_HEAD - 16;
        renderToggle(m, tx, ty, tw, th, mod.isEnabled());

        // Settings
        int ry = y + CARD_HEAD + 2;
        for (Setting s : mod.getSettings()) {
            renderSetting(m, s, x + 10, ry, w - 20, mx, my);
            ry += SETTING_H;
        }
        return h;
    }

    private void renderSetting(MatrixStack m, Setting s, int x, int y, int w, int mx, int my) {
        if (s instanceof BooleanSetting) {
            BooleanSetting bs = (BooleanSetting) s;
            this.textRenderer.draw(m, s.getName(), x, y + SETTING_H / 2 - 3, COL_TEXT_MID);
            int tw = 24, th = 12;
            int tx = x + w - tw, ty = y + SETTING_H / 2 - th / 2;
            renderToggle(m, tx, ty, tw, th, bs.getValue());
        } else if (s instanceof NumberSetting) {
            NumberSetting ns = (NumberSetting) s;
            this.textRenderer.draw(m, s.getName(), x, y + SETTING_H / 2 - 7, COL_TEXT_MID);

            String val = ns.getDisplay();
            int    vw  = this.textRenderer.getWidth(val);
            this.textRenderer.draw(m, val,
                    x + w - vw, y + SETTING_H / 2 - 7, COL_TEXT_HI);

            // Slider track
            int trackX = x;
            int trackY = y + SETTING_H - 6;
            int trackW = w;
            int trackH = 3;
            fillRect(m, trackX, trackY, trackX + trackW, trackY + trackH, COL_PANEL_LINE);

            double t = (ns.getValue() - ns.getMin()) / (ns.getMax() - ns.getMin());
            t = Math.max(0.0, Math.min(1.0, t));
            int knobX = trackX + (int) Math.round(t * trackW);
            fillRect(m, trackX, trackY, knobX, trackY + trackH, COL_ACCENT);
            fillRect(m, knobX - 2, trackY - 2, knobX + 2, trackY + trackH + 2, COL_ACCENT);
        }
    }

    private static void renderToggle(MatrixStack m, int x, int y, int w, int h, boolean on) {
        int bg = on ? COL_TOGGLE_ON : COL_TOGGLE_OFF;
        fillRect(m, x, y, x + w, y + h, bg);
        // Border
        fillRect(m, x, y, x + w, y + 1, COL_PANEL_LINE);
        fillRect(m, x, y + h - 1, x + w, y + h, COL_PANEL_LINE);
        fillRect(m, x, y, x + 1, y + h, COL_PANEL_LINE);
        fillRect(m, x + w - 1, y, x + w, y + h, COL_PANEL_LINE);

        // Knob
        int knobSize = h - 4;
        int knobX = on ? x + w - knobSize - 2 : x + 2;
        int knobY = y + 2;
        fillRect(m, knobX, knobY, knobX + knobSize, knobY + knobSize, 0xFFE9ECFB);
    }

    // ── Input ────────────────────────────────────────────────────────────────
    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        // Sidebar category clicks
        int sx = PADDING;
        int sy = TOPBAR_H + PADDING + 10;
        int sw = SIDEBAR_W;
        int bw = sw - 16;
        int bh = 28;
        int by = sy;
        for (String cat : categoryOrder) {
            int bx = sx + 8;
            if (inside((int) mx, (int) my, bx, by, bx + bw, by + bh)) {
                currentCategory = cat;
                return true;
            }
            by += bh + 4;
        }

        // Search box click
        int searchX = this.width - 175, searchW = 160;
        int searchY = 12,                searchH = TOPBAR_H - 24;
        if (inside((int) mx, (int) my, searchX, searchY, searchX + searchW, searchY + searchH)) {
            searchFocused = true;
            return true;
        } else {
            searchFocused = false;
        }

        // Module / setting clicks
        if (currentCategory != null && handleCardClicks(mx, my, btn)) return true;

        return super.mouseClicked(mx, my, btn);
    }

    private boolean handleCardClicks(double mx, double my, int btn) {
        int contentX = PADDING * 2 + SIDEBAR_W;
        int contentY = TOPBAR_H + PADDING;
        int contentW = this.width - contentX - PADDING;
        int gap   = CARD_GAP;
        int cardW = (contentW - gap) / 2;
        int colX[] = new int[]{ contentX, contentX + cardW + gap };
        int colY[] = new int[]{ contentY, contentY };

        List<Module> mods = ModuleManager.getInstance().getByCategory(currentCategory);
        for (Module mod : mods) {
            if (!matchesSearch(mod)) continue;
            int col = colY[0] <= colY[1] ? 0 : 1;
            int x   = colX[col];
            int y   = colY[col];
            int settingsCount = mod.getSettings().size();
            int h = CARD_HEAD + (settingsCount > 0 ? settingsCount * SETTING_H + 4 : 0);

            // Header toggle
            int tx = x + cardW - 36, ty = y + 8, tw = 26, th = CARD_HEAD - 16;
            if (inside((int) mx, (int) my, tx, ty, tx + tw, ty + th)) {
                if (btn == 0) mod.toggle();
                return true;
            }

            // Card header (also toggles)
            if (inside((int) mx, (int) my, x, y, x + cardW, y + CARD_HEAD)) {
                if (btn == 0) mod.toggle();
                return true;
            }

            // Setting rows
            int ry = y + CARD_HEAD + 2;
            for (Setting s : mod.getSettings()) {
                int sx0 = x + 10, sw = cardW - 20;
                int sy0 = ry,     sh0 = SETTING_H;
                if (inside((int) mx, (int) my, sx0, sy0, sx0 + sw, sy0 + sh0)) {
                    if (s instanceof BooleanSetting) {
                        if (btn == 0) ((BooleanSetting) s).toggle();
                    } else if (s instanceof NumberSetting) {
                        NumberSetting ns = (NumberSetting) s;
                        int trackY = sy0 + sh0 - 6;
                        if (my >= trackY - 4 && my <= trackY + 7) {
                            applySliderDrag(ns, mx, sx0, sw);
                            draggedSetting = ns;
                        } else if (btn == 1) {
                            // Right-click resets to mid-range
                            ns.setValue((ns.getMin() + ns.getMax()) / 2.0);
                        }
                    }
                    return true;
                }
                ry += SETTING_H;
            }

            colY[col] = y + h + gap;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        if (draggedSetting != null) {
            // Find the slider track for the dragged setting
            int contentX = PADDING * 2 + SIDEBAR_W;
            int contentY = TOPBAR_H + PADDING;
            int contentW = this.width - contentX - PADDING;
            int cardW    = (contentW - CARD_GAP) / 2;
            int colY[]   = new int[]{ contentY, contentY };

            List<Module> mods = ModuleManager.getInstance().getByCategory(currentCategory);
            for (Module mod : mods) {
                if (!matchesSearch(mod)) continue;
                int col = colY[0] <= colY[1] ? 0 : 1;
                int x   = (col == 0 ? contentX : contentX + cardW + CARD_GAP);
                int y   = colY[col];
                int settingsCount = mod.getSettings().size();
                int h = CARD_HEAD + (settingsCount > 0 ? settingsCount * SETTING_H + 4 : 0);

                int ry = y + CARD_HEAD + 2;
                for (Setting s : mod.getSettings()) {
                    if (s == draggedSetting) {
                        applySliderDrag((NumberSetting) s, mx, x + 10, cardW - 20);
                        return true;
                    }
                    ry += SETTING_H;
                }
                colY[col] = y + h + CARD_GAP;
            }
        }
        return super.mouseDragged(mx, my, btn, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int btn) {
        draggedSetting = null;
        return super.mouseReleased(mx, my, btn);
    }

    private static void applySliderDrag(NumberSetting ns, double mouseX, int trackX, int trackW) {
        double t = (mouseX - trackX) / (double) trackW;
        t = Math.max(0.0, Math.min(1.0, t));
        double raw = ns.getMin() + t * (ns.getMax() - ns.getMin());
        // Snap to step
        double step = ns.getStep();
        double snapped = Math.round(raw / step) * step;
        snapped = Math.max(ns.getMin(), Math.min(ns.getMax(), snapped));
        ns.setValue(snapped);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchFocused) {
            if (keyCode == 256) {        // ESC closes the search, not the GUI
                searchFocused = false;
                return true;
            }
            if (keyCode == 259 && search.length() > 0) {  // Backspace
                search = search.substring(0, search.length() - 1);
                return true;
            }
            if (keyCode == 257) {        // Enter
                searchFocused = false;
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (searchFocused && chr >= 32 && chr < 127 && search.length() < 24) {
            search = search + chr;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override public boolean isPauseScreen()    { return false; }
    @Override public boolean shouldCloseOnEsc() { return true;  }

    // ── Utility ──────────────────────────────────────────────────────────────
    private boolean matchesSearch(Module mod) {
        if (search.isEmpty()) return true;
        String s = search.toLowerCase();
        return mod.getName().toLowerCase().contains(s)
                || mod.getCategory().toLowerCase().contains(s);
    }

    private static int getCategoryColor(String cat) {
        if ("Combat".equals(cat))   return 0xFFE05E5E;
        if ("Movement".equals(cat)) return 0xFF63D29B;
        if ("Visual".equals(cat))   return 0xFF6E8BFF;
        if ("Misc".equals(cat))     return 0xFFD0A86A;
        return 0xFF8A93AD;
    }

    private static boolean inside(int mx, int my, int x1, int y1, int x2, int y2) {
        return mx >= x1 && mx <= x2 && my >= y1 && my <= y2;
    }

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

    private static void drawDiamond(MatrixStack m, int cx, int cy, int s, int color) {
        // Square rotated by drawing horizontal scan-lines decreasing toward edges
        for (int dy = -s; dy <= s; dy++) {
            int span = s - Math.abs(dy);
            fillRect(m, cx - span, cy + dy, cx + span + 1, cy + dy + 1, color);
        }
    }

    private static void drawCircle(MatrixStack m, int cx, int cy, int r, int color) {
        for (int dy = -r; dy <= r; dy++) {
            int dx = (int) Math.round(Math.sqrt(Math.max(0, r * r - dy * dy)));
            fillRect(m, cx - dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
        }
    }
}
