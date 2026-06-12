package net.zic.zenithlib.tooltip.api.value;

import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.Optional;

/**
 * Neutral runtime value returned by a registered tooltip source.
 *
 * <p>Value sources describe data, while authored tooltip elements decide how that data
 * is presented. The initial built-in forms cover text and bounded integer progress;
 * additional generic value forms can be added without teaching the renderer about a
 * dependent mod's gameplay concepts.</p>
 */
public interface ZenithTooltipValue {
    record Text(Component component) implements ZenithTooltipValue {
        public Text {
            Objects.requireNonNull(component, "component");
            component = component.copy();
        }

        @Override
        public Component component() {
            return component.copy();
        }
    }

    record Progress(
            int value,
            int max,
            Optional<Component> displayText
    ) implements ZenithTooltipValue {
        public Progress {
            max = Math.max(1, max);
            value = Math.max(0, Math.min(value, max));
            displayText = displayText == null
                    ? Optional.empty()
                    : displayText.map(Component::copy);
        }

        public Progress(int value, int max) {
            this(value, max, Optional.empty());
        }

        public Progress(int value, int max, Component displayText) {
            this(value, max, Optional.ofNullable(displayText));
        }

        @Override
        public Optional<Component> displayText() {
            return displayText.map(Component::copy);
        }
    }

    static Text text(Component component) {
        return new Text(component);
    }

    static Progress progress(int value, int max) {
        return new Progress(value, max);
    }

    static Progress progress(int value, int max, Component displayText) {
        return new Progress(value, max, displayText);
    }
}
