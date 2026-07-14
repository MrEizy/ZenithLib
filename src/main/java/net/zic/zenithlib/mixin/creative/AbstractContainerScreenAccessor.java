package net.zic.zenithlib.mixin.creative;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
    @Accessor("leftPos")
    int zenithlib$getLeftPos();

    @Accessor("topPos")
    int zenithlib$getTopPos();

    @Accessor("imageWidth")
    int zenithlib$getImageWidth();
}
