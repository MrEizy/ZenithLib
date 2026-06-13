package net.zic.zenithlib.datagen;

import net.minecraft.data.PackOutput;
import net.zic.zenithlib.ZenithLib;
import net.zic.zenithlib.tooltip.api.ZenithTooltipColor;
import net.zic.zenithlib.tooltip.api.animation.ZenithTooltipPresets;
import net.zic.zenithlib.tooltip.api.builder.ZenithTooltipTemplateBuilder;
import net.zic.zenithlib.tooltip.api.builder.ZenithTooltipTemplates;
import net.zic.zenithlib.tooltip.datagen.ZenithTooltipDataProvider;

import static net.zic.zenithlib.tooltip.api.builder.ZenithTooltipBuilders.*;

/**
 * Generates temporary showcase tooltip documents and selector rules.
 *
 * <p>Current Showcase Items:
 *      Diamond - A basic custom tooltip
 *      Emerald - Text Animation Effects
 *      Amethyst Shard - The Corrupted Animation Preset
 *      Echo Shard - iconTitleSummary Template
 *      Nether Star - Nebula Animation Preset
 *      Slime Ball - statCard Template
 *      Redstone Dust - Segmented Bars/ Gauge Extremes
 *      Lapis Lazuli - Scrolling long page
 *      Gold Ingot - progressDisplay Template
 *      Ender Pearl - requirementsDisplay Template
 *      Iron Sword - Dynamic item tooltip
 *      Book - Lore, scramble reveal, Living Animation Preset
 *      Prismarine Crystals - Background Animations
 *      Quartz - Border and Divider Animations
 *      Blaze Powder - Gauge Motions
 *      Clock - Page Motions </p>
*/

public final class ZenithLibTooltipDataProvider extends ZenithTooltipDataProvider {
    public ZenithLibTooltipDataProvider(PackOutput output) {
        super(output, ZenithLib.MOD_ID);
    }

    @Override
    public String getName() {
        return "ZenithLib tooltip showcase definitions";
    }


    @Override
    protected void addTooltips() {
        addShowcaseTemplates();
        addShowcaseRules();
    }

    private void addShowcaseTemplates() {
        addMaterialShowcaseTemplate();
        addLoreShowcaseTemplate();
        addTextAnimationLabTemplate();
        addCelestialPresetTemplate();
        addCorruptedPresetTemplate();
        addMechanicalGaugeTemplate();
        addBackgroundAnimationLabTemplate();
        addFrameDividerAnimationLabTemplate();
        addGaugeMotionLabTemplate();
        addPageMotionLabTemplate();
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
                                literal("A compact card with a title icon, badge, rows, divider, and the mana-blue theme."),
                                ZenithTooltipColor.MUTED
                        )))
                .page(page(literal("Celestial Motion"))
                        .add(header(literal("Quiet Motion"), ZenithTooltipColor.ACCENT))
                        .add(text(literal("Stars shimmer behind the card while the frame gathers at the edges.")))
                        .add(text(
                                literal("The motion stays decorative: no extra space, no shifting rows."),
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
                                literal("ᚱ Runes collapse into a clean inscription."),
                                ZenithTooltipColor.ACCENT,
                                runeDecipher(1100, 80, 42)
                        )))
                .page(page(literal("Composed Effects"))
                        .add(header(literal("Stacked Glyph Passes"), ZenithTooltipColor.ACCENT))
                        .add(text(
                                literal("Shimmer, wave, and gradient braid together across the same line."),
                                ZenithTooltipColor.TEXT,
                                combine(shimmer(1800, 0.16F, 0.55F), wave(1200, 8.5F, 1), gradient(3200, 0.03F, 0.74F, 0.92F))
                        ))
                        .add(text(
                                literal("Runes spark first, then settle when the page opens."),
                                ZenithTooltipColor.MUTED,
                                runeDecipher(900, 60, 36)
                        )));
    }

    private void addCelestialPresetTemplate() {
        template(id("showcase_celestial_preset"))
                .animationPreset(ZenithTooltipPresets.NEBULA)
                .page(page(literal("Celestial Preset"))
                        .add(titleIcon(literal("Celestial"), literal("stars, frame, and soft bloom")))
                        .add(badge(literal("PRESET • STARS • FRAME"), ZenithTooltipColor.ACCENT))
                        .add(text(literal("Stars drift behind the glass while the frame draws a slow orbit of light.")))
                        .add(divider())
                        .add(text(
                                literal("The glow is calm enough for lore pages and rare drops."),
                                ZenithTooltipColor.MUTED
                        )));
    }

    private void addCorruptedPresetTemplate() {
        template(id("showcase_corrupted_preset"))
                .animationPreset(ZenithTooltipPresets.RUNIC)
                .page(page(literal("Corrupted Preset"))
                        .add(titleIcon(literal("Corrupted Signal"), literal("mist, pulse, and fractured glyphs")))
                        .add(badge(
                                literal("UNSTABLE"),
                                ZenithTooltipColor.BACKGROUND,
                                ZenithTooltipColor.NEGATIVE,
                                ZenithTooltipColor.NEGATIVE
                        ))
                        .add(text(
                                literal("The frame hums, mist drifts, and glyphs fracture without drowning the card in noise."),
                                ZenithTooltipColor.TEXT,
                                combine(scrambleReveal(0.7F, 28), shimmer(2600, 0.13F, 0.45F))
                        ))
                        .add(divider())
                        .add(row(literal("Signal"), literal("Fractured"), ZenithTooltipColor.TEXT, ZenithTooltipColor.NEGATIVE))
                        .add(row(literal("Containment"), literal("Partial"), ZenithTooltipColor.TEXT, ZenithTooltipColor.WARNING)));
    }

    private void addMechanicalGaugeTemplate() {
        template(id("showcase_mechanical_gauges"))
                .animationPreset(ZenithTooltipPresets.KINETIC)
                .page(page(literal("Gauge Preview"))
                        .add(header(literal("Bars"), ZenithTooltipColor.WARNING))
                        .add(text(literal("Segmented motion, scan lines, and edge sparks sit on top of the real bar value.")))
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

    private void addBackgroundAnimationLabTemplate() {
        template(id("showcase_background_lab"))
                .animationPreset(ZenithTooltipPresets.NEBULA)
                .page(page(literal("Background Animation Lab"))
                        .add(titleIcon(literal("Nebula Surface"), literal("Stars, mist, motes, and aurora bands")))
                        .add(text(literal("This card stacks the quieter background effects so you can judge whether they feel magical without turning into visual soup.")))
                        .add(divider())
                        .add(row(literal("Stars"), literal("deterministic twinkle"), ZenithTooltipColor.TEXT, ZenithTooltipColor.ACCENT))
                        .add(row(literal("Mist"), literal("slow horizontal drift"), ZenithTooltipColor.TEXT, ZenithTooltipColor.MUTED))
                        .add(row(literal("Motes"), literal("tiny rising particles"), ZenithTooltipColor.TEXT, ZenithTooltipColor.POSITIVE))
                        .add(row(literal("Aurora"), literal("soft moving bands"), ZenithTooltipColor.TEXT, ZenithTooltipColor.WARNING))
                        .add(divider())
                        .add(text(literal("Reduced motion pares this back to the still card."), ZenithTooltipColor.MUTED)))
                .page(page(literal("Background Stress Page"))
                        .add(header(literal("Stacked With Content"), ZenithTooltipColor.ACCENT))
                        .add(text(literal("The glow stays behind the rows and bars instead of resizing the card.")))
                        .add(bar(literal("Ambient Budget"), 32, 32, literal("32 motes max"), ZenithTooltipColor.ACCENT))
                        .add(bar(literal("Motion Strength"), 100, 100, literal("configurable"), ZenithTooltipColor.POSITIVE)));
    }

    private void addFrameDividerAnimationLabTemplate() {
        template(id("showcase_frame_divider_lab"))
                .animationPreset(ZenithTooltipPresets.RUNIC)
                .page(page(literal("Frame + Divider Lab"))
                        .add(titleIcon(literal("Runic Circuit"), literal("Border pulses, sparks, and animated dividers")))
                        .add(text(literal("This preset layers pulsing inner borders, travelling perimeter energy, tiny corner sparks, divider sweeps, and embedded rune ticks.")))
                        .add(divider())
                        .add(header(literal("Divider Variants"), ZenithTooltipColor.ACCENT))
                        .add(text(literal("A plain divider turns into a tiny rune channel."), ZenithTooltipColor.MUTED))
                        .add(divider())
                        .add(row(literal("Border"), literal("pulse + comet"), ZenithTooltipColor.TEXT, ZenithTooltipColor.WARNING))
                        .add(row(literal("Corners"), literal("spark ticks"), ZenithTooltipColor.TEXT, ZenithTooltipColor.ACCENT)))
                .page(page(literal("Page Transition Check"))
                        .add(header(literal("Slide + Wash"), ZenithTooltipColor.ACCENT))
                        .add(text(literal("Fresh pages slide in with a quick wash of light.")))
                        .add(divider())
                        .add(text(literal("The motion is brief, then the card settles."), ZenithTooltipColor.MUTED)));
    }

    private void addGaugeMotionLabTemplate() {
        template(id("showcase_gauge_motion_lab"))
                .animationPreset(ZenithTooltipPresets.KINETIC)
                .page(page(literal("Gauge Motion Lab"))
                        .add(header(literal("Mechanical Bars"), ZenithTooltipColor.WARNING))
                        .add(text(literal("Kinetic bars combine segmented cells, a moving scanline, pulse overlays, and edge sparks.")))
                        .add(divider())
                        .add(bar(literal("Ignition"), 6, 100, literal("6%"), ZenithTooltipColor.NEGATIVE))
                        .add(bar(literal("Pressure"), 37, 100, literal("37%"), ZenithTooltipColor.WARNING))
                        .add(bar(literal("Charge"), 84, 100, literal("84%"), ZenithTooltipColor.ACCENT))
                        .add(bar(literal("Output"), 100, 100, literal("Stable"), ZenithTooltipColor.POSITIVE)))
                .page(page(literal("Animation Integrity"))
                        .add(header(literal("Value Check"), ZenithTooltipColor.ACCENT))
                        .add(text(literal("The spark rides the edge; the fill width stays locked to the value.")))
                        .add(bar(literal("One Tick"), 1, 100, literal("1 / 100"), ZenithTooltipColor.NEGATIVE))
                        .add(bar(literal("Exact Half"), 50, 100, literal("50 / 100"), ZenithTooltipColor.WARNING))
                        .add(bar(literal("Complete"), 100, 100, literal("100 / 100"), ZenithTooltipColor.POSITIVE)));
    }

    private void addPageMotionLabTemplate() {
        template(id("showcase_page_motion_lab"))
                .animationPreset(ZenithTooltipPresets.KINETIC)
                .page(page(literal("Page Motion Lab"))
                        .add(titleIcon(literal("Opening Sequence"), literal("Bloom, assembly, wash, and slide")))
                        .add(text(literal("Open the card and flip pages to compare bloom, assembly, wash, and slide.")))
                        .add(divider())
                        .add(row(literal("Open"), literal("bloom + frame assembly"), ZenithTooltipColor.TEXT, ZenithTooltipColor.ACCENT))
                        .add(row(literal("Page Entry"), literal("slide + wash"), ZenithTooltipColor.TEXT, ZenithTooltipColor.WARNING)))
                .page(page(literal("Second Page"))
                        .add(header(literal("Fresh Page Entry"), ZenithTooltipColor.ACCENT))
                        .add(text(literal("This page starts its own entry beat, separate from the first hover.")))
                        .add(divider())
                        .add(text(literal("Small offsets, short duration, no jitter."), ZenithTooltipColor.MUTED)))
                .page(page(literal("Third Page"))
                        .add(header(literal("Reduced Motion Check"), ZenithTooltipColor.WARNING))
                        .add(text(literal("Reduced motion drops the slide and wash, leaving the static card.")))
                        .add(bar(literal("Clarity"), 100, 100, literal("steady"), ZenithTooltipColor.POSITIVE)));
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
                        .add(header(literal("Stress Notes"), ZenithTooltipColor.ACCENT))
                        .add(text(literal("Clipped pages, controls, rows, bars, and staged entry share one long scroll.")))
                        .add(text(
                                literal("Converted damageable items use live durability; authored cards can reuse bars for lore, charge, or progress."),
                                ZenithTooltipColor.MUTED
                        )));
    }

    private void addTemplateBuilderShowcases() {
        ZenithTooltipTemplateBuilder iconSummary = template(id("showcase_template_icon_summary"))
                .animationPreset(ZenithTooltipPresets.CELESTIAL);
        addTemplatePages(iconSummary, ZenithTooltipTemplates.iconTitleSummary(
                literal("Template: Icon Summary"),
                literal("Icon, title, and summary"),
                literal("The helper creates the opening page; the rule supplies the theme and preset.")
        ));
        iconSummary.page(page(literal("Custom Page Added After Template"))
                .add(header(literal("Still Just Normal Pages"), ZenithTooltipColor.ACCENT))
                .add(text(literal("Template pages are normal pages, so extra sections can be added after them.")))
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
                .add(text(literal("The opening card sets the shape; these rows add detail."), ZenithTooltipColor.MUTED)));

        ZenithTooltipTemplateBuilder progress = template(id("showcase_template_progress"))
                .animationPreset(ZenithTooltipPresets.KINETIC);
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
                .add(text(literal("Segmented motion and border scans circle the 64% assembly bar.")))
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
                .add(text(literal("A compact checklist for gates, ranks, attunements, and locked abilities."), ZenithTooltipColor.MUTED)));
    }

    private void addDynamicItemTemplate() {
        template(id("showcase_dynamic_item"))
                .animationPreset(ZenithTooltipPresets.KINETIC)
                .page(page(literal("Dynamic Item Tooltip"))
                        .add(titleIcon(sourced("item_name"), literal("Uses built-in value sources")))
                        .add(text(literal("Authored text mixes with values pulled from the hovered stack.")))
                        .add(divider())
                        .add(row(literal("Item ID"), sourced("item_id"), ZenithTooltipColor.TEXT, ZenithTooltipColor.ACCENT))
                        .add(durabilityBar(literal("Live Durability"), ZenithTooltipColor.POSITIVE)));
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
        addShowcaseRule("prismarine_crystals", "showcase_background_lab", "glass_clear");
        addShowcaseRule("quartz", "showcase_frame_divider_lab", "arcane_purple");
        addShowcaseRule("blaze_powder", "showcase_gauge_motion_lab", "ember_orange");
        addShowcaseRule("clock", "showcase_page_motion_lab", "cobalt_blue");
    }

    private void addShowcaseRule(String itemPath, String documentPath, String themePath) {
        rule(id(itemPath))
                .priority(100)
                .items(minecraft(itemPath))
                .document(id(documentPath))
                .theme(id(themePath));
    }

}