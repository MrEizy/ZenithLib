package net.zic.zenithlib.tooltip.api.element;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Locale;

/**
 * Renders the living entity represented by the hovered spawn egg inside a
 * compact tooltip display chamber.
 */
public record EntityPreviewElement(
        int width,
        int height,
        boolean rotate,
        boolean adaptiveWidth,
        Placement placement
) implements ZenithTooltipElement {
    public static final int DEFAULT_WIDTH = 58;
    public static final int DEFAULT_HEIGHT = 66;

    public EntityPreviewElement() {
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT, true, true, Placement.CENTER);
    }

    public EntityPreviewElement(int width, int height, boolean rotate) {
        this(width, height, rotate, true, Placement.CENTER);
    }

    public EntityPreviewElement {
        width = Math.max(26, Math.min(180, width));
        height = Math.max(30, Math.min(160, height));
        placement = placement == null ? Placement.CENTER : placement;
    }

    public static EntityPreviewElement automaticSpawnEggPreview() {
        return new EntityPreviewElement(DEFAULT_WIDTH, DEFAULT_HEIGHT, true, true, Placement.LEFT);
    }

    public static final MapCodec<EntityPreviewElement> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.INT.optionalFieldOf("width", DEFAULT_WIDTH).forGetter(EntityPreviewElement::width),
                    Codec.INT.optionalFieldOf("height", DEFAULT_HEIGHT).forGetter(EntityPreviewElement::height),
                    Codec.BOOL.optionalFieldOf("rotate", true).forGetter(EntityPreviewElement::rotate),
                    Codec.BOOL.optionalFieldOf("adaptive_width", true).forGetter(EntityPreviewElement::adaptiveWidth),
                    Placement.CODEC.optionalFieldOf("placement", Placement.CENTER).forGetter(EntityPreviewElement::placement)
            ).apply(instance, EntityPreviewElement::new)
    );

    @Override
    public MapCodec<? extends ZenithTooltipElement> codec() {
        return CODEC;
    }

    public enum Placement {
        LEFT("left"),
        CENTER("center"),
        RIGHT("right");

        private static final Codec<Placement> CODEC = Codec.STRING.xmap(Placement::fromName, Placement::serializedName);

        private final String serializedName;

        Placement(String serializedName) {
            this.serializedName = serializedName;
        }

        public String serializedName() {
            return serializedName;
        }

        private static Placement fromName(String name) {
            String normalised = name.toLowerCase(Locale.ROOT);
            for (Placement placement : values()) {
                if (placement.serializedName.equals(normalised)) {
                    return placement;
                }
            }
            return CENTER;
        }
    }
}
