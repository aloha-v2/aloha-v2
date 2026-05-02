package com.alohaclient.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

/**
 * Flat dark button with an animated indigo accent stripe and hover glow.
 * Matches the AlohaClient ClickGUI palette.
 */
public class GlowButton extends ButtonWidget {

    private float hover  = 0f;
    private long  lastMs = 0L;

    public GlowButton(int x, int y, int width, int height, Text message, PressAction onPress) {
        super(x, y, width, height, message, onPress);
    }

    @Override
    public void renderButton(MatrixStack m, int mouseX, int mouseY, float delta) {
        long now = System.currentTimeMillis();
        float dt = lastMs == 0 ? 0 : (now - lastMs) / 1000f;
        lastMs = now;

        float target = this.isHovered() ? 1f : 0f;
        hover = MathHelper.clamp(hover + (target - hover) * Math.min(dt * 12f, 1f), 0f, 1f);

        int x = this.x, y = this.y, w = this.width, h = this.height;

        // Outer glow on hover
        if (hover > 0.01f) {
            for (int i = 1; i <= 4; i++) {
                int alpha = (int) (40 * hover) - i * 6;
                if (alpha <= 0) break;
                int color = (alpha << 24) | 0x6E8BFF;
                CustomTitleScreen.fillRect(m, x - i, y - i, x + w + i, y + h + i, color);
            }
        }

        // Background — fades from #0E1424 to #1A2236 on hover
        int bgAlpha = (int) (210 + 45 * hover);
        int bgR = (int) (14 + 12 * hover);
        int bgG = (int) (20 + 14 * hover);
        int bgB = (int) (36 + 18 * hover);
        int bg  = (bgAlpha << 24) | (bgR << 16) | (bgG << 8) | bgB;
        CustomTitleScreen.fillRect(m, x, y, x + w, y + h, bg);

        // Border
        int border = (int) (0x55 + 0x80 * hover);
        int bcol   = (border << 24) | 0x6E8BFF;
        CustomTitleScreen.fillRect(m, x, y,         x + w, y + 1,     bcol);
        CustomTitleScreen.fillRect(m, x, y + h - 1, x + w, y + h,     bcol);
        CustomTitleScreen.fillRect(m, x, y,         x + 1, y + h,     bcol);
        CustomTitleScreen.fillRect(m, x + w - 1, y, x + w, y + h,     bcol);

        // Left accent stripe
        int stripeAlpha = (int) (0x90 + 0x6F * hover);
        int stripeCol   = (stripeAlpha << 24) | 0x6E8BFF;
        CustomTitleScreen.fillRect(m, x, y, x + 3, y + h, stripeCol);

        // Text
        int textColor = isHovered() ? 0xFFE6ECFF : 0xFFB6BFD4;
        drawCenteredText(m, MinecraftClient.getInstance().textRenderer,
                this.getMessage(), x + w / 2, y + (h - 8) / 2, textColor);
    }
}
