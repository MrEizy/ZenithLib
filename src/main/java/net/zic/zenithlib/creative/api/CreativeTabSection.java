package net.zic.zenithlib.creative.api;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A named, icon-backed filtered view of one creative-mode tab.
 *
 * <p>Sections do not own or reorder creative entries. Their matcher is applied to the
 * entries already produced by the target creative tab, preserving vanilla and modded
 * tab population rules.</p>
 */
public record CreativeTabSection(
        Identifier id,
        Component title,
        Supplier<ItemStack> iconSupplier,
        Predicate<ItemStack> matcher,
        BooleanSupplier visibility,
        int order
) {
    public CreativeTabSection {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(iconSupplier, "iconSupplier");
        Objects.requireNonNull(matcher, "matcher");
        Objects.requireNonNull(visibility, "visibility");
    }

    public static Builder builder(Identifier id) {
        return new Builder(id);
    }

    public boolean isVisible() {
        return visibility.getAsBoolean();
    }

    public boolean matches(ItemStack stack) {
        return matcher.test(stack);
    }

    public ItemStack icon() {
        ItemStack icon = iconSupplier.get();
        if (icon == null || icon.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack copy = icon.copy();
        copy.setCount(1);
        return copy;
    }

    public static final class Builder {
        private final Identifier id;
        private Component title;
        private Supplier<ItemStack> iconSupplier;
        private Predicate<ItemStack> matcher;
        private BooleanSupplier visibility = () -> true;
        private int order;

        private Builder(Identifier id) {
            this.id = Objects.requireNonNull(id, "id");
        }

        public Builder title(Component title) {
            this.title = Objects.requireNonNull(title, "title");
            return this;
        }

        public Builder icon(Supplier<ItemStack> iconSupplier) {
            this.iconSupplier = Objects.requireNonNull(iconSupplier, "iconSupplier");
            return this;
        }

        public Builder matching(Predicate<ItemStack> matcher) {
            Objects.requireNonNull(matcher, "matcher");
            this.matcher = this.matcher == null ? matcher : this.matcher.or(matcher);
            return this;
        }

        public Builder matchingTag(TagKey<Item> tag) {
            Objects.requireNonNull(tag, "tag");
            return matching(stack -> stack.is(tag));
        }

        // yeah but like that one I showed i made

        public Builder matchingItems(ItemLike... items) {
            Objects.requireNonNull(items, "items");
            Set<Item> accepted = new HashSet<>();
            Arrays.stream(items)
                    .map(item -> Objects.requireNonNull(item, "item").asItem())
                    .forEach(accepted::add);
            return matching(stack -> accepted.contains(stack.getItem()));
        }

        public Builder visibleWhen(BooleanSupplier visibility) {
            this.visibility = Objects.requireNonNull(visibility, "visibility");
            return this;
        }

        public Builder order(int order) {
            this.order = order;
            return this;
        }

        public CreativeTabSection build() {
            if (title == null) {
                throw new IllegalStateException("Creative section " + id + " is missing a title");
            }
            if (iconSupplier == null) {
                throw new IllegalStateException("Creative section " + id + " is missing an icon");
            }
            if (matcher == null) {
                throw new IllegalStateException("Creative section " + id + " is missing an item matcher");
            }

            return new CreativeTabSection(id, title, iconSupplier, matcher, visibility, order);
        }
    }
}
