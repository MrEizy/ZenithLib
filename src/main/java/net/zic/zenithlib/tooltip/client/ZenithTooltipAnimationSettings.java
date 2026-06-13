package net.zic.zenithlib.tooltip.client;

import net.zic.zenithlib.Config;

/** Captured client animation/accessibility options for one tooltip render pass. */
public record ZenithTooltipAnimationSettings(
        boolean animationsEnabled,
        boolean reduceMotion,
        boolean ambientEffectsEnabled,
        float intensity,
        int particleBudget
) {
    public static ZenithTooltipAnimationSettings capture() {
        boolean enabled = Config.TOOLTIP_ANIMATIONS_ENABLED.get();
        float intensity = Math.max(0.0F, Math.min(1.0F, Config.TOOLTIP_ANIMATION_INTENSITY.get() / 100.0F));
        return new ZenithTooltipAnimationSettings(
                enabled && intensity > 0.0F,
                Config.TOOLTIP_REDUCE_MOTION.get(),
                Config.TOOLTIP_AMBIENT_EFFECTS_ENABLED.get(),
                intensity,
                Math.max(0, Config.TOOLTIP_PARTICLE_BUDGET.get())
        );
    }

    public static ZenithTooltipAnimationSettings disabled() {
        return new ZenithTooltipAnimationSettings(false, false, false, 0.0F, 0);
    }

    public ZenithTooltipAnimationSettings {
        intensity = Math.max(0.0F, Math.min(1.0F, intensity));
        particleBudget = Math.max(0, particleBudget);
        animationsEnabled = animationsEnabled && intensity > 0.0F;
        ambientEffectsEnabled = ambientEffectsEnabled && animationsEnabled && particleBudget > 0;
    }

    public boolean allowsMotion() {
        return animationsEnabled && !reduceMotion;
    }

    public boolean allowsAmbientEffects() {
        return ambientEffectsEnabled;
    }

    public int scaledParticleBudget() {
        return animationsEnabled ? Math.round(particleBudget * intensity) : 0;
    }

    public int scaledAlpha(int alpha) {
        return Math.max(0, Math.min(255, Math.round(alpha * intensity)));
    }
}
