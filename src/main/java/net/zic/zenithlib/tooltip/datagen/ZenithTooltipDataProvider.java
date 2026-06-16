package net.zic.zenithlib.tooltip.datagen;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.zic.zenithlib.tooltip.api.ZenithTooltipRule;
import net.zic.zenithlib.tooltip.api.ZenithTooltipTemplate;
import net.zic.zenithlib.tooltip.api.ZenithTooltipTheme;
import net.zic.zenithlib.tooltip.api.ZenithTooltipText;
import net.zic.zenithlib.tooltip.api.builder.ZenithTooltipPageBuilder;
import net.zic.zenithlib.tooltip.api.builder.ZenithTooltipRuleBuilder;
import net.zic.zenithlib.tooltip.api.builder.ZenithTooltipTemplateBuilder;
import net.zic.zenithlib.tooltip.api.builder.ZenithTooltipThemeBuilder;
import net.zic.zenithlib.tooltip.api.animation.ZenithTooltipPresets;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Base data provider for generated Zenith tooltip rules, templates, and themes.
 */
public abstract class ZenithTooltipDataProvider implements DataProvider {
    private final String modId;
    private final PackOutput.PathProvider definitionPaths;
    private final PackOutput.PathProvider templatePaths;
    private final PackOutput.PathProvider themePaths;
    private final PackOutput.PathProvider animationPresetPaths;

    private final Map<Identifier, ZenithTooltipTemplateBuilder> templates = new LinkedHashMap<>();
    private final Map<Identifier, ZenithTooltipRuleBuilder> rules = new LinkedHashMap<>();
    private final Map<Identifier, ZenithTooltipThemeBuilder> themes = new LinkedHashMap<>();
    private final Map<Identifier, ZenithTooltipPresets.Data> animationPresets = new LinkedHashMap<>();

    protected ZenithTooltipDataProvider(PackOutput output, String modId) {
        this.modId = Objects.requireNonNull(modId, "modId");
        this.definitionPaths = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "zenith_tooltips/definitions");
        this.templatePaths = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "zenith_tooltips/templates");
        this.themePaths = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "zenith_tooltips/themes");
        this.animationPresetPaths = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "zenith_tooltips/animation_presets");
    }

    protected abstract void addTooltips();

    protected final Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(this.modId, path);
    }

    protected final ZenithTooltipText t(String key) {
        return ZenithTooltipText.translatable(key);
    }

    protected final ZenithTooltipText lit(String text) {
        return ZenithTooltipText.literal(text);
    }

    protected final ZenithTooltipText src(String source) {
        return ZenithTooltipText.source(source);
    }

    protected final GeneratedTooltipBuilder tooltip(String path) {
        Identifier templateId = id(path);
        Identifier ruleId = id(path + "_rule");

        return new GeneratedTooltipBuilder(
                templateId,
                template(templateId),
                rule(ruleId).template(templateId)
        );
    }

    protected final ZenithTooltipTemplateBuilder template(String path) {
        return template(id(path));
    }

    protected final ZenithTooltipRuleBuilder rule(String path) {
        return rule(id(path));
    }

    protected final ZenithTooltipThemeBuilder theme(String path) {
        return theme(id(path));
    }

    protected final void animationPreset(String path, String... effects) {
        animationPreset(id(path), List.of(effects));
    }

    protected final void animationPreset(Identifier id, List<String> effects) {
        animationPreset(id, List.of(), effects);
    }

    protected final void animationPreset(Identifier id, List<Identifier> parents, List<String> effects) {
        requireOwnedOutputId(id);
        ensureAnimationPresetPathAvailable(id);
        this.animationPresets.put(id, new ZenithTooltipPresets.Data(parents, effects));
    }

    protected final ZenithTooltipRuleBuilder itemTooltip(String path, Identifier template) {
        return rule(path).template(template);
    }

    protected final ZenithTooltipRuleBuilder itemTooltip(String path, String templatePath) {
        return itemTooltip(path, id(templatePath));
    }

    protected final ZenithTooltipRuleBuilder itemTooltip(String path, Identifier item, Identifier template) {
        return rule(path).items(item).template(template);
    }

    protected final ZenithTooltipRuleBuilder itemTooltip(String path, Identifier item, String templatePath) {
        return itemTooltip(path, item, id(templatePath));
    }

    protected final ZenithTooltipRuleBuilder tagTooltip(String path, Identifier tag, Identifier template) {
        return rule(path).tags(tag).template(template);
    }

    protected final ZenithTooltipRuleBuilder tagTooltip(String path, Identifier tag, String templatePath) {
        return tagTooltip(path, tag, id(templatePath));
    }

    protected final ZenithTooltipRuleBuilder namespaceTooltip(String path, String namespace, Identifier template) {
        return rule(path).namespaces(namespace).template(template);
    }

    protected final ZenithTooltipRuleBuilder namespaceTooltip(String path, String namespace, String templatePath) {
        return namespaceTooltip(path, namespace, id(templatePath));
    }

    protected final void template(String path, Consumer<ZenithTooltipTemplateBuilder> action) {
        Objects.requireNonNull(action, "action").accept(template(path));
    }

    protected final void rule(String path, Consumer<ZenithTooltipRuleBuilder> action) {
        Objects.requireNonNull(action, "action").accept(rule(path));
    }

    protected final void theme(String path, Consumer<ZenithTooltipThemeBuilder> action) {
        Objects.requireNonNull(action, "action").accept(theme(path));
    }

    protected final void addTemplatePages(ZenithTooltipTemplateBuilder target, ZenithTooltipTemplateBuilder source) {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(source, "source").build().pages().forEach(target::page);
    }

    protected final Identifier external(String namespace, String path) {
        return Identifier.fromNamespaceAndPath(namespace, path);
    }

    protected final Identifier minecraft(String path) {
        return external("minecraft", path);
    }

    protected final ZenithTooltipTemplateBuilder template(Identifier id) {
        requireOwnedOutputId(id);
        ensureTemplatePathAvailable(id);

        ZenithTooltipTemplateBuilder builder = new ZenithTooltipTemplateBuilder();
        this.templates.put(id, builder);
        return builder;
    }

    protected final ZenithTooltipRuleBuilder rule(Identifier id) {
        requireOwnedOutputId(id);
        ensureRulePathAvailable(id);

        ZenithTooltipRuleBuilder builder = new ZenithTooltipRuleBuilder();
        this.rules.put(id, builder);
        return builder;
    }

    protected final ZenithTooltipThemeBuilder theme(Identifier id) {
        requireOwnedOutputId(id);
        ensureThemePathAvailable(id);

        ZenithTooltipThemeBuilder builder = new ZenithTooltipThemeBuilder();
        this.themes.put(id, builder);
        return builder;
    }

    @Override
    public final CompletableFuture<?> run(CachedOutput output) {
        this.templates.clear();
        this.rules.clear();
        this.themes.clear();
        this.animationPresets.clear();

        addTooltips();

        Map<Identifier, ZenithTooltipRule> encodedRules = new LinkedHashMap<>();
        this.rules.forEach((id, builder) -> encodedRules.put(id, builder.build()));

        Map<Identifier, ZenithTooltipTemplate> encodedTemplates = new LinkedHashMap<>();
        this.templates.forEach((id, builder) -> encodedTemplates.put(id, builder.build()));

        Map<Identifier, ZenithTooltipTheme> encodedThemes = new LinkedHashMap<>();
        this.themes.forEach((id, builder) -> encodedThemes.put(id, builder.build()));

        Map<Identifier, ZenithTooltipPresets.Data> encodedAnimationPresets = new LinkedHashMap<>(this.animationPresets);

        CompletableFuture<?> rulesFuture = DataProvider.saveAll(
                output,
                ZenithTooltipRule.CODEC,
                this.definitionPaths,
                encodedRules
        );

        CompletableFuture<?> templatesFuture = DataProvider.saveAll(
                output,
                ZenithTooltipTemplate.CODEC,
                this.templatePaths,
                encodedTemplates
        );

        CompletableFuture<?> themesFuture = DataProvider.saveAll(
                output,
                ZenithTooltipTheme.CODEC,
                this.themePaths,
                encodedThemes
        );

        CompletableFuture<?> animationPresetsFuture = DataProvider.saveAll(
                output,
                ZenithTooltipPresets.Data.CODEC,
                this.animationPresetPaths,
                encodedAnimationPresets
        );

        return CompletableFuture.allOf(rulesFuture, templatesFuture, themesFuture, animationPresetsFuture);
    }

    @Override
    public String getName() {
        return "Zenith tooltip resources: " + this.modId;
    }

    protected final class GeneratedTooltipBuilder {
        private final Identifier templateId;
        private final ZenithTooltipTemplateBuilder template;
        private final ZenithTooltipRuleBuilder rule;

        private GeneratedTooltipBuilder(
                Identifier templateId,
                ZenithTooltipTemplateBuilder template,
                ZenithTooltipRuleBuilder rule
        ) {
            this.templateId = templateId;
            this.template = template;
            this.rule = rule;
        }

        public Identifier templateId() {
            return templateId;
        }


        public ZenithTooltipTemplateBuilder template() {
            return template;
        }

        public ZenithTooltipRuleBuilder rule() {
            return rule;
        }

        public GeneratedTooltipBuilder priority(int priority) {
            rule.priority(priority);
            return this;
        }

        public GeneratedTooltipBuilder theme(Identifier theme) {
            rule.theme(theme);
            return this;
        }

        public GeneratedTooltipBuilder item(Identifier item) {
            rule.items(item);
            return this;
        }

        public GeneratedTooltipBuilder item(String namespace, String path) {
            return item(external(namespace, path));
        }

        public GeneratedTooltipBuilder minecraftItem(String path) {
            return item(minecraft(path));
        }

        public GeneratedTooltipBuilder tag(Identifier tag) {
            rule.tags(tag);
            return this;
        }

        public GeneratedTooltipBuilder namespace(String namespace) {
            rule.namespaces(namespace);
            return this;
        }

        public GeneratedTooltipBuilder all() {
            rule.all();
            return this;
        }

        public GeneratedTooltipBuilder animationPreset(Identifier preset) {
            template.animationPreset(preset);
            return this;
        }

        public GeneratedTooltipBuilder animations(Identifier... presets) {
            template.animationPresets(presets);
            return this;
        }

        public GeneratedTooltipBuilder template(ZenithTooltipTemplateBuilder source) {
            template.pages(source);
            return this;
        }

        public GeneratedTooltipBuilder page(ZenithTooltipPageBuilder page) {
            template.page(page);
            return this;
        }

        public GeneratedTooltipBuilder page(ZenithTooltipText title, Consumer<ZenithTooltipPageBuilder> action) {
            template.page(title, action);
            return this;
        }
    }

    private void ensureTemplatePathAvailable(Identifier id) {
        if (this.templates.containsKey(id)) {
            throw new IllegalStateException("Duplicate generated Zenith tooltip template id: " + id);
        }
    }

    private void ensureRulePathAvailable(Identifier id) {
        if (this.rules.containsKey(id)) {
            throw new IllegalStateException("Duplicate generated Zenith tooltip rule id: " + id);
        }
    }

    private void ensureThemePathAvailable(Identifier id) {
        if (this.themes.containsKey(id)) {
            throw new IllegalStateException("Duplicate generated Zenith tooltip theme id: " + id);
        }
    }

    private void ensureAnimationPresetPathAvailable(Identifier id) {
        if (this.animationPresets.containsKey(id)) {
            throw new IllegalStateException("Duplicate generated Zenith tooltip animation preset id: " + id);
        }
    }

    private void requireOwnedOutputId(Identifier id) {
        Objects.requireNonNull(id, "id");

        if (!this.modId.equals(id.getNamespace())) {
            throw new IllegalArgumentException(
                    "Generated Zenith tooltip resources must be in provider namespace '" + this.modId + "': " + id
            );
        }
    }
}