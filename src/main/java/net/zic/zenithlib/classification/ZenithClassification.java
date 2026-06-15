package net.zic.zenithlib.classification;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.zenithlib.tooltip.api.ZenithTooltipColor;
import net.zic.zenithlib.tooltip.api.ZenithTooltipText;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data model for ZenithLib's item/block classification layer.
 */
public record ZenithClassification(
        Optional<Identifier> categoryId,
        Optional<Category> category,
        Optional<Identifier> rankId,
        Optional<Rank> rank
) {
    public ZenithClassification {
        categoryId = categoryId == null ? Optional.empty() : categoryId;
        category = category == null ? Optional.empty() : category;
        rankId = rankId == null ? Optional.empty() : rankId;
        rank = rank == null ? Optional.empty() : rank;
    }

    public boolean isEmpty() {
        return category.isEmpty() && rank.isEmpty();
    }

    public record Category(
            ZenithTooltipText label,
            ZenithTooltipColor color,
            Optional<Identifier> icon
    ) {
        public static final Codec<Category> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        ZenithTooltipText.CODEC.fieldOf("label").forGetter(Category::label),
                        ZenithTooltipColor.CODEC.optionalFieldOf("color", ZenithTooltipColor.ACCENT).forGetter(Category::color),
                        Identifier.CODEC.optionalFieldOf("icon").forGetter(Category::icon)
                ).apply(instance, Category::new)
        );

        public Category {
            icon = icon == null ? Optional.empty() : icon;
        }
    }

    public record Rank(
            ZenithTooltipText label,
            ZenithTooltipColor color,
            Optional<Identifier> icon
    ) {
        public static final Codec<Rank> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        ZenithTooltipText.CODEC.fieldOf("label").forGetter(Rank::label),
                        ZenithTooltipColor.CODEC.optionalFieldOf("color", ZenithTooltipColor.ACCENT).forGetter(Rank::color),
                        Identifier.CODEC.optionalFieldOf("icon").forGetter(Rank::icon)
                ).apply(instance, Rank::new)
        );

        public Rank {
            icon = icon == null ? Optional.empty() : icon;
        }
    }

    public record Rule(
            int priority,
            Selector selector,
            Optional<Identifier> category,
            Optional<Identifier> rank
    ) {
        public static final Codec<Rule> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.INT.optionalFieldOf("priority", 0).forGetter(Rule::priority),
                        Selector.CODEC.optionalFieldOf("selector").forGetter(rule -> Optional.of(rule.selector())),
                        Codec.STRING.listOf().optionalFieldOf("applies_to", List.<String>of()).forGetter(rule -> List.<String>of()),
                        Identifier.CODEC.optionalFieldOf("category").forGetter(Rule::category),
                        Identifier.CODEC.optionalFieldOf("rank").forGetter(Rule::rank),
                        Identifier.CODEC.optionalFieldOf("tier").forGetter(rule -> Optional.<Identifier>empty())
                ).apply(instance, Rule::create)
        );

        private static Rule create(
                int priority,
                Optional<Selector> selector,
                List<String> appliesTo,
                Optional<Identifier> category,
                Optional<Identifier> rank,
                Optional<Identifier> tier
        ) {
            Optional<Selector> parsed = Selector.fromAppliesTo(appliesTo);
            Selector combined = selector
                    .map(value -> parsed.map(value::merge).orElse(value))
                    .orElseGet(() -> parsed.orElseThrow(() ->
                            new IllegalArgumentException("Classification rule must define selector or applies_to")
                    ));
            return new Rule(priority, combined, category, rank.or(() -> tier));
        }

        public Rule {
            if (selector == null) {
                throw new IllegalArgumentException("Classification rule selector may not be null");
            }
            category = category == null ? Optional.empty() : category;
            rank = rank == null ? Optional.empty() : rank;

            if (category.isEmpty() && rank.isEmpty()) {
                throw new IllegalArgumentException("Classification rule must define a category, rank, or both");
            }
        }

        public record Selector(
                boolean all,
                List<Identifier> items,
                List<Identifier> tags,
                List<String> namespaces,
                List<Identifier> blocks,
                List<Identifier> blockTags,
                List<String> blockNamespaces
        ) {
            private static final String ITEM_TAG_PREFIX = "#";
            private static final String ITEM_NAMESPACE_PREFIX = "mod:";
            private static final String BLOCK_PREFIX = "block:";
            private static final String BLOCK_TAG_PREFIX = "#block:";
            private static final String BLOCK_NAMESPACE_PREFIX = "block_mod:";
            private static final String BLOCK_NAMESPACE_ALT_PREFIX = "block_namespace:";

            public static final Codec<Selector> CODEC = RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.BOOL.optionalFieldOf("all", false).forGetter(Selector::all),
                            Identifier.CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(Selector::items),
                            Identifier.CODEC.listOf().optionalFieldOf("tags", List.of()).forGetter(Selector::tags),
                            Codec.STRING.listOf().optionalFieldOf("namespaces", List.of()).forGetter(Selector::namespaces),
                            Identifier.CODEC.listOf().optionalFieldOf("blocks", List.of()).forGetter(Selector::blocks),
                            Identifier.CODEC.listOf().optionalFieldOf("block_tags", List.of()).forGetter(Selector::blockTags),
                            Codec.STRING.listOf().optionalFieldOf("block_namespaces", List.of()).forGetter(Selector::blockNamespaces)
                    ).apply(instance, Selector::new)
            );

            public Selector {
                items = List.copyOf(items);
                tags = List.copyOf(tags);
                namespaces = List.copyOf(namespaces);
                blocks = List.copyOf(blocks);
                blockTags = List.copyOf(blockTags);
                blockNamespaces = List.copyOf(blockNamespaces);

                if (!all
                        && items.isEmpty()
                        && tags.isEmpty()
                        && namespaces.isEmpty()
                        && blocks.isEmpty()
                        && blockTags.isEmpty()
                        && blockNamespaces.isEmpty()) {
                    throw new IllegalArgumentException("Classification selector must match items, item tags, item namespaces, blocks, block tags, block namespaces, or all items");
                }

                namespaces.forEach(Selector::validateNamespace);
                blockNamespaces.forEach(Selector::validateNamespace);
            }

            public static Optional<Selector> fromAppliesTo(List<String> appliesTo) {
                if (appliesTo == null || appliesTo.isEmpty()) {
                    return Optional.empty();
                }

                List<Identifier> items = new ArrayList<>();
                List<Identifier> tags = new ArrayList<>();
                List<String> namespaces = new ArrayList<>();
                List<Identifier> blocks = new ArrayList<>();
                List<Identifier> blockTags = new ArrayList<>();
                List<String> blockNamespaces = new ArrayList<>();
                boolean all = false;

                for (String raw : appliesTo) {
                    String value = raw == null ? "" : raw.trim();
                    if (value.isEmpty()) {
                        continue;
                    }

                    if ("*".equals(value) || "all".equals(value)) {
                        all = true;
                    } else if (value.startsWith(BLOCK_TAG_PREFIX)) {
                        blockTags.add(Identifier.parse(value.substring(BLOCK_TAG_PREFIX.length())));
                    } else if (value.startsWith(BLOCK_NAMESPACE_PREFIX)) {
                        blockNamespaces.add(value.substring(BLOCK_NAMESPACE_PREFIX.length()));
                    } else if (value.startsWith(BLOCK_NAMESPACE_ALT_PREFIX)) {
                        blockNamespaces.add(value.substring(BLOCK_NAMESPACE_ALT_PREFIX.length()));
                    } else if (value.startsWith(BLOCK_PREFIX)) {
                        blocks.add(Identifier.parse(value.substring(BLOCK_PREFIX.length())));
                    } else if (value.startsWith(ITEM_NAMESPACE_PREFIX)) {
                        namespaces.add(value.substring(ITEM_NAMESPACE_PREFIX.length()));
                    } else if (value.startsWith(ITEM_TAG_PREFIX)) {
                        tags.add(Identifier.parse(value.substring(ITEM_TAG_PREFIX.length())));
                    } else {
                        items.add(Identifier.parse(value));
                    }
                }

                if (!all
                        && items.isEmpty()
                        && tags.isEmpty()
                        && namespaces.isEmpty()
                        && blocks.isEmpty()
                        && blockTags.isEmpty()
                        && blockNamespaces.isEmpty()) {
                    return Optional.empty();
                }

                return Optional.of(new Selector(all, items, tags, namespaces, blocks, blockTags, blockNamespaces));
            }

            public Selector merge(Selector other) {
                if (other == null) {
                    return this;
                }

                List<Identifier> mergedItems = new ArrayList<>(items);
                mergedItems.addAll(other.items);
                List<Identifier> mergedTags = new ArrayList<>(tags);
                mergedTags.addAll(other.tags);
                List<String> mergedNamespaces = new ArrayList<>(namespaces);
                mergedNamespaces.addAll(other.namespaces);
                List<Identifier> mergedBlocks = new ArrayList<>(blocks);
                mergedBlocks.addAll(other.blocks);
                List<Identifier> mergedBlockTags = new ArrayList<>(blockTags);
                mergedBlockTags.addAll(other.blockTags);
                List<String> mergedBlockNamespaces = new ArrayList<>(blockNamespaces);
                mergedBlockNamespaces.addAll(other.blockNamespaces);

                return new Selector(
                        all || other.all,
                        mergedItems,
                        mergedTags,
                        mergedNamespaces,
                        mergedBlocks,
                        mergedBlockTags,
                        mergedBlockNamespaces
                );
            }

            public boolean isEmpty() {
                return !all
                        && items.isEmpty()
                        && tags.isEmpty()
                        && namespaces.isEmpty()
                        && blocks.isEmpty()
                        && blockTags.isEmpty()
                        && blockNamespaces.isEmpty();
            }

            private static void validateNamespace(String namespace) {
                if (namespace == null || namespace.isBlank() || namespace.contains(":")) {
                    throw new IllegalArgumentException("Classification selector namespace must be a plain namespace: " + namespace);
                }
            }
        }
    }
}
