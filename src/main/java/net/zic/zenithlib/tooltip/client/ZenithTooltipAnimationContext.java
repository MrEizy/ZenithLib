package net.zic.zenithlib.tooltip.client;

import net.minecraft.resources.Identifier;
import net.zic.zenithlib.Config;
import net.zic.zenithlib.tooltip.api.animation.ZenithTooltipPresets;

import java.util.List;

/** Resolved animation state, preset composition, and accessibility gates for one tooltip frame. */
public record ZenithTooltipAnimationContext(
        Settings settings,
        ZenithTooltipPresets.Resolved presets,
        ZenithTooltipAnimationState.Frame frame
) {
    public static ZenithTooltipAnimationContext create(
            List<Identifier> presetIds,
            ZenithTooltipAnimationState.Frame frame
    ) {
        return create(presetIds, frame, Settings.capture());
    }

    public static ZenithTooltipAnimationContext create(
            List<Identifier> presetIds,
            ZenithTooltipAnimationState.Frame frame,
            Settings settings
    ) {
        Settings captured = settings == null ? Settings.disabled() : settings;
        ZenithTooltipPresets.Resolved presets = captured.animationsEnabled()
                ? ZenithTooltipPresets.resolve(presetIds)
                : ZenithTooltipPresets.resolve(List.of());
        return new ZenithTooltipAnimationContext(captured, presets, frame);
    }

    public boolean enabled() {
        return settings.animationsEnabled();
    }

    public boolean reduceMotion() {
        return settings.reduceMotion();
    }

    public boolean allowsMotion() {
        return settings.allowsMotion();
    }

    public boolean ambientEnabled() {
        return settings.allowsAmbientEffects();
    }

    public float intensity() {
        return settings.intensity();
    }

    public int particleBudget() {
        return settings.scaledParticleBudget();
    }

    public int alpha(int authoredAlpha) {
        return settings.scaledAlpha(authoredAlpha);
    }

    /** Captured client animation/accessibility options for one tooltip render pass. */
    public record Settings(
            boolean animationsEnabled,
            boolean reduceMotion,
            boolean ambientEffectsEnabled,
            float intensity,
            int particleBudget
    ) {
        public static Settings capture() {
            boolean enabled = Config.TOOLTIP_ANIMATIONS_ENABLED.get();
            float intensity = Math.max(0.0F, Math.min(1.0F, Config.TOOLTIP_ANIMATION_INTENSITY.get() / 100.0F));
            return new Settings(
                    enabled && intensity > 0.0F,
                    Config.TOOLTIP_REDUCE_MOTION.get(),
                    Config.TOOLTIP_AMBIENT_EFFECTS_ENABLED.get(),
                    intensity,
                    Math.max(0, Config.TOOLTIP_PARTICLE_BUDGET.get())
            );
        }

        public static Settings disabled() {
            return new Settings(false, false, false, 0.0F, 0);
        }

        public Settings {
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
}
