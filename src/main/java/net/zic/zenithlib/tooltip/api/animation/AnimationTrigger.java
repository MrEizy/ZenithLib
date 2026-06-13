package net.zic.zenithlib.tooltip.api.animation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.Locale;

/** High-level events that can drive tooltip animations. */
public enum AnimationTrigger {
    ON_TOOLTIP_OPEN("on_tooltip_open"),
    ON_PAGE_ENTER("on_page_enter"),
    ON_VALUE_CHANGE("on_value_change"),
    WHILE_HOVERED("while_hovered"),
    CONTINUOUS("continuous");

    public static final Codec<AnimationTrigger> CODEC = Codec.STRING.comapFlatMap(AnimationTrigger::decode, AnimationTrigger::serializedName);

    private final String serializedName;

    AnimationTrigger(String serializedName) {
        this.serializedName = serializedName;
    }

    public String serializedName() {
        return serializedName;
    }

    private static DataResult<AnimationTrigger> decode(String raw) {
        String normalized = raw.toLowerCase(Locale.ROOT);
        for (AnimationTrigger trigger : values()) {
            if (trigger.serializedName.equals(normalized)) {
                return DataResult.success(trigger);
            }
        }
        return DataResult.error(() -> "Unsupported tooltip animation trigger: " + raw);
    }
}
