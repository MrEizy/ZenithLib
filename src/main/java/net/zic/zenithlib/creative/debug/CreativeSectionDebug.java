package net.zic.zenithlib.creative.debug;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.zic.zenithlib.ZenithLib;
import net.zic.zenithlib.creative.api.CreativeTabSection;
import net.zic.zenithlib.creative.api.CreativeTabSections;

public final class CreativeSectionDebug {
    private static boolean registered;

    private CreativeSectionDebug() {}

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;

        CreativeTabSections.register(
                CreativeModeTabs.BUILDING_BLOCKS,
                section("logs", "creative_section.zenithlib.debug.logs", Items.OAK_LOG)
                        .matchingTag(ItemTags.LOGS)
                        .order(10)
                        .build(),
                section("planks", "creative_section.zenithlib.debug.planks", Items.OAK_PLANKS)
                        .matchingTag(ItemTags.PLANKS)
                        .order(20)
                        .build(),
                section("slabs", "creative_section.zenithlib.debug.slabs", Items.STONE_SLAB)
                        .matching(stack -> stack.getItem() instanceof BlockItem blockItem
                                && blockItem.getBlock() instanceof SlabBlock)
                        .order(30)
                        .build(),
                section("stairs", "creative_section.zenithlib.debug.stairs", Items.OAK_STAIRS)
                        .matching(stack -> stack.getItem() instanceof BlockItem blockItem
                                && blockItem.getBlock() instanceof StairBlock)
                        .order(40)
                        .build(),
                section("fences", "creative_section.zenithlib.debug.fences", Items.OAK_FENCE)
                        .matching(stack -> stack.getItem() instanceof BlockItem blockItem
                                && blockItem.getBlock() instanceof FenceBlock)
                        .order(50)
                        .build()

                //
        );
    }

    private static CreativeTabSection.Builder section(String path, String translationKey, Item item) {
        return CreativeTabSection.builder(Identifier.fromNamespaceAndPath(ZenithLib.MOD_ID, "debug/" + path))
                .title(Component.translatable(translationKey))
                .icon(item::getDefaultInstance);
    }
}
