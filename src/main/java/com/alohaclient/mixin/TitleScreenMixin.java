package com.alohaclient.mixin;

import com.alohaclient.gui.CustomTitleScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void onInit(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        client.openScreen(new CustomTitleScreen());
        ci.cancel();
    }
}
