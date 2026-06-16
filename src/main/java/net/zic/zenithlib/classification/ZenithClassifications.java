package net.zic.zenithlib.classification;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.ModList;
import net.zic.zenithlib.ZenithLib;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side repository for classifications.
 */
public final class ZenithClassifications {
    private static volatile Snapshot snapshot = Snapshot.empty();
    private static final Map<Identifier, ClassificationProvider> PROVIDERS = new ConcurrentHashMap<>();

    private ZenithClassifications() {}

    public static Optional<ZenithClassification> get(ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        return get(stack, BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    public static Optional<ZenithClassification> get(ItemStack stack, Identifier itemId) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }

        for (Map.Entry<Identifier, ClassificationProvider> entry : PROVIDERS.entrySet().stream()
                .sorted(Comparator.comparing(value -> value.getKey().toString()))
                .toList()) {
            try {
                Optional<ZenithClassification> provided = entry.getValue().resolve(stack, itemId);
                if (provided != null && provided.isPresent()) {
                    return provided;
                }
            } catch (RuntimeException exception) {
                ZenithLib.LOGGER.warn(
                        "Zenith classification provider {} failed while handling {}",
                        entry.getKey(),
                        itemId,
                        exception
                );
            }
        }

        Optional<BlockMatch> block = blockMatch(stack);
        for (CompiledRule rule : snapshot.rules()) {
            if (rule.selector().matches(stack, itemId, block)) {
                return Optional.of(rule.classification());
            }
        }

        return Optional.empty();
    }

    public static Map<Identifier, ZenithClassification.Category> categoriesView() {
        return snapshot.categories();
    }

    public static Map<Identifier, ZenithClassification.Rank> ranksView() {
        return snapshot.ranks();
    }

    public static Map<Identifier, ZenithClassification.Rule> rulesView() {
        return snapshot.sourceRules();
    }


    public static void registerProvider(Identifier id, ClassificationProvider provider) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(provider, "provider");
        if (PROVIDERS.putIfAbsent(id, provider) != null) {
            ZenithLib.LOGGER.debug("Skipped duplicate Zenith classification provider registration for {}", id);
        }
    }

    public static void registerProviderIfLoaded(String requiredModId, Identifier id, ClassificationProvider provider) {
        Objects.requireNonNull(requiredModId, "requiredModId");
        if (ModList.get().isLoaded(requiredModId)) {
            registerProvider(id, provider);
        }
    }

    public static List<Identifier> providerIds() {
        return PROVIDERS.keySet().stream().sorted().toList();
    }

    private static Optional<BlockMatch> blockMatch(ItemStack stack) {
        if (stack.getItem() instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            return Optional.of(new BlockMatch(BuiltInRegistries.BLOCK.getKey(block), block));
        }
        return Optional.empty();
    }

    private static synchronized void replaceCategories(Map<Identifier, ZenithClassification.Category> categories) {
        Snapshot current = snapshot;
        snapshot = Snapshot.create(Map.copyOf(categories), current.ranks(), current.sourceRules());
    }

    private static synchronized void replaceRanks(Map<Identifier, ZenithClassification.Rank> ranks) {
        Snapshot current = snapshot;
        snapshot = Snapshot.create(current.categories(), Map.copyOf(ranks), current.sourceRules());
    }

    private static synchronized LoadSummary replaceRules(Map<Identifier, ZenithClassification.Rule> rules) {
        Snapshot current = snapshot;
        snapshot = Snapshot.create(current.categories(), current.ranks(), Map.copyOf(rules));
        return new LoadSummary(rules.size(), snapshot.missingCategories(), snapshot.missingRanks());
    }

    private record Snapshot(
            Map<Identifier, ZenithClassification.Category> categories,
            Map<Identifier, ZenithClassification.Rank> ranks,
            Map<Identifier, ZenithClassification.Rule> sourceRules,
            List<CompiledRule> rules,
            int missingCategories,
            int missingRanks
    ) {
        private static final Comparator<CompiledRule> RULE_ORDER = Comparator
                .comparingInt(CompiledRule::priority)
                .reversed()
                .thenComparing(rule -> rule.id().toString());

        private static Snapshot empty() {
            return new Snapshot(Map.of(), Map.of(), Map.of(), List.of(), 0, 0);
        }

        private static Snapshot create(
                Map<Identifier, ZenithClassification.Category> categories,
                Map<Identifier, ZenithClassification.Rank> ranks,
                Map<Identifier, ZenithClassification.Rule> sourceRules
        ) {
            List<CompiledRule> compiled = new ArrayList<>();
            int missingCategories = 0;
            int missingRanks = 0;

            for (Map.Entry<Identifier, ZenithClassification.Rule> entry : sourceRules.entrySet()) {
                ZenithClassification.Rule rule = entry.getValue();
                Optional<ZenithClassification.Category> category = rule.category()
                        .flatMap(id -> Optional.ofNullable(categories.get(id)));
                Optional<ZenithClassification.Rank> rank = rule.rank()
                        .flatMap(id -> Optional.ofNullable(ranks.get(id)));

                if (rule.category().isPresent() && category.isEmpty()) {
                    missingCategories++;
                }
                if (rule.rank().isPresent() && rank.isEmpty()) {
                    missingRanks++;
                }

                ZenithClassification classification = new ZenithClassification(rule.category(), category, rule.rank(), rank);
                if (classification.isEmpty()) {
                    continue;
                }

                compiled.add(new CompiledRule(
                        entry.getKey(),
                        rule.priority(),
                        CompiledSelector.create(rule.selector()),
                        classification
                ));
            }

            compiled.sort(RULE_ORDER);
            return new Snapshot(
                    categories,
                    ranks,
                    sourceRules,
                    List.copyOf(compiled),
                    missingCategories,
                    missingRanks
            );
        }
    }

    private record LoadSummary(int rules, int missingCategories, int missingRanks) {}

    private record CompiledRule(
            Identifier id,
            int priority,
            CompiledSelector selector,
            ZenithClassification classification
    ) {}

    private record CompiledSelector(
            boolean all,
            Set<Identifier> items,
            Set<String> namespaces,
            List<TagKey<Item>> tags,
            Set<Identifier> blocks,
            Set<String> blockNamespaces,
            List<TagKey<Block>> blockTags
    ) {
        private static CompiledSelector create(ZenithClassification.Rule.Selector selector) {
            List<TagKey<Item>> tags = selector.tags().stream()
                    .map(id -> TagKey.create(BuiltInRegistries.ITEM.key(), id))
                    .toList();
            List<TagKey<Block>> blockTags = selector.blockTags().stream()
                    .map(id -> TagKey.create(BuiltInRegistries.BLOCK.key(), id))
                    .toList();

            return new CompiledSelector(
                    selector.all(),
                    Set.copyOf(new HashSet<>(selector.items())),
                    Set.copyOf(new HashSet<>(selector.namespaces())),
                    tags,
                    Set.copyOf(new HashSet<>(selector.blocks())),
                    Set.copyOf(new HashSet<>(selector.blockNamespaces())),
                    blockTags
            );
        }

        private boolean matches(ItemStack stack, Identifier itemId, Optional<BlockMatch> blockMatch) {
            if (all || items.contains(itemId) || namespaces.contains(itemId.getNamespace())) {
                return true;
            }

            for (TagKey<Item> tag : tags) {
                if (stack.is(tag)) {
                    return true;
                }
            }

            if (blockMatch.isEmpty()) {
                return false;
            }

            BlockMatch block = blockMatch.orElseThrow();
            if (blocks.contains(block.id()) || blockNamespaces.contains(block.id().getNamespace())) {
                return true;
            }

            for (TagKey<Block> tag : blockTags) {
                if (block.block().defaultBlockState().is(tag)) {
                    return true;
                }
            }

            return false;
        }
    }

    private record BlockMatch(Identifier id, Block block) {}

    @FunctionalInterface
    public interface ClassificationProvider {
        Optional<ZenithClassification> resolve(ItemStack stack, Identifier itemId);
    }

    public static final class CategoriesReloadListener extends SimpleJsonResourceReloadListener<ZenithClassification.Category> {
        public CategoriesReloadListener() {
            super(
                    ZenithClassification.Category.CODEC,
                    FileToIdConverter.json("zenith_tooltips/categories")
            );
        }

        @Override
        protected void apply(
                Map<Identifier, ZenithClassification.Category> objects,
                ResourceManager resourceManager,
                ProfilerFiller profiler
        ) {
            replaceCategories(objects);
            ZenithLib.LOGGER.info("Loaded {} Zenith classification categor(ies)", objects.size());
        }
    }

    public static final class RanksReloadListener extends SimpleJsonResourceReloadListener<ZenithClassification.Rank> {
        public RanksReloadListener() {
            super(
                    ZenithClassification.Rank.CODEC,
                    FileToIdConverter.json("zenith_tooltips/ranks")
            );
        }

        @Override
        protected void apply(
                Map<Identifier, ZenithClassification.Rank> objects,
                ResourceManager resourceManager,
                ProfilerFiller profiler
        ) {
            replaceRanks(objects);
            ZenithLib.LOGGER.info("Loaded {} Zenith classification rank(s)", objects.size());
        }
    }

    public static final class RulesReloadListener extends SimpleJsonResourceReloadListener<ZenithClassification.Rule> {
        public RulesReloadListener() {
            super(
                    ZenithClassification.Rule.CODEC,
                    FileToIdConverter.json("zenith_tooltips/classifications")
            );
        }

        @Override
        protected void apply(
                Map<Identifier, ZenithClassification.Rule> objects,
                ResourceManager resourceManager,
                ProfilerFiller profiler
        ) {
            LoadSummary summary = replaceRules(objects);
            ZenithLib.LOGGER.info("Loaded {} Zenith classification rule(s)", summary.rules());

            if (summary.missingCategories() > 0) {
                ZenithLib.LOGGER.warn(
                        "Ignored {} missing Zenith classification category reference(s)",
                        summary.missingCategories()
                );
            }
            if (summary.missingRanks() > 0) {
                ZenithLib.LOGGER.warn(
                        "Ignored {} missing Zenith classification rank reference(s)",
                        summary.missingRanks()
                );
            }
        }
    }
}
