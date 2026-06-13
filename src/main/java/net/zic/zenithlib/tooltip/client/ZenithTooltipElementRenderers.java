package net.zic.zenithlib.tooltip.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.zic.zenithlib.ZenithLib;
import net.zic.zenithlib.tooltip.api.ZenithTooltipTheme;
import net.zic.zenithlib.tooltip.api.animation.ZenithTooltipPresets;
import net.zic.zenithlib.tooltip.api.element.ZenithTooltipElement;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side registry for custom element layout and rendering.
 *
 * <p>Built-in elements stay in the compact core renderer. Dependent mods can register
 * only their custom element type here, avoiding edits to ZenithLib's layout and draw
 * switches.</p>
 */
public final class ZenithTooltipElementRenderers {
    private static final Map<Identifier, Entry<?>> ENTRIES = new ConcurrentHashMap<>();

    private ZenithTooltipElementRenderers() {}

    public static <T extends ZenithTooltipElement> void registerIfLoaded(
            String requiredModId,
            Identifier elementType,
            Class<T> elementClass,
            Preparer<T> preparer,
            Renderer renderer
    ) {
        Objects.requireNonNull(requiredModId, "requiredModId");
        if (ModList.get().isLoaded(requiredModId)) {
            register(elementType, elementClass, preparer, renderer);
        }
    }

    public static <T extends ZenithTooltipElement> void register(
            Identifier elementType,
            Class<T> elementClass,
            Preparer<T> preparer,
            Renderer renderer
    ) {
        Objects.requireNonNull(elementType, "elementType");
        Objects.requireNonNull(elementClass, "elementClass");
        Objects.requireNonNull(preparer, "preparer");
        Objects.requireNonNull(renderer, "renderer");

        Entry<T> entry = new Entry<>(elementClass, preparer, renderer);
        Entry<?> previous = ENTRIES.putIfAbsent(elementType, entry);
        if (previous != null) {
            ZenithLib.LOGGER.debug("Skipped duplicate Zenith tooltip element renderer registration for {}", elementType);
        }
    }

    static Optional<ZenithTooltipLayout.PreparedCustom> prepare(
            Font font,
            ZenithTooltipTheme theme,
            ItemStack stack,
            int innerWidth,
            ZenithTooltipElement element,
            long seed
    ) {
        Entry<?> entry = ENTRIES.get(element.type());
        if (entry == null) {
            return Optional.empty();
        }
        return entry.prepare(new LayoutContext(font, theme, stack, innerWidth, seed), element);
    }

    @FunctionalInterface
    public interface Preparer<T extends ZenithTooltipElement> {
        CustomElementLayout prepare(LayoutContext context, T element);
    }

    @FunctionalInterface
    public interface Renderer {
        void render(RenderContext context, Object data);
    }

    public record CustomElementLayout(
            int width,
            int height,
            Object data
    ) {
        public CustomElementLayout {
            width = Math.max(0, width);
            height = Math.max(0, height);
        }
    }

    public record LayoutContext(
            Font font,
            ZenithTooltipTheme theme,
            ItemStack stack,
            int innerWidth,
            long seed
    ) {
        public List<FormattedCharSequence> split(net.minecraft.network.chat.Component component, int width) {
            return List.copyOf(font.split(component, Math.max(1, width)));
        }

        public int lineBlockHeight(int lineCount, int gap) {
            return ZenithTooltipLayout.lineBlockHeight(font, lineCount, gap);
        }
    }

    public record RenderContext(
            Font font,
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            ItemStack stack,
            ZenithTooltipTheme theme,
            int innerWidth,
            ZenithTooltipAnimationState.Frame animationFrame,
            ZenithTooltipPresets.Resolved presets
    ) {}

    private record Entry<T extends ZenithTooltipElement>(
            Class<T> elementClass,
            Preparer<T> preparer,
            Renderer renderer
    ) {
        private Optional<ZenithTooltipLayout.PreparedCustom> prepare(LayoutContext context, ZenithTooltipElement element) {
            if (!elementClass.isInstance(element)) {
                return Optional.empty();
            }

            CustomElementLayout layout = preparer.prepare(context, elementClass.cast(element));
            if (layout == null) {
                return Optional.empty();
            }
            return Optional.of(new ZenithTooltipLayout.PreparedCustom(
                    layout.width(),
                    layout.height(),
                    layout.data(),
                    renderer
            ));
        }
    }
}
