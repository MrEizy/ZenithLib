package net.zic.zenithlib.classification.datagen;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.zic.zenithlib.classification.ZenithClassification;
import net.zic.zenithlib.tooltip.api.ZenithTooltipColor;
import net.zic.zenithlib.tooltip.api.ZenithTooltipText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Base data provider for generated Zenith classification categories, ranks, and
 * item/block assignment rules.
 */
public abstract class ZenithClassificationDataProvider implements DataProvider {
    private final String modId;
    private final PackOutput.PathProvider categoryPaths;
    private final PackOutput.PathProvider rankPaths;
    private final PackOutput.PathProvider rulePaths;
    private final Map<Identifier, CategoryBuilder> categories = new LinkedHashMap<>();
    private final Map<Identifier, RankBuilder> ranks = new LinkedHashMap<>();
    private final Map<Identifier, RuleBuilder> rules = new LinkedHashMap<>();

    protected ZenithClassificationDataProvider(PackOutput output, String modId) {
        this.modId = Objects.requireNonNull(modId, "modId");
        this.categoryPaths = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "zenith_tooltips/categories");
        this.rankPaths = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "zenith_tooltips/ranks");
        this.rulePaths = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "zenith_tooltips/classifications");
    }

    protected abstract void addClassifications();

    protected final Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(this.modId, path);
    }

    protected final Identifier external(String namespace, String path) {
        return Identifier.fromNamespaceAndPath(namespace, path);
    }

    protected final Identifier minecraft(String path) {
        return external("minecraft", path);
    }

    protected final ZenithTooltipText t(String key) {
        return ZenithTooltipText.translatable(key);
    }

    protected final ZenithTooltipText lit(String text) {
        return ZenithTooltipText.literal(text);
    }

    protected final CategoryBuilder category(String path) {
        return category(id(path));
    }

    protected final RankBuilder rank(String path) {
        return rank(id(path));
    }

    protected final RuleBuilder classification(String path) {
        return classification(id(path));
    }

    protected final void category(String path, Consumer<CategoryBuilder> action) {
        Objects.requireNonNull(action, "action").accept(category(path));
    }

    protected final void rank(String path, Consumer<RankBuilder> action) {
        Objects.requireNonNull(action, "action").accept(rank(path));
    }

    protected final void classification(String path, Consumer<RuleBuilder> action) {
        Objects.requireNonNull(action, "action").accept(classification(path));
    }

    protected final CategoryBuilder category(Identifier id) {
        requireOwnedOutputId(id);
        if (this.categories.containsKey(id)) {
            throw new IllegalStateException("Duplicate generated Zenith classification category id: " + id);
        }
        CategoryBuilder builder = new CategoryBuilder();
        this.categories.put(id, builder);
        return builder;
    }

    protected final RankBuilder rank(Identifier id) {
        requireOwnedOutputId(id);
        if (this.ranks.containsKey(id)) {
            throw new IllegalStateException("Duplicate generated Zenith classification rank id: " + id);
        }
        RankBuilder builder = new RankBuilder();
        this.ranks.put(id, builder);
        return builder;
    }

    protected final RuleBuilder classification(Identifier id) {
        requireOwnedOutputId(id);
        if (this.rules.containsKey(id)) {
            throw new IllegalStateException("Duplicate generated Zenith classification rule id: " + id);
        }
        RuleBuilder builder = new RuleBuilder();
        this.rules.put(id, builder);
        return builder;
    }

    @Override
    public final CompletableFuture<?> run(CachedOutput output) {
        this.categories.clear();
        this.ranks.clear();
        this.rules.clear();
        addClassifications();

        Map<Identifier, ZenithClassification.Category> encodedCategories = new LinkedHashMap<>();
        this.categories.forEach((id, builder) -> encodedCategories.put(id, builder.build()));

        Map<Identifier, ZenithClassification.Rank> encodedRanks = new LinkedHashMap<>();
        this.ranks.forEach((id, builder) -> encodedRanks.put(id, builder.build()));

        Map<Identifier, ZenithClassification.Rule> encodedRules = new LinkedHashMap<>();
        this.rules.forEach((id, builder) -> encodedRules.put(id, builder.build()));

        CompletableFuture<?> categoriesFuture = DataProvider.saveAll(output, ZenithClassification.Category.CODEC, this.categoryPaths, encodedCategories);
        CompletableFuture<?> ranksFuture = DataProvider.saveAll(output, ZenithClassification.Rank.CODEC, this.rankPaths, encodedRanks);
        CompletableFuture<?> rulesFuture = DataProvider.saveAll(output, ZenithClassification.Rule.CODEC, this.rulePaths, encodedRules);
        return CompletableFuture.allOf(categoriesFuture, ranksFuture, rulesFuture);
    }

    @Override
    public String getName() {
        return "Zenith classification resources: " + this.modId;
    }

    private void requireOwnedOutputId(Identifier id) {
        Objects.requireNonNull(id, "id");
        if (!this.modId.equals(id.getNamespace())) {
            throw new IllegalArgumentException(
                    "Generated Zenith classification resources must be in provider namespace '" + this.modId + "': " + id
            );
        }
    }

    public static final class CategoryBuilder {
        private ZenithTooltipText label;
        private ZenithTooltipColor color = ZenithTooltipColor.ACCENT;
        private Optional<Identifier> icon = Optional.empty();

        public CategoryBuilder label(ZenithTooltipText label) {
            this.label = Objects.requireNonNull(label, "label");
            return this;
        }

        public CategoryBuilder label(String translationKey) {
            return label(ZenithTooltipText.translatable(translationKey));
        }

        public CategoryBuilder literalLabel(String label) {
            return label(ZenithTooltipText.literal(label));
        }

        public CategoryBuilder color(ZenithTooltipColor color) {
            this.color = Objects.requireNonNull(color, "color");
            return this;
        }

        public CategoryBuilder color(String tokenOrHex) {
            return color(new ZenithTooltipColor(tokenOrHex));
        }

        public CategoryBuilder icon(Identifier icon) {
            this.icon = Optional.of(Objects.requireNonNull(icon, "icon"));
            return this;
        }

        public ZenithClassification.Category build() {
            if (label == null) {
                throw new IllegalStateException("A generated Zenith classification category must define a label");
            }
            return new ZenithClassification.Category(label, color, icon);
        }
    }

    public static final class RankBuilder {
        private ZenithTooltipText label;
        private ZenithTooltipColor color = ZenithTooltipColor.ACCENT;
        private Optional<Identifier> icon = Optional.empty();

        public RankBuilder label(ZenithTooltipText label) {
            this.label = Objects.requireNonNull(label, "label");
            return this;
        }

        public RankBuilder label(String translationKey) {
            return label(ZenithTooltipText.translatable(translationKey));
        }

        public RankBuilder literalLabel(String label) {
            return label(ZenithTooltipText.literal(label));
        }

        public RankBuilder color(ZenithTooltipColor color) {
            this.color = Objects.requireNonNull(color, "color");
            return this;
        }

        public RankBuilder color(String tokenOrHex) {
            return color(new ZenithTooltipColor(tokenOrHex));
        }

        public RankBuilder icon(Identifier icon) {
            this.icon = Optional.of(Objects.requireNonNull(icon, "icon"));
            return this;
        }

        public ZenithClassification.Rank build() {
            if (label == null) {
                throw new IllegalStateException("A generated Zenith classification rank must define a label");
            }
            return new ZenithClassification.Rank(label, color, icon);
        }
    }

    public static final class RuleBuilder {
        private int priority;
        private boolean all;
        private final List<Identifier> items = new ArrayList<>();
        private final List<Identifier> tags = new ArrayList<>();
        private final List<String> namespaces = new ArrayList<>();
        private final List<Identifier> blocks = new ArrayList<>();
        private final List<Identifier> blockTags = new ArrayList<>();
        private final List<String> blockNamespaces = new ArrayList<>();
        private Optional<Identifier> category = Optional.empty();
        private Optional<Identifier> rank = Optional.empty();

        public RuleBuilder priority(int priority) {
            this.priority = priority;
            return this;
        }

        public RuleBuilder all() {
            this.all = true;
            return this;
        }

        public RuleBuilder items(Identifier... items) {
            this.items.addAll(Arrays.stream(items).map(item -> Objects.requireNonNull(item, "item")).toList());
            return this;
        }

        public RuleBuilder tags(Identifier... tags) {
            this.tags.addAll(Arrays.stream(tags).map(tag -> Objects.requireNonNull(tag, "tag")).toList());
            return this;
        }

        public RuleBuilder namespaces(String... namespaces) {
            this.namespaces.addAll(Arrays.stream(namespaces).map(namespace -> Objects.requireNonNull(namespace, "namespace")).toList());
            return this;
        }

        public RuleBuilder blocks(Identifier... blocks) {
            this.blocks.addAll(Arrays.stream(blocks).map(block -> Objects.requireNonNull(block, "block")).toList());
            return this;
        }

        public RuleBuilder blockTags(Identifier... blockTags) {
            this.blockTags.addAll(Arrays.stream(blockTags).map(tag -> Objects.requireNonNull(tag, "blockTag")).toList());
            return this;
        }

        public RuleBuilder blockNamespaces(String... blockNamespaces) {
            this.blockNamespaces.addAll(Arrays.stream(blockNamespaces).map(namespace -> Objects.requireNonNull(namespace, "blockNamespace")).toList());
            return this;
        }

        public RuleBuilder category(Identifier category) {
            this.category = Optional.of(Objects.requireNonNull(category, "category"));
            return this;
        }

        public RuleBuilder rank(Identifier rank) {
            this.rank = Optional.of(Objects.requireNonNull(rank, "rank"));
            return this;
        }

        public ZenithClassification.Rule build() {
            return new ZenithClassification.Rule(
                    priority,
                    new ZenithClassification.Rule.Selector(
                            all,
                            items,
                            tags,
                            namespaces,
                            blocks,
                            blockTags,
                            blockNamespaces
                    ),
                    category,
                    rank
            );
        }
    }
}
