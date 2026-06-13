package net.zic.zenithlib.tooltip.api.animation;

/**
 * Immutable render-time information supplied to tooltip animations and effects.
 */
public record AnimationContext(
        long tooltipAgeMillis,
        long pageAgeMillis,
        float partialTick,
        int mouseX,
        int mouseY,
        Bounds elementBounds,
        Bounds tooltipBounds,
        long seed,
        int pageIndex,
        int value,
        int maxValue,
        boolean reducedMotion,
        float intensity
) {
    public AnimationContext {
        if (elementBounds == null) {
            elementBounds = Bounds.ZERO;
        }
        if (tooltipBounds == null) {
            tooltipBounds = Bounds.ZERO;
        }
        intensity = Math.max(0.0F, Math.min(1.0F, intensity));
    }

    public float valueProgress() {
        return maxValue <= 0 ? 0.0F : Math.max(0.0F, Math.min(1.0F, value / (float) maxValue));
    }

    public record Bounds(int x, int y, int width, int height) {
        public static final Bounds ZERO = new Bounds(0, 0, 0, 0);

        public int right() {
            return x + width;
        }

        public int bottom() {
            return y + height;
        }
    }
}
