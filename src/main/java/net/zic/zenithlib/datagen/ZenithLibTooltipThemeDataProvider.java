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
                .colors("#061826DD", "#4DE6FFFF", "#114E72FF", "#E8FBFFFF", "#64DFFFFF", "#7DAEBEFF", "#86F7B7FF", "#FFD47AFF", "#FF6B7EFF")
                .layout(6, 240, 220, 3, 1)
                .iconHolder(ZenithTooltipTheme.Shape.CIRCLE, 29, 2, 8, "accent", "background", 78)
                .barStyle(5, 2, "border_bottom", 64, "accent", 1, 238)
                .badgeStyle(5, 1, 1, 205)
                .dividerStyle(1, 2, 3, "accent", ZenithTooltipTheme.Decoration.DOTTED)
                .frameStyle(ZenithTooltipTheme.CornerDecoration.BRACKET, 5, 2, "accent", true, 2, "border_top", 76)
                .headerStyle(ZenithTooltipTheme.Ornament.SIDE_LINES, "accent")
                .backgroundStyle(ZenithTooltipTheme.Pattern.GRID, "accent", 8, 10);
    }

    private void addArcanePurpleTheme() {
        theme(id("arcane_purple"))
                .colors("#16061FDD", "#C46CFFFF", "#3B145FFF", "#F1E2FFFF", "#DE9DFFFF", "#B796CFFF", "#95FFC8FF", "#F0C66AFF", "#FF6EAAFF")
                .layout(6, 240, 220, 3, 1)
                .iconHolder(ZenithTooltipTheme.Shape.GEM, 31, 2, 8, "accent", "background", 86)
                .barStyle(4, 2, "muted", 46, "border_bottom", 0, 218)
                .badgeStyle(6, 1, 1, 188)
                .dividerStyle(1, 2, 3, "accent", ZenithTooltipTheme.Decoration.CENTER_RUNE)
                .frameStyle(ZenithTooltipTheme.CornerDecoration.RUNE, 5, 2, "accent", true, 2, "accent", 70)
                .headerStyle(ZenithTooltipTheme.Ornament.SMALL_DIAMONDS, "accent")
                .backgroundStyle(ZenithTooltipTheme.Pattern.RUNES, "accent", 12, 13);
    }

    private void addEmberOrangeTheme() {
        theme(id("ember_orange"))
                .colors("#211006DD", "#FFB35CFF", "#7D2D0EFF", "#FFF0D6FF", "#FFB35CFF", "#C18A63FF", "#B9F27CFF", "#FFD166FF", "#FF6A48FF")
                .layout(6, 240, 220, 3, 1)
                .iconHolder(ZenithTooltipTheme.Shape.BRACKET, 29, 2, 8, "warning", "background", 86)
                .barStyle(6, 2, "border_bottom", 96, "warning", 1, 238)
                .badgeStyle(5, 2, 2, 210)
                .dividerStyle(2, 2, 3, "warning", ZenithTooltipTheme.Decoration.DOTTED)
                .frameStyle(ZenithTooltipTheme.CornerDecoration.BRACKET, 5, 2, "warning", false, 2, "border_top", 92)
                .headerStyle(ZenithTooltipTheme.Ornament.CORNER_TICKS, "warning")
                .backgroundStyle(ZenithTooltipTheme.Pattern.DIAGONAL_LINES, "warning", 8, 11);
    }

    private void addMoonlitWhiteTheme() {
        theme(id("moonlit_white"))
                .colors("#F7F2E4DD", "#FFFFFFFF", "#A9A095FF", "#2B2730FF", "#3A75A0FF", "#706A76FF", "#247B4AFF", "#936715FF", "#A13F48FF")
                .layout(6, 240, 220, 3, 1)
                .iconHolder(ZenithTooltipTheme.Shape.OCTAGON, 27, 1, 8, "border_bottom", "background", 48)
                .barStyle(3, 2, "muted", 42, "border_bottom", 0, 198)
                .badgeStyle(6, 1, 1, 132)
                .dividerStyle(1, 2, 3, "border_bottom", ZenithTooltipTheme.Decoration.DIAMOND)
                .frameStyle(ZenithTooltipTheme.CornerDecoration.NOTCHED, 7, 2, "border_bottom", true, 2, "border_bottom", 70)
                .headerStyle(ZenithTooltipTheme.Ornament.SIDE_LINES, "border_bottom")
                .backgroundStyle(ZenithTooltipTheme.Pattern.NONE, "muted", 0, 10);
    }

    private void addGlassClearTheme() {
        theme(id("glass_clear"))
                .colors("#09111EA0", "#E3FAFFFF", "#77A6BCCC", "#F4FCFFFF", "#DFF8FFFF", "#A8C4D6FF", "#A8FFD0FF", "#FFE49AFF", "#FF9AA7FF")
                .layout(6, 240, 220, 3, 1)
                .iconHolder(ZenithTooltipTheme.Shape.NONE, 18, 0, 7, "accent", "background", 0)
                .barStyle(3, 2, "muted", 34, "border_bottom", 0, 172)
                .badgeStyle(5, 1, 1, 98)
                .dividerStyle(1, 2, 3, "muted", ZenithTooltipTheme.Decoration.NONE)
                .frameStyle(ZenithTooltipTheme.CornerDecoration.NONE, 6, 2, "accent", true, 2, "border_top", 54)
                .headerStyle(ZenithTooltipTheme.Ornament.SIDE_LINES, "muted")
                .backgroundStyle(ZenithTooltipTheme.Pattern.STARS, "accent", 6, 13);
    }

    private void addVerdantGreenTheme() {
        theme(id("verdant_green"))
                .colors("#07190EDD", "#73E892FF", "#174D2FFF", "#E9FFF0FF", "#9AFFA5FF", "#88B894FF", "#C4FF72FF", "#FFE083FF", "#FF7474FF")
                .layout(6, 240, 220, 3, 1)
                .iconHolder(ZenithTooltipTheme.Shape.CIRCLE, 29, 2, 8, "positive", "background", 76)
                .barStyle(5, 2, "border_bottom", 52, "positive", 1, 225)
                .badgeStyle(5, 2, 1, 190)
                .dividerStyle(1, 2, 3, "positive", ZenithTooltipTheme.Decoration.DOTTED)
                .frameStyle(ZenithTooltipTheme.CornerDecoration.NOTCHED, 7, 2, "positive", true, 2, "border_top", 74)
                .headerStyle(ZenithTooltipTheme.Ornament.CORNER_TICKS, "positive")
                .backgroundStyle(ZenithTooltipTheme.Pattern.DIAGONAL_LINES, "positive", 7, 12);
    }

    private void addCrimsonRedTheme() {
        theme(id("crimson_red"))
                .colors("#24070BDD", "#FF5F74FF", "#681522FF", "#FFE9EDFF", "#FF5F74FF", "#C58A93FF", "#86EFA0FF", "#FFC85CFF", "#FF3856FF")
                .layout(6, 240, 220, 3, 1)
                .iconHolder(ZenithTooltipTheme.Shape.DIAMOND, 31, 2, 8, "negative", "background", 82)
                .barStyle(6, 2, "border_bottom", 62, "negative", 1, 230)
                .badgeStyle(6, 2, 2, 218)
                .dividerStyle(1, 2, 3, "negative", ZenithTooltipTheme.Decoration.DOUBLE_DIAMOND)
                .frameStyle(ZenithTooltipTheme.CornerDecoration.DIAMOND, 5, 2, "negative", true, 2, "border_bottom", 76)
                .headerStyle(ZenithTooltipTheme.Ornament.SMALL_DIAMONDS, "negative")
                .backgroundStyle(ZenithTooltipTheme.Pattern.DIAGONAL_LINES, "negative", 7, 9);
    }

    private void addCobaltBlueTheme() {
        theme(id("cobalt_blue"))
                .colors("#08152CDD", "#6E98FFFF", "#1D347CFF", "#EDF3FFFF", "#88AFFFFF", "#8D9FCFFF", "#8EF0A6FF", "#FFD875FF", "#FF7782FF")
                .layout(6, 240, 220, 3, 1)
                .iconHolder(ZenithTooltipTheme.Shape.SQUARE, 28, 1, 8, "accent", "background", 70)
                .barStyle(4, 1, "border_bottom", 58, "accent", 1, 220)
                .badgeStyle(5, 1, 1, 190)
                .dividerStyle(1, 2, 3, "accent", ZenithTooltipTheme.Decoration.NONE)
                .frameStyle(ZenithTooltipTheme.CornerDecoration.BRACKET, 5, 1, "accent", true, 1, "border_top", 68)
                .headerStyle(ZenithTooltipTheme.Ornament.SIDE_LINES, "accent")
                .backgroundStyle(ZenithTooltipTheme.Pattern.GRID, "accent", 7, 9);
    }

}

