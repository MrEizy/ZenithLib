package net.zic.zenithlib.tooltip.client;

import net.minecraft.resources.Identifier;
import net.zic.zenithlib.tooltip.api.animation.ZenithTooltipPresets;

import java.util.List;

/** Resolved animation state, preset composition, and accessibility gates for one tooltip frame. */
public record ZenithTooltipAnimationContext(
        ZenithTooltipAnimationSettings settings,
        ZenithTooltipPresets.Resolved presets,
        ZenithTooltipAnimationState.Frame frame
) {
    public static ZenithTooltipAnimationContext create(
            List<Identifier> presetIds,
            ZenithTooltipAnimationState.Frame frame
    ) {
        ZenithTooltipAnimationSettings settings = ZenithTooltipAnimationSettings.capture();
        ZenithTooltipPresets.Resolved presets = settings.animationsEnabled()
                ? ZenithTooltipPresets.resolve(presetIds)
                : ZenithTooltipPresets.resolve(List.of());
        return new ZenithTooltipAnimationContext(settings, presets, frame);
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
}
