package net.zic.zenithlib.tooltip.api.builder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.zic.zenithlib.tooltip.api.ZenithTooltipColor;
import net.zic.zenithlib.tooltip.api.ZenithTooltipTheme;
import net.zic.zenithlib.tooltip.api.ZenithTooltipThemeOverride;

import java.util.Objects;

/**
 * Builder for sparse tooltip theme overrides.
 */
public final class ZenithTooltipThemeOverrideBuilder {
    private final JsonObject root = new JsonObject();

    public ZenithTooltipThemeOverrideBuilder set(
            String path,
            String value
    ) {
        put(path, new JsonPrimitive(
                Objects.requireNonNull(value, "value")
        ));

        return this;
    }

    public ZenithTooltipThemeOverrideBuilder set(
            String path,
            int value
    ) {
        put(path, new JsonPrimitive(value));
        return this;
    }

    public ZenithTooltipThemeOverrideBuilder set(
            String path,
            boolean value
    ) {
        put(path, new JsonPrimitive(value));
        return this;
    }

    public ZenithTooltipThemeOverrideBuilder background(String color) {
        validateRgba(color);
        return set("colors.background", color);
    }

    public ZenithTooltipThemeOverrideBuilder borderTop(String color) {
        validateRgba(color);
        return set("colors.border_top", color);
    }

    public ZenithTooltipThemeOverrideBuilder borderBottom(String color) {
        validateRgba(color);
        return set("colors.border_bottom", color);
    }

    public ZenithTooltipThemeOverrideBuilder text(String color) {
        validateRgba(color);
        return set("colors.text", color);
    }

    public ZenithTooltipThemeOverrideBuilder accent(String color) {
        validateRgba(color);
        return set("colors.accent", color);
    }

    public ZenithTooltipThemeOverrideBuilder muted(String color) {
        validateRgba(color);
        return set("colors.muted", color);
    }

    public ZenithTooltipThemeOverrideBuilder positive(String color) {
        validateRgba(color);
        return set("colors.positive", color);
    }

    public ZenithTooltipThemeOverrideBuilder warning(String color) {
        validateRgba(color);
        return set("colors.warning", color);
    }

    public ZenithTooltipThemeOverrideBuilder negative(String color) {
        validateRgba(color);
        return set("colors.negative", color);
    }

    public ZenithTooltipThemeOverrideBuilder padding(int padding) {
        return set("layout.padding", padding);
    }

    public ZenithTooltipThemeOverrideBuilder maxWidth(int maxWidth) {
        return set("layout.max_width", maxWidth);
    }

    public ZenithTooltipThemeOverrideBuilder maxHeight(int maxHeight) {
        return set("layout.max_height", maxHeight);
    }

    public ZenithTooltipThemeOverrideBuilder elementGap(int gap) {
        return set("layout.element_gap", gap);
    }

    public ZenithTooltipThemeOverrideBuilder rowGap(int gap) {
        return set("layout.row_gap", gap);
    }

    public ZenithTooltipThemeOverrideBuilder iconShape(
            ZenithTooltipTheme.Shape shape
    ) {
        return set(
                "icon_holder.shape",
                Objects.requireNonNull(shape, "shape").serializedName()
        );
    }

    public ZenithTooltipThemeOverrideBuilder barHeight(int height) {
        return set("bar_style.height", height);
    }

    public ZenithTooltipThemeOverrideBuilder barFillAlpha(int alpha) {
        return set("bar_style.fill_alpha", alpha);
    }

    public ZenithTooltipThemeOverrideBuilder badgeFillAlpha(int alpha) {
        return set("badge_style.fill_alpha", alpha);
    }

    public ZenithTooltipThemeOverrideBuilder dividerDecoration(
            ZenithTooltipTheme.Decoration decoration
    ) {
        return set(
                "divider_style.decoration",
                Objects.requireNonNull(
                        decoration,
                        "decoration"
                ).serializedName()
        );
    }

    public ZenithTooltipThemeOverrideBuilder frameCornerDecoration(
            ZenithTooltipTheme.CornerDecoration decoration
    ) {
        return set(
                "frame_style.corner_decoration",
                Objects.requireNonNull(
                        decoration,
                        "decoration"
                ).serializedName()
        );
    }

    public ZenithTooltipThemeOverrideBuilder headerOrnament(
            ZenithTooltipTheme.Ornament ornament
    ) {
        return set(
                "header_style.ornament",
                Objects.requireNonNull(
                        ornament,
                        "ornament"
                ).serializedName()
        );
    }

    public ZenithTooltipThemeOverrideBuilder backgroundPattern(
            ZenithTooltipTheme.Pattern pattern
    ) {
        return set(
                "background_style.pattern",
                Objects.requireNonNull(
                        pattern,
                        "pattern"
                ).serializedName()
        );
    }

    public ZenithTooltipThemeOverrideBuilder backgroundPatternColor(
            String colorReference
    ) {
        validateColorReference(colorReference);
        return set("background_style.color", colorReference);
    }

    public ZenithTooltipThemeOverrideBuilder backgroundPatternAlpha(
            int alpha
    ) {
        return set("background_style.alpha", alpha);
    }

    public ZenithTooltipThemeOverrideBuilder backgroundPatternSpacing(
            int spacing
    ) {
        return set("background_style.spacing", spacing);
    }

    public ZenithTooltipThemeOverride build() {
        ZenithTooltipThemeOverride override =
                new ZenithTooltipThemeOverride(
                        new Dynamic<>(
                                JsonOps.INSTANCE,
                                root.deepCopy()
                        )
                );

        StringBuilder error = new StringBuilder();

        if (override.applyTo(ZenithTooltipTheme.defaultTheme())
                .resultOrPartial(message -> {
                    if (!error.isEmpty()) {
                        error.append("; ");
                    }

                    error.append(message);
                })
                .isEmpty()) {

            throw new IllegalStateException(
                    "Invalid tooltip theme override: " + error
            );
        }

        return override;
    }

    private void put(String path, JsonElement value) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(value, "value");

        if (path.isBlank()) {
            throw new IllegalArgumentException(
                    "Theme override path cannot be blank"
            );
        }

        String[] parts = path.split("\\.");
        JsonObject current = root;

        for (int index = 0; index < parts.length - 1; index++) {
            String part = parts[index];

            if (part.isBlank()) {
                throw new IllegalArgumentException(
                        "Invalid theme override path: " + path
                );
            }

            JsonElement existing = current.get(part);

            if (existing == null) {
                JsonObject child = new JsonObject();
                current.add(part, child);
                current = child;
                continue;
            }

            if (!existing.isJsonObject()) {
                throw new IllegalStateException(
                        "Theme override path conflicts with an existing value: "
                                + path
                );
            }

            current = existing.getAsJsonObject();
        }

        String key = parts[parts.length - 1];

        if (key.isBlank()) {
            throw new IllegalArgumentException(
                    "Invalid theme override path: " + path
            );
        }

        current.add(key, value);
    }

    private static void validateRgba(String color) {
        ZenithTooltipTheme.parseRgbaHex(
                Objects.requireNonNull(color, "color")
        );
    }

    private static void validateColorReference(String color) {
        new ZenithTooltipColor(
                Objects.requireNonNull(color, "color")
        );
    }
}