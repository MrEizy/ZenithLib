package net.zic.zenithlib.tooltip.api;

import java.util.List;

/**
 * Full data-driven theme definition: color palette plus visual behavior settings.
 * Instances are loaded from assets/zenithlib/themes/<key>.json
 *
 * <p>Defaults (used when a JSON field is absent):
 * <ul>
 *   <li>{@code itemAnimStyle} = {@code "breathe_spin_bob"}</li>
 *   <li>{@code titleAnimStyle} = {@code "wave"}</li>
 *   <li>{@code itemBorderShape} = {@code "diamond"}</li>
 *   <li>{@code showPageIndicator} = {@code true}</li>
 *   <li>{@code showDiamondBadge} = {@code true}</li>
 *   <li>{@code customTextKeys} = empty list</li>
 * </ul>
 */
public record ThemeDefinition(
        /** The color palette. */
        TooltipTheme colors,

        /**
         * Animation style for the item icon in the diamond badge.
         * <ul>
         *   <li>{@code "breathe_spin_bob"} - scale pulse + pendulum spin + vertical bob (default)</li>
         *   <li>{@code "spin"} - slow continuous rotation only</li>
         *   <li>{@code "bob"} - gentle vertical bob only</li>
         *   <li>{@code "breathe"} - scale pulse only</li>
         *   <li>{@code "static"} - no animation</li>
         * </ul>
         */
        String itemAnimStyle,

        /**
         * Animation style for the title text.
         * <ul>
         *   <li>{@code "wave"} - travelling vertical wave (default)</li>
         *   <li>{@code "shimmer"} - brightness glint sweeps across letters</li>
         *   <li>{@code "pulse"} - whole title brightens and dims on a slow cycle</li>
         *   <li>{@code "static"} - no animation, plain text</li>
         * </ul>
         */
        String titleAnimStyle,

        /**
         * Shape of the frame drawn around the item icon.
         * <ul>
         *   <li>{@code "diamond"} - rotated diamond (default)</li>
         *   <li>{@code "square"} - beveled square</li>
         *   <li>{@code "circle"} - pixel circle</li>
         *   <li>{@code "none"} - no frame</li>
         * </ul>
         */
        String itemBorderShape,

        /** Whether to show page indicators at the bottom. */
        boolean showPageIndicator,

        /** Whether to show the diamond badge at the top-left. */
        boolean showDiamondBadge,

        /**
         * Translation key strings resolved via Component.translatable(key).getString() at
         * render time and inserted below the description section.
         */
        List<String> customTextKeys,

        /** Maximum lines per page before pagination occurs. */
        int maxLinesPerPage,

        /** Whether to enable tooltip rendering for this theme. */
        boolean enabled
) {
    /** Default definition matching the legacy golden theme behavior. */
    public static ThemeDefinition defaultDefinition() {
        return new ThemeDefinition(
                TooltipTheme.defaultTheme(),
                "breathe_spin_bob",
                "wave",
                "diamond",
                true,   // showPageIndicator
                true,   // showDiamondBadge
                List.of(),
                8,      // maxLinesPerPage
                true    // enabled
        );
    }
}
