package net.zic.zenithlib.datagen;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.zic.zenithlib.ZenithLib;

public class ZenithLanguageProvider extends LanguageProvider {

    public ZenithLanguageProvider(PackOutput output) {
        super(output, ZenithLib.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {

        add("tooltip.zenithlib.diamond.title", "Zenith Diamond");
        add("tooltip.zenithlib.diamond.header", "Diamond Tooltip");
        add("tooltip.zenithlib.diamond.line_1", "A tooltip rendered through ZenithLib.");
        add("tooltip.zenithlib.diamond.line_2", "Themes, pages, icons, and gradients come next.");
        add("tooltip.zenithlib.diamond.row.rarity", "Rarity");
        add("tooltip.zenithlib.diamond.row.rarity_value", "Rare");
        add("tooltip.zenithlib.diamond.row.origin", "Origin");
        add("tooltip.zenithlib.diamond.row.origin_value", "Vanilla");
        add("tooltip.zenithlib.diamond.page_2.title", "Zenith Details");
        add("tooltip.zenithlib.diamond.page_2.header", "Theme System");
        add("tooltip.zenithlib.diamond.page_2.line_1", "This is the second tooltip page.");
        add("tooltip.zenithlib.diamond.page_2.line_2", "Use the navigation keys to cycle pages.");
        add("tooltip.zenithlib.diamond.page_3.title", "Zenith Notes");
        add("tooltip.zenithlib.diamond.page_3.header", "Renderer Notes");
        add("tooltip.zenithlib.diamond.page_3.line_1", "This is the third tooltip page.");
        add("tooltip.zenithlib.diamond.page_3.line_2", "Cycle again to return to the first page.");
        add("tooltip.zenithlib.diamond.page_3.row.note", "Status");
        add("tooltip.zenithlib.diamond.page_3.row.note_value", "Working");

        add("tooltip.zenithlib.vanilla.converted", "Converted Vanilla Tooltip");
        add("tooltip.zenithlib.vanilla.enchantments", "Enchantments");
        add("tooltip.zenithlib.vanilla.stats", "Stats");
        add("tooltip.zenithlib.vanilla.details", "Details");
        add("tooltip.zenithlib.page_hint_navigation", "Page %1$s/%2$s  %3$s / %4$s");
        add("tooltip.zenithlib.page_hint_navigation_scroll", "Page %1$s/%2$s  %3$s / %4$s  Scroll");
        add("tooltip.zenithlib.scroll_hint.down", "Scroll down to view more");
        add("tooltip.zenithlib.scroll_hint.up", "Scroll up to view more");
        add("tooltip.zenithlib.scroll_hint.both", "Scroll to view more");
        add("tooltip.zenithlib.vanilla.durability", "Durability");
        add("key.zenithlib.tooltip.previous_page", "ZenithLib: Previous Tooltip Page");
        add("key.zenithlib.tooltip.next_page", "ZenithLib: Next Tooltip Page");
        add("config.zenithlib.tooltips.enabled", "Enable Zenith Tooltips");
        add("zenithlib.configuration.tooltips", "ZenithLib Tooltip Configs");
        add("zenithlib.configuration.animations", "Tooltip Animation Controls");
        add("config.zenithlib.tooltips.spawn_egg_entity_previews", "Show Spawn Egg Entity Previews");
        add("config.zenithlib.tooltips.animations_enabled", "Enable Tooltip Animations");
        add("config.zenithlib.tooltips.reduce_motion", "Reduce Tooltip Motion");
        add("config.zenithlib.tooltips.ambient_effects_enabled", "Enable Tooltip Ambient Effects");
        add("config.zenithlib.tooltips.animation_intensity", "Tooltip Animation Intensity");
        add("config.zenithlib.tooltips.particle_budget", "Tooltip Particle Budget");

    }
}

