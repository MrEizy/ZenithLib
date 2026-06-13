package net.zic.zenithlib.tooltip.api.animation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.Locale;

/** Describes how a tooltip animation consumes time after it reaches the end of its duration. */
public enum AnimationLoopMode {
    ONCE("once"),
    LOOP("loop"),
    PING_PONG("ping_pong");

    public static final Codec<AnimationLoopMode> CODEC = Codec.STRING.comapFlatMap(AnimationLoopMode::decode, AnimationLoopMode::serializedName);

    private final String serializedName;

    AnimationLoopMode(String serializedName) {
        this.serializedName = serializedName;
    }

    public String serializedName() {
        return serializedName;
    }

    private static DataResult<AnimationLoopMode> decode(String raw) {
        String normalized = raw.toLowerCase(Locale.ROOT);
        for (AnimationLoopMode mode : values()) {
            if (mode.serializedName.equals(normalized)) {
                return DataResult.success(mode);
            }
        }
        return DataResult.error(() -> "Unsupported tooltip animation loop mode: " + raw);
    }
}
