package net.zic.zenithlib.tooltip.datagen;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
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

import java.util.function.Consumer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Base data provider for generated Zenith tooltip definitions and themes.
 *
 * <p>Generated resources are written into the same client resource folders used by
 * {@code ZenithTooltipRepository}; the repository and its reload behaviour remain
 * the runtime contract. Builders only construct values that are encoded by the
 * existing runtime codecs.</p>
 */
public abstract class ZenithTooltipDataProvider implements DataProvider {
    private static final Codec<Either<ZenithTooltipRule, ZenithTooltipTemplate>> DEFINITION_CODEC =
            Codec.either(ZenithTooltipRule.CODEC, ZenithTooltipTemplate.CODEC);

    private final String modId;
    private final PackOutput.PathProvider definitionPaths;
    private final PackOutput.PathProvider themePaths;
    private final Map<Identifier, ZenithTooltipTemplateBuilder> templates = new LinkedHashMap<>();
    private final Map<Identifier, ZenithTooltipRuleBuilder> rules = new LinkedHashMap<>();
    private final Map<Identifier, ZenithTooltipThemeBuilder> themes = new LinkedHashMap<>();

    protected ZenithTooltipDataProvider(PackOutput output, String modId) {
        this.modId = Objects.requireNonNull(modId, "modId");
        this.definitionPaths = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "zenith_tooltips/definitions");
        this.themePaths = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "zenith_tooltips/themes");
    }

    /** Adds generated templates, selector rules, and themes for this provider. */
    protected abstract void addTooltips();

    /** Creates an identifier in the namespace owned by this data provider. */
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
        return new GeneratedTooltipBuilder(templateId, template(templateId), rule(ruleId).document(templateId));
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

    /** Creates an identifier used as a cross-namespace reference, such as a vanilla item or ZenithLib theme. */
    protected final Identifier external(String namespace, String path) {
        return Identifier.fromNamespaceAndPath(namespace, path);
    }

    protected final Identifier minecraft(String path) {
        return external("minecraft", path);
    }

    protected final ZenithTooltipTemplateBuilder template(Identifier id) {
        requireOwnedOutputId(id);
        ensureDefinitionPathAvailable(id);
        ZenithTooltipTemplateBuilder builder = new ZenithTooltipTemplateBuilder();
        this.templates.put(id, builder);
        return builder;
    }

    protected final ZenithTooltipRuleBuilder rule(Identifier id) {
        requireOwnedOutputId(id);
        ensureDefinitionPathAvailable(id);
        ZenithTooltipRuleBuilder builder = new ZenithTooltipRuleBuilder();
        this.rules.put(id, builder);
        return builder;
    }

    protected final ZenithTooltipThemeBuilder theme(Identifier id) {
        requireOwnedOutputId(id);
        if (this.themes.containsKey(id)) {
            throw new IllegalStateException("Duplicate generated Zenith tooltip theme id: " + id);
        }
        ZenithTooltipThemeBuilder builder = new ZenithTooltipThemeBuilder();
        this.themes.put(id, builder);
        return builder;
    }

    @Override
    public final CompletableFuture<?> run(CachedOutput output) {
        this.templates.clear();
        this.rules.clear();
        this.themes.clear();
        addTooltips();

        Map<Identifier, Either<ZenithTooltipRule, ZenithTooltipTemplate>> definitions = new LinkedHashMap<>();
        this.templates.forEach((id, builder) -> definitions.put(id, Either.right(builder.build())));
        this.rules.forEach((id, builder) -> definitions.put(id, Either.left(builder.build())));

        Map<Identifier, ZenithTooltipTheme> encodedThemes = new LinkedHashMap<>();
        this.themes.forEach((id, builder) -> encodedThemes.put(id, builder.build()));

        CompletableFuture<?> definitionsFuture = DataProvider.saveAll(output, DEFINITION_CODEC, this.definitionPaths, definitions);
        CompletableFuture<?> themesFuture = DataProvider.saveAll(output, ZenithTooltipTheme.CODEC, this.themePaths, encodedThemes);
        return CompletableFuture.allOf(definitionsFuture, themesFuture);
    }

    @Override
    public String getName() {
        return "Zenith tooltip definitions and themes: " + this.modId;
    }


    protected final class GeneratedTooltipBuilder {
        private final Identifier documentId;
        private final ZenithTooltipTemplateBuilder template;
        private final ZenithTooltipRuleBuilder rule;

        private GeneratedTooltipBuilder(
                Identifier documentId,
                ZenithTooltipTemplateBuilder template,
                ZenithTooltipRuleBuilder rule
        ) {
            this.documentId = documentId;
            this.template = template;
            this.rule = rule;
        }

        public Identifier documentId() {
            return documentId;
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

    private void ensureDefinitionPathAvailable(Identifier id) {
        if (this.templates.containsKey(id) || this.rules.containsKey(id)) {
            throw new IllegalStateException("Duplicate generated Zenith tooltip definition id: " + id);
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
