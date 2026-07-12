package net.zic.zenithlib.tooltip.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;

import java.util.Map;
import java.util.Objects;

/**
 * A sparse, recursively merged patch applied over an existing tooltip theme.
 */
public record ZenithTooltipThemeOverride(Dynamic<?> patch) {
    public static final Codec<ZenithTooltipThemeOverride> CODEC =
            Codec.PASSTHROUGH.comapFlatMap(
                    dynamic -> {
                        ZenithTooltipThemeOverride override = new ZenithTooltipThemeOverride(dynamic);
                        return override.applyTo(ZenithTooltipTheme.defaultTheme()).map(theme -> override);
                    },
                    ZenithTooltipThemeOverride::patch
            );

    public ZenithTooltipThemeOverride {
        Objects.requireNonNull(patch, "patch");
    }


    public DataResult<ZenithTooltipTheme> applyTo(
            ZenithTooltipTheme base
    ) {
        Objects.requireNonNull(base, "base");

        return ZenithTooltipTheme.CODEC
                .encodeStart(JsonOps.INSTANCE, base)
                .flatMap(encodedBase -> {
                    JsonElement encodedPatch =
                            patch.convert(JsonOps.INSTANCE).getValue();

                    if (!encodedPatch.isJsonObject()) {
                        return DataResult.error(
                                () -> "Tooltip theme overrides must be a JSON object"
                        );
                    }

                    if (!encodedBase.isJsonObject()) {
                        return DataResult.error(
                                () -> "Encoded tooltip theme was not a JSON object"
                        );
                    }

                    JsonElement merged = merge(
                            encodedBase,
                            encodedPatch
                    );

                    return ZenithTooltipTheme.CODEC.parse(
                            JsonOps.INSTANCE,
                            merged
                    );
                });
    }


    private static JsonElement merge(
            JsonElement base,
            JsonElement patch
    ) {
        if (!base.isJsonObject() || !patch.isJsonObject()) {
            return patch.deepCopy();
        }

        JsonObject merged = base.getAsJsonObject().deepCopy();

        for (Map.Entry<String, JsonElement> entry
                : patch.getAsJsonObject().entrySet()) {

            String key = entry.getKey();
            JsonElement patchValue = entry.getValue();
            JsonElement baseValue = merged.get(key);

            if (baseValue != null
                    && baseValue.isJsonObject()
                    && patchValue.isJsonObject()) {

                merged.add(
                        key,
                        merge(baseValue, patchValue)
                );
            } else {
                merged.add(
                        key,
                        patchValue.deepCopy()
                );
            }
        }

        return merged;
    }
}