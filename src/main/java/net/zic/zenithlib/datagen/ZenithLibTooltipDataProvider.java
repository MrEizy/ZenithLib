package net.zic.zenithlib.datagen;

import net.minecraft.data.PackOutput;
import net.zic.zenithlib.ZenithLib;
import net.zic.zenithlib.tooltip.api.ZenithTooltipColor;
import net.zic.zenithlib.tooltip.api.ZenithTooltipTheme;
import net.zic.zenithlib.tooltip.datagen.ZenithTooltipDataProvider;

import static net.zic.zenithlib.tooltip.api.builder.ZenithTooltipBuilders.*;

/*
A temporary setup that just generates the showcase tooltips I made, and the themes.
I will add actual tooltips to items later, but this is a layout guide for you guys :)
*/

public final class ZenithLibTooltipDataProvider extends ZenithTooltipDataProvider {
    public ZenithLibTooltipDataProvider(PackOutput output) {
        super(output, ZenithLib.MOD_ID);
    }

    @Override
    protected void addTooltips() {
        addShowcaseTemplate();
        addShowcaseRules();
        addThemes();
    }

    private void addShowcaseTemplate() {
        template(id("showcase"))
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
                        )))
                .page(page(translated("tooltip.zenithlib.diamond.page_2.title"))
                        .add(header(translated("tooltip.zenithlib.diamond.page_2.header"), ZenithTooltipColor.ACCENT))
                        .add(text(translated("tooltip.zenithlib.diamond.page_2.line_1")))
                        .add(text(literal("Sometimes I do not care. Sometimes I wish to tell them this world they take for truth is merely an illusion, and that they see so little of reality in their long dream."),
                                ZenithTooltipColor.TEXT, scrambleReveal(0.55F, 20)))
                        .add(spacer(4))
                        .add(text(translated("tooltip.zenithlib.diamond.page_2.line_2"), ZenithTooltipColor.ACCENT)))
                .page(page(literal("Gauge Preview"))
                        .add(header(literal("Bars"), ZenithTooltipColor.WARNING))
                        .add(text(literal("Bars support bounded values with optional custom display text. These are Placeholders")))
                        .add(divider())
                        .add(bar(literal("Integrity"), 78, 100, literal("78%"), ZenithTooltipColor.POSITIVE))
                        .add(bar(literal("Stored Qi"), 42, 100, ZenithTooltipColor.ACCENT))
                        .add(bar(literal("Impurities"), 16, 100, ZenithTooltipColor.NEGATIVE)))
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
                        .add(text(
                                literal("Damageable converted items can display live durability bars, while authored documents can use bars for any lore or resource value."),
                                ZenithTooltipColor.MUTED
                        )));
    }

    private void addShowcaseRules() {
        addShowcaseRule("diamond", "mana_blue");
        addShowcaseRule("emerald", "arcane_purple");
        addShowcaseRule("echo_shard", "ember_orange");
        addShowcaseRule("amethyst_shard", "moonlit_white");
        addShowcaseRule("nether_star", "glass_clear");
        addShowcaseRule("slime_ball", "verdant_green");
        addShowcaseRule("redstone", "crimson_red");
        addShowcaseRule("lapis_lazuli", "cobalt_blue");
    }

    private void addShowcaseRule(String itemPath, String themePath) {
        rule(id(itemPath))
                .priority(100)
                .items(minecraft(itemPath))
                .document(id("showcase"))
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
