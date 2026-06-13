package net.zic.zenithlib.datagen;

import net.minecraft.data.PackOutput;
import net.zic.zenithlib.ZenithLib;
import net.zic.zenithlib.tooltip.api.ZenithTooltipTheme;
import net.zic.zenithlib.tooltip.datagen.ZenithTooltipDataProvider;

/**
 * Generates tooltip themes.
 */
public final class ZenithLibTooltipThemeDataProvider extends ZenithTooltipDataProvider {
    public ZenithLibTooltipThemeDataProvider(PackOutput output) {
        super(output, ZenithLib.MOD_ID);
    }

    @Override
    public String getName() {
        return "ZenithLib tooltip showcase themes";
    }

    @Override
    protected void addTooltips() {
        addThemes();
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
