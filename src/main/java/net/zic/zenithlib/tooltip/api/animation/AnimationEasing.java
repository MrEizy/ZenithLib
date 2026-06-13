package net.zic.zenithlib.tooltip.api.animation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.Locale;

/** Easing curves shared by tooltip property animations and procedural effects. */
public enum AnimationEasing {
    LINEAR("linear"),
    EASE_OUT("ease_out"),
    EASE_IN_OUT("ease_in_out"),
    STEPPED("stepped");

    public static final Codec<AnimationEasing> CODEC = Codec.STRING.comapFlatMap(AnimationEasing::decode, AnimationEasing::serializedName);

    private final String serializedName;

    AnimationEasing(String serializedName) {
        this.serializedName = serializedName;
    }

    public String serializedName() {
        return serializedName;
    }

    public float apply(float progress) {
        float t = Math.max(0.0F, Math.min(1.0F, progress));
        return switch (this) {
            case LINEAR -> t;
            case EASE_OUT -> 1.0F - (1.0F - t) * (1.0F - t);
            case EASE_IN_OUT -> t < 0.5F
                    ? 2.0F * t * t
                    : 1.0F - (float) Math.pow(-2.0F * t + 2.0F, 2.0F) / 2.0F;
            case STEPPED -> (float) Math.floor(t * 8.0F) / 8.0F;
        };
    }

    private static DataResult<AnimationEasing> decode(String raw) {
        String normalized = raw.toLowerCase(Locale.ROOT);
        for (AnimationEasing easing : values()) {
            if (easing.serializedName.equals(normalized)) {
                return DataResult.success(easing);
            }
        }
        return DataResult.error(() -> "Unsupported tooltip animation easing: " + raw);
    }
}
