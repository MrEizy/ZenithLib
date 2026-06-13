package net.zic.zenithlib.tooltip.api.context;

import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.Optional;

/**
 * Presentation adapter for an object being described by a contextual tooltip.
 */
public interface ZenithTooltipSubject {
    Component tooltipName(ZenithTooltipContext context);

    default Optional<Component> tooltipDescription(ZenithTooltipContext context) {
        return Optional.empty();
    }

    static ZenithTooltipSubject of(Component name) {
        return of(name, null);
    }

    static ZenithTooltipSubject of(Component name, Component description) {
        Objects.requireNonNull(name, "name");
        return new Simple(name.copy(), Optional.ofNullable(description).map(Component::copy));
    }

    /** Simple immutable adapter for subjects whose metadata is already available. */
    record Simple(Component name, Optional<Component> description) implements ZenithTooltipSubject {
        public Simple {
            Objects.requireNonNull(name, "name");
            name = name.copy();
            description = description == null
                    ? Optional.empty()
                    : description.map(Component::copy);
        }

        @Override
        public Component name() {
            return name.copy();
        }

        @Override
        public Optional<Component> description() {
            return description.map(Component::copy);
        }

        @Override
        public Component tooltipName(ZenithTooltipContext context) {
            return name.copy();
        }

        @Override
        public Optional<Component> tooltipDescription(ZenithTooltipContext context) {
            return description.map(Component::copy);
        }
    }
}
