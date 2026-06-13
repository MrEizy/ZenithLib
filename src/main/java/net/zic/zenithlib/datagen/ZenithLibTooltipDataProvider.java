package net.zic.zenithlib.datagen;

import net.minecraft.data.PackOutput;
import net.zic.zenithlib.ZenithLib;
import net.zic.zenithlib.tooltip.api.ZenithTooltipColor;
import net.zic.zenithlib.tooltip.api.ZenithTooltipTheme;
import net.zic.zenithlib.tooltip.api.animation.ZenithTooltipPresets;
import net.zic.zenithlib.tooltip.api.builder.ZenithTooltipTemplateBuilder;
import net.zic.zenithlib.tooltip.api.builder.ZenithTooltipTemplates;
import net.zic.zenithlib.tooltip.datagen.ZenithTooltipDataProvider;

import static net.zic.zenithlib.tooltip.api.builder.ZenithTooltipBuilders.*;

/**
 * A temporary setup that just generates the showcase tooltips I made, and the themes.
 * I will add actual tooltips to items later, but this is a layout guide for you guys :)
 *
 * <p>Current Showcase Items:
 *      Diamond - A basic custom tooltip
 *      Emerald - Text Animation Effects
 *      Amethyst Shard - The Corrupted Animation Preset
 *      Echo Shard - iconTitleSummary Template
 *      Nether Star - Celestial Animation Preset
 *      Slime Ball - statCard Template
 *      Redstone Dust - Segmented Bars/ Gauge Extremes
 *      Lapis Lazuli - Scrolling long page
 *      Gold Ingot - progressDisplay Template
 *      Iron Sword - Dynamic item tooltip
 *      Book - Lore, scramble reveal, Living Animation Preset</p>
*/

public final class ZenithLibTooltipDataProvider extends ZenithTooltipDataProvider {
    public ZenithLibTooltipDataProvider(PackOutput output) {
        super(output, ZenithLib.MOD_ID);
    }

    @Override
    protected void addTooltips() {
        addShowcaseTemplates();
        addShowcaseRules();
        addThemes();
    }

    private void addShowcaseTemplates() {
        addMaterialShowcaseTemplate();
        addLoreShowcaseTemplate();
        addTextAnimationLabTemplate();
        addCelestialPresetTemplate();
        addCorruptedPresetTemplate();
        addMechanicalGaugeTemplate();
        addScrollableSheetTemplate();
        addTemplateBuilderShowcases();
        addDynamicItemTemplate();
    }

    private void addMaterialShowcaseTemplate() {
        template(id("showcase_material"))
                .animationPreset(ZenithTooltipPresets.CELESTIAL)
                .page(page(translated("tooltip.zenithlib.diamond.title"))
                        .add(titleIcon(
                                translated("tooltip.zenithlib.diamond.title"),
                                translated("tooltip.zenithlib.diamond.header")
                        ))
                        .add(badge(literal("RARE MATERIAL"), ZenithTooltipColor.ACCENT))
                        .add(text(translated("tooltip.zenithlib.diamond.line_1")))
                        .add(divider())
                        .add(row(
                                translated("tooltip.zenithlib.diamond.row.rarity"),
                                translated("tooltip.zenithlib.diamond.row.rarity_value"),
                                ZenithTooltipColor.TEXT,
                                ZenithTooltipColor.POSITIVE
                        ))
                        .add(row(
                                translated("tooltip.zenithlib.diamond.row.origin"),
                                translated("tooltip.zenithlib.diamond.row.origin_value"),
                                ZenithTooltipColor.TEXT,
                                ZenithTooltipColor.ACCENT
                        ))
                        .add(divider())
                        .add(text(
                                literal("This compact card shows the default authored layout: title icon, badge, rows, divider, and theme colours."),
                                ZenithTooltipColor.MUTED
                        )))
                .page(page(literal("Authoring Notes"))
                        .add(header(literal("Stable Layout"), ZenithTooltipColor.ACCENT))
                        .add(text(literal("The celestial preset adds stars, border energy, frame assembly, icon float, and divider sweeps without changing measured layout bounds.")))
                        .add(text(
                                literal("Good for dependent mods that want a polished tooltip without hand-authoring every animation."),
                                ZenithTooltipColor.MUTED
                        )));
    }

    private void addLoreShowcaseTemplate() {
        template(id("showcase_lore"))
                .animationPreset(ZenithTooltipPresets.LIVING)
                .page(page(translated("tooltip.zenithlib.diamond.page_2.title"))
                        .add(header(translated("tooltip.zenithlib.diamond.page_2.header"), ZenithTooltipColor.ACCENT))
                        .add(text(translated("tooltip.zenithlib.diamond.page_2.line_1")))
                        .add(text(
                                literal("Sometimes I do not care. Sometimes I wish to tell them this world they take for truth is merely an illusion, and that they see so little of reality in their long dream."),
                                ZenithTooltipColor.TEXT,
                                scrambleReveal(0.55F, 20)
                        ))
                        .add(spacer(4))
                        .add(text(translated("tooltip.zenithlib.diamond.page_2.line_2"), ZenithTooltipColor.ACCENT)));
    }

    private void addTextAnimationLabTemplate() {
        template(id("showcase_text_lab"))
                .animationPreset(ZenithTooltipPresets.CORRUPTED)
                .page(page(literal("Text Animation Lab"))
                        .add(header(literal("Living Lettering"), ZenithTooltipColor.ACCENT))
                        .add(text(
                                literal("A full-spectrum gradient travels through every glyph."),
                                ZenithTooltipColor.TEXT,
                                combine(rainbow(), shimmer())
                        ))
                        .add(text(
                                literal("Each letter takes its turn hopping through the wave."),
                                ZenithTooltipColor.ACCENT,
                                wave()
                        ))
                        .add(text(
                                literal("Prismatic resonance"),
                                ZenithTooltipColor.TEXT,
                                combine(rainbow(2000, 0.055F), shimmer(), wave(850, 6.0F, 2))
                        ))
                        .add(divider())
                        .add(text(
                                literal("A bounded hue range recreates the old ping-pong gradient."),
                                ZenithTooltipColor.TEXT,
                                gradient(2600, 0.035F, 0.52F, 0.82F)
                        ))
                        .add(text(
                                literal("Typed once per page entry, with layout width held steady."),
                                ZenithTooltipColor.TEXT,
                                typewriter(850, 120)
                        ))
                        .add(text(
                                literal("ᚱ Rune deciphering settles into readable text."),
                                ZenithTooltipColor.ACCENT,
                                runeDecipher(1100, 80, 42)
                        )))
                .page(page(literal("Composed Effects"))
                        .add(header(literal("Stacked Glyph Passes"), ZenithTooltipColor.ACCENT))
                        .add(text(
                                literal("Shimmer, wave, and gradient can be layered without changing the wrapped width."),
                                ZenithTooltipColor.TEXT,
                                combine(shimmer(1800, 0.16F, 0.55F), wave(1200, 8.5F, 1), gradient(3200, 0.03F, 0.74F, 0.92F))
                        ))
                        .add(text(
                                literal("Rune and typewriter effects restart when this page is entered."),
                                ZenithTooltipColor.MUTED,
                                runeDecipher(900, 60, 36)
                        )));
    }

    private void addCelestialPresetTemplate() {
        template(id("showcase_celestial_preset"))
                .animationPreset(ZenithTooltipPresets.CELESTIAL)
                .page(page(literal("Celestial Preset"))
                        .add(titleIcon(literal("Celestial"), literal("Preset composition demo")))
                        .add(badge(literal("PRESET • STARS • FRAME"), ZenithTooltipColor.ACCENT))
                        .add(text(literal("A theme can stay visual, while a preset adds motion identity: faint stars, calm border energy, title shimmer, divider sweeps, icon float, and a restrained opening.")))
                        .add(divider())
                        .add(text(
                                literal("Preset effects are ordinary reusable pieces. Dependent mods can register their own namespaced presets later."),
                                ZenithTooltipColor.MUTED
                        )));
    }

    private void addCorruptedPresetTemplate() {
        template(id("showcase_corrupted_preset"))
                .animationPreset(ZenithTooltipPresets.CORRUPTED)
                .page(page(literal("Corrupted Preset"))
                        .add(titleIcon(literal("Corrupted Signal"), literal("Restrained instability demo")))
                        .add(badge(
                                literal("UNSTABLE"),
                                ZenithTooltipColor.BACKGROUND,
                                ZenithTooltipColor.NEGATIVE,
                                ZenithTooltipColor.NEGATIVE
                        ))
                        .add(text(
                                literal("The frame hums, mist drifts, and text can glitch without forcing the whole tooltip to become unreadable."),
                                ZenithTooltipColor.TEXT,
                                combine(scrambleReveal(0.7F, 28), shimmer(2600, 0.13F, 0.45F))
                        ))
                        .add(divider())
                        .add(row(literal("Signal"), literal("Fractured"), ZenithTooltipColor.TEXT, ZenithTooltipColor.NEGATIVE))
                        .add(row(literal("Containment"), literal("Partial"), ZenithTooltipColor.TEXT, ZenithTooltipColor.WARNING)));
    }

    private void addMechanicalGaugeTemplate() {
        template(id("showcase_mechanical_gauges"))
                .animationPreset(ZenithTooltipPresets.MECHANICAL)
                .page(page(literal("Gauge Preview"))
                        .add(header(literal("Bars"), ZenithTooltipColor.WARNING))
                        .add(text(literal("Bars keep their logical value separate from segmented charging, scan lines, and edge sparks. These are placeholder values.")))
                        .add(divider())
                        .add(bar(literal("Integrity"), 78, 100, literal("78%"), ZenithTooltipColor.POSITIVE))
                        .add(bar(literal("Stored Qi"), 42, 100, ZenithTooltipColor.ACCENT))
                        .add(bar(literal("Impurities"), 16, 100, ZenithTooltipColor.NEGATIVE)))
                .page(page(literal("Value Extremes"))
                        .add(header(literal("Low / High Checks"), ZenithTooltipColor.ACCENT))
                        .add(bar(literal("Almost Empty"), 1, 100, literal("1 / 100"), ZenithTooltipColor.NEGATIVE))
                        .add(bar(literal("Half Charge"), 50, 100, literal("50 / 100"), ZenithTooltipColor.WARNING))
                        .add(bar(literal("Overflow Test"), 100, 100, literal("Full"), ZenithTooltipColor.POSITIVE)));
    }

    private void addScrollableSheetTemplate() {
        template(id("showcase_scrollable_sheet"))
                .animationPreset(ZenithTooltipPresets.CELESTIAL)
                .page(page(literal("Scrollable Detail Sheet"))
                        .add(header(literal("Long Tooltip Preview"), ZenithTooltipColor.ACCENT))
                        .add(badge(
                                literal("EXPERIMENTAL • PLACEHOLDER"),
                                ZenithTooltipColor.BACKGROUND,
                                ZenithTooltipColor.WARNING,
                                ZenithTooltipColor.WARNING
                        ))
                        .add(text(literal("This page exceeds the theme height. Use the mouse wheel while hovering to browse the body without losing its heading or page controls.")))
                        .add(divider())
                        .add(header(literal("Attunement"), ZenithTooltipColor.TEXT))
                        .add(bar(literal("Alignment"), 91, 100, ZenithTooltipColor.POSITIVE))
                        .add(bar(literal("Stored Qi"), 63, 100, ZenithTooltipColor.ACCENT))
                        .add(bar(literal("Karma"), 12, 100, ZenithTooltipColor.NEGATIVE))
                        .add(divider())
                        .add(header(literal("Requirements"), ZenithTooltipColor.WARNING))
                        .add(row(literal("Cultivation Realm"), literal("Golden Core"), ZenithTooltipColor.TEXT, ZenithTooltipColor.POSITIVE))
                        .add(row(literal("Strength"), literal("12"), ZenithTooltipColor.TEXT, ZenithTooltipColor.POSITIVE))
                        .add(row(literal("Agility"), literal("8"), ZenithTooltipColor.TEXT, ZenithTooltipColor.POSITIVE))
                        .add(row(literal("Intelligence"), literal("92"), ZenithTooltipColor.TEXT, ZenithTooltipColor.NEGATIVE))
                        .add(divider())
                        .add(header(literal("Renderer Notes"), ZenithTooltipColor.ACCENT))
                        .add(text(literal("This is mostly here to test clipped pages, page controls, wheel scrolling, dividers, rows, bars, and staged entry in one place.")))
                        .add(text(
                                literal("Damageable converted items can display live durability bars, while authored documents can use bars for any lore or resource value."),
                                ZenithTooltipColor.MUTED
                        )));
    }

    private void addTemplateBuilderShowcases() {
        ZenithTooltipTemplateBuilder iconSummary = template(id("showcase_template_icon_summary"))
                .animationPreset(ZenithTooltipPresets.CELESTIAL);
        addTemplatePages(iconSummary, ZenithTooltipTemplates.iconTitleSummary(
                literal("Template: Icon Summary"),
                literal("Built with ZenithTooltipTemplates.iconTitleSummary"),
                literal("This document was generated through a beginner-friendly template helper, then assigned a normal animation preset and theme by the rule.")
        ));
        iconSummary.page(page(literal("Custom Page Added After Template"))
                .add(header(literal("Still Just Normal Pages"), ZenithTooltipColor.ACCENT))
                .add(text(literal("Template builders do not create a parallel system. They simply emit regular pages and elements that can be extended afterwards.")))
                .add(divider())
                .add(row(literal("Template"), literal("iconTitleSummary"), ZenithTooltipColor.TEXT, ZenithTooltipColor.POSITIVE)));

        ZenithTooltipTemplateBuilder statCard = template(id("showcase_template_stat_card"))
                .animationPreset(ZenithTooltipPresets.LIVING);
        addTemplatePages(statCard, ZenithTooltipTemplates.statCard(
                literal("Template: Stat Card"),
                literal("Growth"),
                literal("+12%"),
                literal("A compact generated page for a single stat, modifier, perk, or item trait.")
        ));
        statCard.page(page(literal("Inserted Rows"))
                .add(header(literal("Manual Extension"), ZenithTooltipColor.ACCENT))
                .add(row(literal("Affinity"), literal("Verdant"), ZenithTooltipColor.TEXT, ZenithTooltipColor.POSITIVE))
                .add(row(literal("Stability"), literal("Breathing"), ZenithTooltipColor.TEXT, ZenithTooltipColor.ACCENT))
                .add(text(literal("The template helper handled the basic shape; authored rows add the flavour."), ZenithTooltipColor.MUTED)));

        ZenithTooltipTemplateBuilder progress = template(id("showcase_template_progress"))
                .animationPreset(ZenithTooltipPresets.MECHANICAL);
        addTemplatePages(progress, ZenithTooltipTemplates.progressDisplay(
                literal("Template: Progress"),
                literal("Assembly"),
                64,
                100,
                literal("64%"),
                ZenithTooltipColor.WARNING
        ));
        progress.page(page(literal("Mechanical Additions"))
                .add(header(literal("Segmented Motion"), ZenithTooltipColor.WARNING))
                .add(text(literal("The mechanical preset can add segmented bar animation and border scans while the bar value remains exactly 64%.")))
                .add(bar(literal("Calibration"), 33, 100, literal("33 / 100"), ZenithTooltipColor.ACCENT)));

        ZenithTooltipTemplateBuilder requirements = template(id("showcase_template_requirements"))
                .animationPreset(ZenithTooltipPresets.CORRUPTED);
        addTemplatePages(requirements, ZenithTooltipTemplates.requirementsDisplay(
                literal("Template: Requirements"),
                literal("Entry Conditions"),
                literal("Void Clearance"),
                literal("Missing")
        ));
        requirements.page(page(literal("Extra Requirements"))
                .add(header(literal("Expanded Check List"), ZenithTooltipColor.WARNING))
                .add(row(literal("Rank"), literal("Adept"), ZenithTooltipColor.TEXT, ZenithTooltipColor.POSITIVE))
                .add(row(literal("Clearance"), literal("Denied"), ZenithTooltipColor.TEXT, ZenithTooltipColor.NEGATIVE))
                .add(row(literal("Stability"), literal("Uncertain"), ZenithTooltipColor.TEXT, ZenithTooltipColor.WARNING))
                .add(text(literal("A useful pattern for dependent mods that need conditional sections later."), ZenithTooltipColor.MUTED)));
    }

    private void addDynamicItemTemplate() {
        template(id("showcase_dynamic_item"))
                .animationPreset(ZenithTooltipPresets.MECHANICAL)
                .page(page(literal("Dynamic Item Tooltip"))
                        .add(titleIcon(sourced("item_name"), literal("Uses built-in value sources")))
                        .add(text(literal("This card demonstrates the simple dependent-mod style: authored elements mixed with data-driven values from the hovered stack.")))
                        .add(divider())
                        .add(row(literal("Item ID"), sourced("item_id"), ZenithTooltipColor.TEXT, ZenithTooltipColor.ACCENT))
                        .add(durabilityBar(literal("Live Durability"), ZenithTooltipColor.POSITIVE)));
    }

    private void addTemplatePages(ZenithTooltipTemplateBuilder target, ZenithTooltipTemplateBuilder source) {
        source.build().pages().forEach(target::page);
    }

    private void addShowcaseRules() {
        addShowcaseRule("diamond", "showcase_material", "mana_blue");
        addShowcaseRule("emerald", "showcase_text_lab", "arcane_purple");
        addShowcaseRule("echo_shard", "showcase_corrupted_preset", "ember_orange");
        addShowcaseRule("amethyst_shard", "showcase_template_icon_summary", "moonlit_white");
        addShowcaseRule("nether_star", "showcase_celestial_preset", "glass_clear");
        addShowcaseRule("slime_ball", "showcase_template_stat_card", "verdant_green");
        addShowcaseRule("redstone", "showcase_mechanical_gauges", "crimson_red");
        addShowcaseRule("lapis_lazuli", "showcase_scrollable_sheet", "cobalt_blue");
        addShowcaseRule("gold_ingot", "showcase_template_progress", "ember_orange");
        addShowcaseRule("ender_pearl", "showcase_template_requirements", "arcane_purple");
        addShowcaseRule("iron_sword", "showcase_dynamic_item", "mana_blue");
        addShowcaseRule("book", "showcase_lore", "cobalt_blue");
    }

    private void addShowcaseRule(String itemPath, String documentPath, String themePath) {
        rule(id(itemPath))
                .priority(100)
                .items(minecraft(itemPath))
                .document(id(documentPath))
                .theme(id(themePath));
    }

    private void addThemes() {
        addManaBlueTheme();
        addArcanePurpleTheme();
        addEmberOrangeTheme();
        addMoonlitWhiteTheme();
        addGlassClearTheme();
        addVerdantGreenTheme();
        addCrimsonRedTheme();
        addCobaltBlueTheme();
    }

    private void addManaBlueTheme() {
        theme(id("mana_blue"))
                .colors("#071426DD", "#5CCBFFFF", "#1B4F8AFF", "#E6F7FFFF", "#5CCBFFFF", "#7CA8C0FF", "#85FF9AFF", "#FFD166FF", "#FF6B6BFF")
                .layout(6, 240, 220, 3, 1)
                .iconHolder(ZenithTooltipTheme.Shape.DIAMOND, 31, 2, 8, "accent", "background", 85)
                .barStyle(5, 2, "muted", 70, "border_bottom", 1, 255)
                .badgeStyle(5, 2, 1, 235)
                .dividerStyle(1, 2, 3, "accent", ZenithTooltipTheme.Decoration.DOUBLE_DIAMOND)
                .frameStyle(ZenithTooltipTheme.CornerDecoration.BRACKET, 5, 2, "accent", true, 2, "border_top", 92)
                .headerStyle(ZenithTooltipTheme.Ornament.SMALL_DIAMONDS, "accent")
                .backgroundStyle(ZenithTooltipTheme.Pattern.DIAGONAL_LINES, "accent", 12, 9);
    }

    private void addArcanePurpleTheme() {
        theme(id("arcane_purple"))
                .colors("#170820DD", "#B967FFFF", "#4A1B77FF", "#EADFFFFF", "#D49AFFFF", "#B49AC8FF", "#85FF9AFF", "#FFD166FF", "#FF6B7AFF")
                .layout(6, 240, 220, 3, 1)
                .iconHolder(ZenithTooltipTheme.Shape.GEM, 31, 2, 8, "accent", "background", 96)
                .barStyle(4, 2, "muted", 58, "border_bottom", 0, 240)
                .badgeStyle(6, 2, 1, 205)
                .dividerStyle(1, 2, 3, "accent", ZenithTooltipTheme.Decoration.CENTER_RUNE)
                .frameStyle(ZenithTooltipTheme.CornerDecoration.RUNE, 5, 2, "accent", true, 2, "accent", 82)
                .headerStyle(ZenithTooltipTheme.Ornament.SMALL_DIAMONDS, "accent")
                .backgroundStyle(ZenithTooltipTheme.Pattern.RUNES, "accent", 18, 12);
    }

    private void addEmberOrangeTheme() {
        theme(id("ember_orange"))
                .colors("#211006DD", "#FFB45CFF", "#9A3F12FF", "#FFF0D6FF", "#FFB45CFF", "#C69A74FF", "#B9F27CFF", "#FFD166FF", "#FF765CFF")
                .layout(6, 240, 220, 3, 1)
                .iconHolder(ZenithTooltipTheme.Shape.BRACKET, 29, 2, 8, "accent", "background", 96)
                .barStyle(6, 2, "border_bottom", 115, "accent", 1, 255)
                .badgeStyle(5, 2, 2, 230)
                .dividerStyle(2, 2, 3, "accent", ZenithTooltipTheme.Decoration.DOTTED)
                .frameStyle(ZenithTooltipTheme.CornerDecoration.BRACKET, 5, 2, "accent", false, 2, "border_top", 110)
                .headerStyle(ZenithTooltipTheme.Ornament.CORNER_TICKS, "warning")
                .backgroundStyle(ZenithTooltipTheme.Pattern.DIAGONAL_LINES, "warning", 10, 10);
    }

    private void addMoonlitWhiteTheme() {
        theme(id("moonlit_white"))
                .colors("#F8F4E8DD", "#FFFFFFFF", "#B8B1A3FF", "#2A2530FF", "#356F9DFF", "#716979FF", "#237544FF", "#946315FF", "#A53845FF")
                .layout(6, 240, 220, 3, 1)
                .iconHolder(ZenithTooltipTheme.Shape.OCTAGON, 27, 2, 8, "accent", "background", 72)
                .barStyle(3, 2, "muted", 55, "border_bottom", 0, 215)
                .badgeStyle(6, 1, 1, 155)
                .dividerStyle(1, 2, 3, "accent", ZenithTooltipTheme.Decoration.DIAMOND)
                .frameStyle(ZenithTooltipTheme.CornerDecoration.BRACKET, 7, 2, "accent", true, 2, "border_bottom", 86)
                .headerStyle(ZenithTooltipTheme.Ornament.SIDE_LINES, "accent")
                .backgroundStyle(ZenithTooltipTheme.Pattern.GRID, "muted", 10, 11);
    }

    private void addGlassClearTheme() {
        theme(id("glass_clear"))
                .colors("#0B1020A8", "#DFF8FFFF", "#7CA8C0CC", "#F3FBFFFF", "#DFF8FFFF", "#A8C4D6FF", "#A8FFD0FF", "#FFE49AFF", "#FF9AA7FF")
                .layout(6, 240, 220, 3, 1)
                .iconHolder(ZenithTooltipTheme.Shape.NONE, 18, 0, 7, "accent", "background", 0)
                .barStyle(3, 2, "muted", 45, "border_bottom", 0, 190)
                .badgeStyle(5, 1, 1, 125)
                .dividerStyle(1, 2, 3, "muted", ZenithTooltipTheme.Decoration.NONE)
                .frameStyle(ZenithTooltipTheme.CornerDecoration.NONE, 6, 2, "accent", true, 2, "border_top", 76)
                .headerStyle(ZenithTooltipTheme.Ornament.SIDE_LINES, "muted")
                .backgroundStyle(ZenithTooltipTheme.Pattern.STARS, "accent", 10, 12);
    }

    private void addVerdantGreenTheme() {
        theme(id("verdant_green"))
                .colors("#071A12DD", "#67E89AFF", "#16613BFF", "#E8FFF0FF", "#67E89AFF", "#83BFA0FF", "#B1FF7AFF", "#FFE083FF", "#FF7474FF")
                .layout(6, 240, 220, 3, 1)
                .iconHolder(ZenithTooltipTheme.Shape.CIRCLE, 29, 2, 8, "accent", "background", 88)
                .barStyle(5, 2, "muted", 60, "border_bottom", 1, 250)
                .badgeStyle(5, 2, 1, 215)
                .dividerStyle(1, 2, 3, "positive", ZenithTooltipTheme.Decoration.DOTTED)
                .frameStyle(ZenithTooltipTheme.CornerDecoration.BRACKET, 7, 2, "positive", true, 2, "border_top", 92)
                .headerStyle(ZenithTooltipTheme.Ornament.CORNER_TICKS, "positive")
                .backgroundStyle(ZenithTooltipTheme.Pattern.GRID, "positive", 10, 10);
    }

    private void addCrimsonRedTheme() {
        theme(id("crimson_red"))
                .colors("#21090CDD", "#FF6976FF", "#7F1D2AFF", "#FFECEEFF", "#FF6976FF", "#C18C94FF", "#86EFA0FF", "#FFD166FF", "#FF4760FF")
                .layout(6, 240, 220, 3, 1)
                .iconHolder(ZenithTooltipTheme.Shape.DIAMOND, 31, 2, 8, "accent", "background", 92)
                .barStyle(6, 2, "muted", 74, "border_bottom", 1, 255)
                .badgeStyle(6, 2, 2, 235)
                .dividerStyle(1, 2, 3, "accent", ZenithTooltipTheme.Decoration.DOUBLE_DIAMOND)
                .frameStyle(ZenithTooltipTheme.CornerDecoration.BRACKET, 5, 2, "accent", true, 2, "border_bottom", 96)
                .headerStyle(ZenithTooltipTheme.Ornament.SMALL_DIAMONDS, "warning")
                .backgroundStyle(ZenithTooltipTheme.Pattern.DIAGONAL_LINES, "accent", 12, 8);
    }

    private void addCobaltBlueTheme() {
        theme(id("cobalt_blue"))
                .colors("#091226DD", "#6D95FFFF", "#263E92FF", "#EDF2FFFF", "#7DA4FFFF", "#8C9FCFFF", "#8EF0A6FF", "#FFD875FF", "#FF7782FF")
                .layout(6, 240, 220, 3, 1)
                .iconHolder(ZenithTooltipTheme.Shape.OCTAGON, 29, 2, 8, "accent", "background", 96)
                .barStyle(4, 1, "muted", 72, "border_bottom", 1, 255)
                .badgeStyle(5, 2, 1, 220)
                .dividerStyle(1, 2, 3, "accent", ZenithTooltipTheme.Decoration.CENTER_RUNE)
                .frameStyle(ZenithTooltipTheme.CornerDecoration.BRACKET, 5, 2, "accent", true, 2, "border_top", 88)
                .headerStyle(ZenithTooltipTheme.Ornament.SIDE_LINES, "accent")
                .backgroundStyle(ZenithTooltipTheme.Pattern.STARS, "accent", 12, 11);
    }
}
