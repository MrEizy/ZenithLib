package net.zic.zenithlib.tooltip.api.animation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Timing metadata shared by simple serialisable tooltip animations. */
public record AnimationTiming(
        int durationMillis,
        int delayMillis,
        AnimationEasing easing,
        AnimationLoopMode loopMode,
        AnimationTrigger trigger
) {
    public static final AnimationTiming DEFAULT = new AnimationTiming(
            1000,
            0,
            AnimationEasing.LINEAR,
            AnimationLoopMode.LOOP,
            AnimationTrigger.WHILE_HOVERED
    );

    public static final Codec<AnimationTiming> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.optionalFieldOf("duration", DEFAULT.durationMillis()).forGetter(AnimationTiming::durationMillis),
                    Codec.INT.optionalFieldOf("delay", DEFAULT.delayMillis()).forGetter(AnimationTiming::delayMillis),
                    AnimationEasing.CODEC.optionalFieldOf("easing", DEFAULT.easing()).forGetter(AnimationTiming::easing),
                    AnimationLoopMode.CODEC.optionalFieldOf("loop", DEFAULT.loopMode()).forGetter(AnimationTiming::loopMode),
                    AnimationTrigger.CODEC.optionalFieldOf("trigger", DEFAULT.trigger()).forGetter(AnimationTiming::trigger)
            ).apply(instance, AnimationTiming::new)
    );

    public AnimationTiming {
        durationMillis = Math.max(1, durationMillis);
        delayMillis = Math.max(0, delayMillis);
        easing = easing == null ? AnimationEasing.LINEAR : easing;
        loopMode = loopMode == null ? AnimationLoopMode.LOOP : loopMode;
        trigger = trigger == null ? AnimationTrigger.WHILE_HOVERED : trigger;
    }

    public float progress(long elapsedMillis) {
        long adjusted = Math.max(0L, elapsedMillis - delayMillis);
        float raw = switch (loopMode) {
            case ONCE -> Math.min(1.0F, adjusted / (float) durationMillis);
            case LOOP -> (adjusted % durationMillis) / (float) durationMillis;
            case PING_PONG -> {
                long period = durationMillis * 2L;
                long step = adjusted % period;
                float forward = step <= durationMillis
                        ? step / (float) durationMillis
                        : 1.0F - (step - durationMillis) / (float) durationMillis;
                yield forward;
            }
        };
        return easing.apply(raw);
    }
}
