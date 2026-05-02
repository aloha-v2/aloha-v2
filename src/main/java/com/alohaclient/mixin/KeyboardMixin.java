package com.alohaclient.mixin;

import com.alohaclient.gui.ClickGui;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {

    @Inject(method = "onKey", at = @At("HEAD"))
    private void alohaclient$onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        // key 260 = GLFW_KEY_INSERT, action 1 = GLFW_PRESS
        if (key != 260 || action != 1) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) return; // Only in-game

        if (mc.currentScreen instanceof ClickGui) {
            mc.execute(() -> mc.openScreen(null));
        } else if (mc.currentScreen == null) {
            mc.execute(() -> mc.openScreen(new ClickGui()));
        }
    }
}
