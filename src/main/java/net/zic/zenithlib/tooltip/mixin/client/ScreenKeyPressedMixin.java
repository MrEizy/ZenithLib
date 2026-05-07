package net.zic.zenithlib.tooltip.mixin.client;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.zic.zenithlib.tooltip.client.render.TooltipKeybinds;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public abstract class ScreenKeyPressedMixin {

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void zenithlib$tooltipKeyPressed(KeyEvent keyEvent,
                                             CallbackInfoReturnable<Boolean> cir) {
        if (TooltipKeybinds.handleScreenKeyPressed(keyEvent.key(), keyEvent.scancode())) {
            cir.setReturnValue(true);
        }
    }
}