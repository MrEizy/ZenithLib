package net.zic.zenithlib;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    private static final ModConfigSpec.Builder CLIENT_BUILDER = new ModConfigSpec.Builder();


    public static final ModConfigSpec.BooleanValue LOG_DIRT_BLOCK = BUILDER
            .comment("Whether to log the dirt block on common setup")
            .define("logDirtBlock", true);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static final ModConfigSpec.BooleanValue ENABLE_ZENITH_TOOLTIPS = CLIENT_BUILDER
            .comment("Whether ZenithLib replaces vanilla item tooltips with themed Zenith tooltips.")
            .translation("config.zenithlib.tooltips.enabled")
            .define("tooltips.enabled", true);

    public static final ModConfigSpec.BooleanValue SHOW_SPAWN_EGG_ENTITY_PREVIEWS = CLIENT_BUILDER
            .comment("Whether Zenith tooltips render entity previews for spawn eggs.")
            .translation("config.zenithlib.tooltips.spawn_egg_entity_previews")
            .define("tooltips.spawnEggEntityPreviews", true);


    public static final ModConfigSpec.BooleanValue TOOLTIP_ANIMATIONS_ENABLED = CLIENT_BUILDER
            .comment("Whether Zenith tooltip animations and procedural effects are enabled.")
            .translation("config.zenithlib.tooltips.animations_enabled")
            .define("tooltips.animations.enabled", true);

    public static final ModConfigSpec.BooleanValue TOOLTIP_REDUCE_MOTION = CLIENT_BUILDER
            .comment("Reduces large or continuous motion while preserving readable fades and static decoration.")
            .translation("config.zenithlib.tooltips.reduce_motion")
            .define("tooltips.animations.reduceMotion", false);

    public static final ModConfigSpec.BooleanValue TOOLTIP_AMBIENT_EFFECTS_ENABLED = CLIENT_BUILDER
            .comment("Whether ambient procedural background effects such as stars and mist are drawn.")
            .translation("config.zenithlib.tooltips.ambient_effects_enabled")
            .define("tooltips.animations.ambientEffects", true);

    public static final ModConfigSpec.IntValue TOOLTIP_ANIMATION_INTENSITY = CLIENT_BUILDER
            .comment("Tooltip animation intensity percentage. 0 disables most optional effects, 100 is the authored default.")
            .translation("config.zenithlib.tooltips.animation_intensity")
            .defineInRange("tooltips.animations.intensity", 100, 0, 100);

    public static final ModConfigSpec.IntValue TOOLTIP_PARTICLE_BUDGET = CLIENT_BUILDER
            .comment("Maximum tiny procedural particles or motes drawn per tooltip frame.")
            .translation("config.zenithlib.tooltips.particle_budget")
            .defineInRange("tooltips.animations.particleBudget", 32, 0, 128);

    public static final ModConfigSpec CLIENT_SPEC = CLIENT_BUILDER.build();

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(Identifier.parse(itemName));
    }
}
