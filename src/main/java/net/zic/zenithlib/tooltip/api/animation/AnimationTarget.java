package net.zic.zenithlib.tooltip.api.animation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.Locale;

/** Coarse render targets used by reusable tooltip animation presets. */
public enum AnimationTarget {
    TOOLTIP("tooltip"),
    PAGE("page"),
    TITLE("title"),
    TEXT("text"),
    FRAME("frame"),
    BACKGROUND("background"),
    ICON("icon"),
    BAR("bar"),
    DIVIDER("divider");

    public static final Codec<AnimationTarget> CODEC = Codec.STRING.comapFlatMap(AnimationTarget::decode, AnimationTarget::serializedName);

    private final String serializedName;

    AnimationTarget(String serializedName) {
        this.serializedName = serializedName;
    }

    public String serializedName() {
        return serializedName;
    }

    private static DataResult<AnimationTarget> decode(String raw) {
        String normalized = raw.toLowerCase(Locale.ROOT);
        for (AnimationTarget target : values()) {
            if (target.serializedName.equals(normalized)) {
                return DataResult.success(target);
            }
        }
        return DataResult.error(() -> "Unsupported tooltip animation target: " + raw);
    }
}
