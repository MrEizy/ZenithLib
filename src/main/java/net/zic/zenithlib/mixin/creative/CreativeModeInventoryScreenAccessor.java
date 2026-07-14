package net.zic.zenithlib.mixin.creative;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Collection;

@Mixin(CreativeModeInventoryScreen.class)
public interface CreativeModeInventoryScreenAccessor {
    @Accessor("selectedTab")
    static CreativeModeTab zenithlib$getSelectedTab() {
        throw new AssertionError();
    }

    @Accessor("scrollOffs")
    void zenithlib$setScrollOffset(float scrollOffset);

    @Invoker("refreshCurrentTabContents")
    void zenithlib$refreshCurrentTabContents(Collection<ItemStack> displayList);
}
