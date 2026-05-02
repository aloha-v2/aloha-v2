package com.alohaclient.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class GlowButton extends ButtonWidget {

    private float hover = 0f;
    private long lastMs = 0L;

    public GlowButton(int x, int y, int width, int height, Text message, PressAction onPress) {
        super(x, y, width, height, message, onPress);
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        long now = System.currentTimeMillis();
        float dt = lastMs == 0 ? 0 : (now - lastMs) / 1000f;
        lastMs = now;
        float target = this.isHovered() ? 1f : 0f;
        hover = MathHelper.clamp(hover + (target - hover) * Math.min(dt * 10f, 1f), 0f, 1f);

        int x = this.x, y = this.y, w = this.width, h = this.height;

        // Background
        int bgAlpha = (int)(90 + 50 * hover);
        int bgR = (int)(8 + 12 * hover);
        int bgG = (int)(10 + 15 * hover);
        int bgB = (int)(22 + 18 * hover);
        fill(matrices, x, y, x + w, y + h, (bgAlpha << 24) | (bgR << 16) | (bgG << 8) | bgB);

        // Left accent border
        int accentAlpha = (int)(120 + 135 * hover);
        fill(matrices, x, y, x + 2, y + h, (accentAlpha << 24) | 0x3366CC);

        // Bottom border line
        fill(matrices, x + 2, y + h - 1, x + w, y + h, 0x22334466);

        // Text
        int textColor = isHovered() ? 0xFFFFFF : 0xAABBCC;
        drawCenteredText(matrices, MinecraftClient.getInstance().textRenderer,
            this.getMessage(), x + w / 2, y + (h - 8) / 2, textColor);
    }
}
