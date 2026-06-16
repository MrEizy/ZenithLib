package net.zic.zenithlib.tooltip.manager;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.zic.zenithlib.ZenithLib;
import net.zic.zenithlib.tooltip.api.ZenithTooltipDocument;
import net.zic.zenithlib.tooltip.api.ZenithTooltipRule;
import net.zic.zenithlib.tooltip.api.ZenithTooltipTemplate;
import net.zic.zenithlib.tooltip.api.ZenithTooltipTheme;
import net.zic.zenithlib.tooltip.api.animation.ZenithTooltipPresets;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Central client-side resource repository and runtime matcher for data-driven Zenith
 * tooltips.
 */

public final class ZenithTooltipRepository {
    private static final Codec<Either<ZenithTooltipRule, ZenithTooltipTemplate>> DEFINITION_CODEC =
            Codec.either(ZenithTooltipRule.CODEC, ZenithTooltipTemplate.CODEC);

    private static volatile Snapshot snapshot = Snapshot.empty();
    private static Map<Identifier, ZenithTooltipRule> loadedRules = Map.of();
    private static Map<Identifier, ZenithTooltipTemplate> legacyDefinitionTemplates = Map.of();
    private static Map<Identifier, ZenithTooltipTemplate> loadedTemplates = Map.of();
    private static Map<Identifier, ZenithTooltipTheme> loadedThemes = Map.of();

    private ZenithTooltipRepository() {}

    public static ZenithTooltipDocument get(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        return get(stack, BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    public static ZenithTooltipDocument get(ItemStack stack, Identifier itemId) {
        if (stack.isEmpty()) {
            return null;
        }

        CompiledRule exactRule = snapshot.exactRules().get(itemId);

        for (CompiledRule dynamicRule : snapshot.dynamicRules()) {
            if (exactRule != null && Snapshot.RULE_ORDER.compare(exactRule, dynamicRule) < 0) {
                return exactRule.document();
            }

            if (dynamicRule.selector().matches(stack, itemId)) {
                return dynamicRule.document();
            }
        }

        return exactRule == null ? null : exactRule.document();
    }

    public static boolean hasRule(ItemStack stack) {
        return get(stack) != null;
    }

    public static Map<Identifier, ZenithTooltipTheme> themesView() {
        return snapshot.themes();
    }

    public static Map<Identifier, ZenithTooltipTemplate> templatesView() {
        return snapshot.templates();
    }

    public static Optional<ZenithTooltipDocument> fromTemplate(Identifier template, Identifier theme) {
        Snapshot current = snapshot;
        ZenithTooltipTemplate resolvedTemplate = current.templates().get(template);
        if (resolvedTemplate == null) {
            return Optional.empty();
        }

        ZenithTooltipTheme resolvedTheme = current.themes().getOrDefault(theme, ZenithTooltipTheme.defaultTheme());
        return Optional.of(resolvedTemplate.themed(resolvedTheme));
    }

    public static Optional<ZenithTooltipDocument> fromTemplate(Identifier template) {
        return fromTemplate(template, ZenithTooltipRule.DEFAULT_THEME);
    }

    /**
     * Backwards-compatible name for callers that still think of templates as documents.
     */
    public static Optional<ZenithTooltipDocument> document(Identifier template, Identifier theme) {
        return fromTemplate(template, theme);
    }

    public static Optional<ZenithTooltipDocument> document(Identifier template) {
        return fromTemplate(template);
    }

    private static synchronized LoadSummary replaceDefinitions(Map<Identifier, Either<ZenithTooltipRule, ZenithTooltipTemplate>> definitions) {
        Map<Identifier, ZenithTooltipRule> rules = new LinkedHashMap<>();
        Map<Identifier, ZenithTooltipTemplate> templates = new LinkedHashMap<>();

        for (Map.Entry<Identifier, Either<ZenithTooltipRule, ZenithTooltipTemplate>> entry : definitions.entrySet()) {
            entry.getValue().ifLeft(rule -> rules.put(entry.getKey(), rule));
            entry.getValue().ifRight(template -> templates.put(entry.getKey(), template));
        }

        loadedRules = Map.copyOf(rules);
        legacyDefinitionTemplates = Map.copyOf(templates);
        rebuildSnapshot();
        return new LoadSummary(rules.size(), templates.size(), snapshot.missingTemplates());
    }

    private static synchronized LoadSummary replaceTemplates(Map<Identifier, ZenithTooltipTemplate> templates) {
        loadedTemplates = Map.copyOf(templates);
        rebuildSnapshot();
        return new LoadSummary(loadedRules.size(), loadedTemplates.size(), snapshot.missingTemplates());
    }

    private static synchronized void replaceThemes(Map<Identifier, ZenithTooltipTheme> themes) {
        loadedThemes = Map.copyOf(themes);
        rebuildSnapshot();
    }

    private static void rebuildSnapshot() {
        snapshot = Snapshot.create(loadedRules, mergedTemplates(), loadedThemes);
    }

    private static Map<Identifier, ZenithTooltipTemplate> mergedTemplates() {
        if (legacyDefinitionTemplates.isEmpty()) {
            return loadedTemplates;
        }

        Map<Identifier, ZenithTooltipTemplate> templates = new LinkedHashMap<>(legacyDefinitionTemplates);
        templates.putAll(loadedTemplates);
        return Map.copyOf(templates);
    }

    private static <T> T getWithLocalFallback(
            Map<Identifier, T> resources,
            Identifier id,
            String fallbackNamespace
    ) {
        T value = resources.get(id);
        if (value != null || !"minecraft".equals(id.getNamespace()) || "minecraft".equals(fallbackNamespace)) {
            return value;
        }

        return resources.get(Identifier.fromNamespaceAndPath(fallbackNamespace, id.getPath()));
    }

    private record Snapshot(
            Map<Identifier, ZenithTooltipRule> rules,
            Map<Identifier, ZenithTooltipTemplate> templates,
            Map<Identifier, ZenithTooltipTheme> themes,
            Map<Identifier, CompiledRule> exactRules,
            List<CompiledRule> dynamicRules,
            int missingTemplates
    ) {
        private static final Comparator<CompiledRule> RULE_ORDER = Comparator
                .comparingInt(CompiledRule::priority)
                .reversed()
                .thenComparing(rule -> rule.id().toString());

        private static Snapshot empty() {
            return new Snapshot(Map.of(), Map.of(), Map.of(), Map.of(), List.of(), 0);
        }

        private static Snapshot create(
                Map<Identifier, ZenithTooltipRule> rules,
                Map<Identifier, ZenithTooltipTemplate> templates,
                Map<Identifier, ZenithTooltipTheme> themes
        ) {
            List<CompiledRule> compiledRules = new ArrayList<>();
            int missingTemplates = 0;

            for (Map.Entry<Identifier, ZenithTooltipRule> entry : rules.entrySet()) {
                ZenithTooltipTemplate template = getWithLocalFallback(
                        templates,
                        entry.getValue().template(),
                        entry.getKey().getNamespace()
                );

                if (template == null) {
                    missingTemplates++;
                    continue;
                }

                compiledRules.add(CompiledRule.create(entry.getKey(), entry.getValue(), template, themes));
            }

            compiledRules.sort(RULE_ORDER);

            Map<Identifier, CompiledRule> exactRules = new HashMap<>();
            List<CompiledRule> dynamicRules = new ArrayList<>();

            for (CompiledRule rule : compiledRules) {
                if (rule.selector().isExactOnly()) {
                    for (Identifier item : rule.selector().items()) {
                        exactRules.putIfAbsent(item, rule);
                    }
                } else {
                    dynamicRules.add(rule);
                }
            }

            return new Snapshot(
                    rules,
                    templates,
                    themes,
                    Map.copyOf(exactRules),
                    List.copyOf(dynamicRules),
                    missingTemplates
            );
        }

    }

    private record LoadSummary(int rules, int templates, int missingTemplates) {}

    private record CompiledRule(
            Identifier id,
            int priority,
            ZenithTooltipDocument document,
            CompiledSelector selector
    ) {
        private static CompiledRule create(
                Identifier id,
                ZenithTooltipRule rule,
                ZenithTooltipTemplate template,
                Map<Identifier, ZenithTooltipTheme> themes
        ) {
            ZenithTooltipTheme theme = getWithLocalFallback(themes, rule.theme(), id.getNamespace());
            if (theme == null) {
                theme = ZenithTooltipTheme.defaultTheme();
            }
            return new CompiledRule(id, rule.priority(), template.themed(theme), CompiledSelector.create(rule.selector(), id));
        }
    }

    private record CompiledSelector(
            boolean all,
            Set<Identifier> items,
            Set<String> namespaces,
            List<TagKey<Item>> tags
    ) {
        private static CompiledSelector create(ZenithTooltipRule.Selector selector, Identifier fallbackItemId) {
            Set<Identifier> items = new HashSet<>(selector.items());
            if (selector.inferredFromDefinitionId()) {
                items.add(fallbackItemId);
            }

            List<TagKey<Item>> tags = selector.tags().stream()
                    .map(id -> TagKey.create(BuiltInRegistries.ITEM.key(), id))
                    .toList();

            return new CompiledSelector(
                    selector.all(),
                    Set.copyOf(items),
                    Set.copyOf(new HashSet<>(selector.namespaces())),
                    tags
            );
        }

        private boolean isExactOnly() {
            return !all && !items.isEmpty() && namespaces.isEmpty() && tags.isEmpty();
        }

        private boolean matches(ItemStack stack, Identifier itemId) {
            if (all || items.contains(itemId) || namespaces.contains(itemId.getNamespace())) {
                return true;
            }

            for (TagKey<Item> tag : tags) {
                if (stack.is(tag)) {
                    return true;
                }
            }

            return false;
        }
    }

    public static final class RulesReloadListener extends SimpleJsonResourceReloadListener<Either<ZenithTooltipRule, ZenithTooltipTemplate>> {
        public RulesReloadListener() {
            super(
                    DEFINITION_CODEC,
                    FileToIdConverter.json("zenith_tooltips/definitions")
            );
        }

        @Override
        protected void apply(
                Map<Identifier, Either<ZenithTooltipRule, ZenithTooltipTemplate>> objects,
                ResourceManager resourceManager,
                ProfilerFiller profiler
        ) {
            LoadSummary summary = replaceDefinitions(objects);

            ZenithLib.LOGGER.info(
                    "Loaded {} Zenith tooltip rule definition(s) and {} legacy template definition(s)",
                    summary.rules(),
                    summary.templates()
            );

            if (summary.missingTemplates() > 0) {
                ZenithLib.LOGGER.warn(
                        "Ignored {} Zenith tooltip rule(s) referencing missing templates",
                        summary.missingTemplates()
                );
            }
        }
    }

    public static final class TemplatesReloadListener extends SimpleJsonResourceReloadListener<ZenithTooltipTemplate> {
        public TemplatesReloadListener() {
            super(
                    ZenithTooltipTemplate.CODEC,
                    FileToIdConverter.json("zenith_tooltips/templates")
            );
        }

        @Override
        protected void apply(
                Map<Identifier, ZenithTooltipTemplate> objects,
                ResourceManager resourceManager,
                ProfilerFiller profiler
        ) {
            LoadSummary summary = replaceTemplates(objects);

            ZenithLib.LOGGER.info("Loaded {} Zenith tooltip template(s)", objects.size());

            if (summary.missingTemplates() > 0) {
                ZenithLib.LOGGER.warn(
                        "Ignored {} Zenith tooltip rule(s) referencing missing templates",
                        summary.missingTemplates()
                );
            }
        }
    }

    public static final class ThemesReloadListener extends SimpleJsonResourceReloadListener<ZenithTooltipTheme> {
        public ThemesReloadListener() {
            super(
                    ZenithTooltipTheme.CODEC,
                    FileToIdConverter.json("zenith_tooltips/themes")
            );
        }

        @Override
        protected void apply(
                Map<Identifier, ZenithTooltipTheme> objects,
                ResourceManager resourceManager,
                ProfilerFiller profiler
        ) {
            replaceThemes(objects);
            ZenithLib.LOGGER.info("Loaded {} Zenith tooltip theme(s)", objects.size());
        }
    }

    public static final class AnimationPresetsReloadListener extends SimpleJsonResourceReloadListener<ZenithTooltipPresets.Data> {
        public AnimationPresetsReloadListener() {
            super(
                    ZenithTooltipPresets.Data.CODEC,
                    FileToIdConverter.json("zenith_tooltips/animation_presets")
            );
        }

        @Override
        protected void apply(
                Map<Identifier, ZenithTooltipPresets.Data> objects,
                ResourceManager resourceManager,
                ProfilerFiller profiler
        ) {
            ZenithTooltipPresets.replaceDataDriven(objects);
            ZenithLib.LOGGER.info("Loaded {} Zenith tooltip animation preset(s)", objects.size());
        }
    }
}
