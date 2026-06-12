package net.zic.zenithlib.tooltip.api.value;

import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Neutral runtime value returned by a registered tooltip source.
 *
 * <p>Value sources describe data, while tooltip elements decide how that data
 * is presented.</p>
 */
public interface ZenithTooltipValue {
    enum Tone {
        POSITIVE,
        NEGATIVE,
        NEUTRAL,
        SPECIAL
    }

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

    record TextList(List<Component> entries) implements ZenithTooltipValue {
        public TextList {
            Objects.requireNonNull(entries, "entries");
            entries = copyComponents(entries);
        }

        @Override
        public List<Component> entries() {
            return copyComponents(entries);
        }
    }

    record Row(Component left, Component right, Tone tone) {
        public Row {
            Objects.requireNonNull(left, "left");
            Objects.requireNonNull(right, "right");
            left = left.copy();
            right = right.copy();
            tone = tone == null ? Tone.NEUTRAL : tone;
        }

        @Override
        public Component left() {
            return left.copy();
        }

        @Override
        public Component right() {
            return right.copy();
        }
    }

    record Rows(List<Row> entries) implements ZenithTooltipValue {
        public Rows {
            Objects.requireNonNull(entries, "entries");
            entries = List.copyOf(entries);
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

    static Rows rows(Collection<Row> entries) {
        return new Rows(List.copyOf(entries));
    }

    static Row row(Component left, Component right, Tone tone) {
        return new Row(left, right, tone);
    }

    static TextList textList(Collection<? extends Component> entries) {
        return new TextList(copyComponents(entries));
    }

    private static List<Component> copyComponents(
            Collection<? extends Component> components
    ) {
        return components.stream()
                .<Component>map(Component::copy)
                .toList();
    }
}
