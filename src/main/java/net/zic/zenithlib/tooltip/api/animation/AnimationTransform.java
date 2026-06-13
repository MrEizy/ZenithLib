package net.zic.zenithlib.tooltip.api.animation;

/** Combined property-animation result applied to a tooltip element render pass. */
public record AnimationTransform(
        float offsetX,
        float offsetY,
        float scaleX,
        float scaleY,
        float rotationDegrees,
        float opacity,
        float clipProgress
) {
    public static final AnimationTransform IDENTITY = new AnimationTransform(0, 0, 1, 1, 0, 1, 1);

    public AnimationTransform {
        scaleX = scaleX == 0.0F ? 1.0F : scaleX;
        scaleY = scaleY == 0.0F ? 1.0F : scaleY;
        opacity = Math.max(0.0F, Math.min(1.0F, opacity));
        clipProgress = Math.max(0.0F, Math.min(1.0F, clipProgress));
    }

    public AnimationTransform then(AnimationTransform other) {
        if (other == null) {
            return this;
        }
        return new AnimationTransform(
                this.offsetX + other.offsetX,
                this.offsetY + other.offsetY,
                this.scaleX * other.scaleX,
                this.scaleY * other.scaleY,
                this.rotationDegrees + other.rotationDegrees,
                this.opacity * other.opacity,
                Math.min(this.clipProgress, other.clipProgress)
        );
    }
}
