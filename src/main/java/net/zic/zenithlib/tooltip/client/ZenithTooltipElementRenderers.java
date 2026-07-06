package net.zic.zenithlib.tooltip.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.fml.ModList;
import net.zic.zenithlib.Config;
import net.zic.zenithlib.ZenithLib;
import net.zic.zenithlib.tooltip.api.ZenithTooltipTheme;
import net.zic.zenithlib.tooltip.api.animation.ZenithTooltipPresets;
import net.zic.zenithlib.tooltip.api.element.BadgeElement;
import net.zic.zenithlib.tooltip.api.element.BadgeRowElement;
import net.zic.zenithlib.tooltip.api.element.BarElement;
import net.zic.zenithlib.tooltip.api.element.DividerElement;
import net.zic.zenithlib.tooltip.api.element.EntityPreviewElement;
import net.zic.zenithlib.tooltip.api.element.HeaderElement;
import net.zic.zenithlib.tooltip.api.element.IconElement;
import net.zic.zenithlib.tooltip.api.element.RowElement;
import net.zic.zenithlib.tooltip.api.element.SpacerElement;
import net.zic.zenithlib.tooltip.api.element.TextElement;
import net.zic.zenithlib.tooltip.api.element.TitleIconElement;
import net.zic.zenithlib.tooltip.api.element.ZenithTooltipElement;
import net.zic.zenithlib.tooltip.api.element.ZenithTooltipElementTypes;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side registry for element measurement and rendering hooks.
 */
public final class ZenithTooltipElementRenderers {
    private static final Map<Identifier, Entry<?>> ENTRIES = new ConcurrentHashMap<>();

    static {
        registerBuiltIns();
    }

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

        registerInternal(
                elementType,
                elementClass,
                (context, element) -> {
                    CustomElementLayout layout = preparer.prepare(context, element);
                    if (layout == null) {
                        return Optional.empty();
                    }
                    return Optional.of(new ZenithTooltipLayout.PreparedCustom(
                            layout.width(),
                            layout.height(),
                            layout.data(),
                            renderer
                    ));
                },
                false
        );
    }

    static Optional<ZenithTooltipLayout.PreparedElement> prepare(
            Font font,
            ZenithTooltipTheme theme,
            ItemStack stack,
            int innerWidth,
            ZenithTooltipElement element,
            long seed,
            ZenithTooltipAnimationContext.Settings animationSettings
    ) {
        Entry<?> entry = ENTRIES.get(element.type());
        if (entry == null) {
            return Optional.empty();
        }
        return entry.prepare(new LayoutContext(font, theme, stack, innerWidth, seed, animationSettings), element);
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
            long seed,
            ZenithTooltipAnimationContext.Settings animationSettings
    ) {
        public List<FormattedCharSequence> split(Component component, int width) {
            return List.copyOf(font.split(component, Math.max(1, width)));
        }

        public int maxLineWidth(List<FormattedCharSequence> lines) {
            return ZenithTooltipLayout.maxLineWidth(font, lines);
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
            ZenithTooltipPresets.Resolved presets,
            ZenithTooltipAnimationContext.Settings animationSettings
    ) {}

    @FunctionalInterface
    private interface ElementPreparer<T extends ZenithTooltipElement> {
        Optional<ZenithTooltipLayout.PreparedElement> prepare(LayoutContext context, T element);
    }

    private static <T extends ZenithTooltipElement> void registerInternal(
            Identifier elementType,
            Class<T> elementClass,
            ElementPreparer<T> preparer,
            boolean builtIn
    ) {
        Entry<T> entry = new Entry<>(elementClass, preparer);
        Entry<?> previous = ENTRIES.putIfAbsent(elementType, entry);
        if (previous != null && !builtIn) {
            ZenithLib.LOGGER.debug("Skipped duplicate Zenith tooltip element renderer registration for {}", elementType);
        }
    }

    private static <T extends ZenithTooltipElement> void registerBuiltIn(
            Identifier elementType,
            Class<T> elementClass,
            ElementPreparer<T> preparer
    ) {
        registerInternal(elementType, elementClass, preparer, true);
    }

    private static void registerBuiltIns() {
        registerBuiltIn(ZenithTooltipElementTypes.TEXT, TextElement.class, ZenithTooltipElementRenderers::prepareText);
        registerBuiltIn(ZenithTooltipElementTypes.HEADER, HeaderElement.class, ZenithTooltipElementRenderers::prepareHeader);
        registerBuiltIn(ZenithTooltipElementTypes.DIVIDER, DividerElement.class, ZenithTooltipElementRenderers::prepareDivider);
        registerBuiltIn(ZenithTooltipElementTypes.SPACER, SpacerElement.class, ZenithTooltipElementRenderers::prepareSpacer);
        registerBuiltIn(ZenithTooltipElementTypes.ROW, RowElement.class, ZenithTooltipElementRenderers::prepareRow);
        registerBuiltIn(ZenithTooltipElementTypes.BADGE, BadgeElement.class, ZenithTooltipElementRenderers::prepareBadge);
        registerBuiltIn(ZenithTooltipElementTypes.BADGE_ROW, BadgeRowElement.class, ZenithTooltipElementRenderers::prepareBadgeRow);
        registerBuiltIn(ZenithTooltipElementTypes.BAR, BarElement.class, ZenithTooltipElementRenderers::prepareBar);
        registerBuiltIn(ZenithTooltipElementTypes.ENTITY_PREVIEW, EntityPreviewElement.class, ZenithTooltipElementRenderers::prepareEntityPreview);
        registerBuiltIn(ZenithTooltipElementTypes.ICON, IconElement.class, ZenithTooltipElementRenderers::prepareIcon);
        registerBuiltIn(ZenithTooltipElementTypes.TITLE_ICON, TitleIconElement.class, ZenithTooltipElementRenderers::prepareTitleIcon);
    }

    private static Optional<ZenithTooltipLayout.PreparedElement> prepareText(LayoutContext context, TextElement text) {
        List<FormattedCharSequence> lines = context.split(text.text().component(), context.innerWidth());
        int width = context.maxLineWidth(lines);
        int animationPadding = text.effect()
                .map(effect -> ZenithTooltipTextAnimator.verticalPadding(effect, context.animationSettings()))
                .orElse(0);
        return Optional.of(new ZenithTooltipLayout.PreparedText(
                lines,
                text.color().resolve(context.theme()),
                width,
                context.lineBlockHeight(lines.size(), ZenithTooltipLayout.LINE_GAP) + animationPadding * 2,
                text.effect(),
                animationPadding,
                context.seed()
        ));
    }

    private static Optional<ZenithTooltipLayout.PreparedElement> prepareHeader(LayoutContext context, HeaderElement header) {
        List<FormattedCharSequence> lines = context.split(
                header.text().component().copy().withStyle(ChatFormatting.BOLD),
                context.innerWidth()
        );
        int animationPadding = header.effect()
                .map(effect -> ZenithTooltipTextAnimator.verticalPadding(effect, context.animationSettings()))
                .orElse(0);
        return Optional.of(new ZenithTooltipLayout.PreparedHeader(
                lines,
                header.color().resolve(context.theme()),
                context.maxLineWidth(lines),
                context.lineBlockHeight(lines.size(), ZenithTooltipLayout.LINE_GAP) + (header.underline() ? 2 : 0) + animationPadding * 2,
                header.effect(),
                animationPadding,
                context.seed(),
                header.underline()
        ));
    }

    private static Optional<ZenithTooltipLayout.PreparedElement> prepareDivider(LayoutContext context, DividerElement divider) {
        ZenithTooltipTheme.DividerStyle style = context.theme().dividerStyle();
        int thickness = divider.thickness().orElse(style.thickness());
        int ornamentSize = dividerOrnamentSize(style, thickness);
        int height = style.gapAbove() + Math.max(thickness, ornamentSize) + style.gapBelow();
        int color = divider.color().map(value -> value.resolve(context.theme())).orElseGet(() -> style.colorValue(context.theme()));
        int endColor = divider.endColor().map(value -> value.resolve(context.theme())).orElse(color);
        int inset = divider.inset().orElse(0);
        return Optional.of(new ZenithTooltipLayout.PreparedDivider(0, height, color, endColor, inset, thickness));
    }

    private static Optional<ZenithTooltipLayout.PreparedElement> prepareSpacer(LayoutContext context, SpacerElement spacer) {
        return Optional.of(new ZenithTooltipLayout.PreparedSpacer(0, Math.max(0, spacer.height())));
    }

    private static Optional<ZenithTooltipLayout.PreparedElement> prepareRow(LayoutContext context, RowElement row) {
        int iconWidth = row.icon().map(icon -> icon.size() + ZenithTooltipLayout.INLINE_ICON_GAP).orElse(0);
        int availableWidth = Math.max(2, context.innerWidth() - iconWidth - ZenithTooltipLayout.ROW_COLUMN_GAP);
        int minimumColumnWidth = Math.max(1, Math.min(ZenithTooltipLayout.ROW_MIN_COLUMN_WIDTH, availableWidth / 2));
        int leftNaturalWidth = context.font().width(row.left().component());
        int rightNaturalWidth = context.font().width(row.right().component());

        int leftWidth;
        int rightWidth;

        if (leftNaturalWidth + rightNaturalWidth <= availableWidth) {
            leftWidth = Math.max(1, leftNaturalWidth);
            rightWidth = Math.max(1, rightNaturalWidth);
        } else if (leftNaturalWidth <= availableWidth - minimumColumnWidth) {
            leftWidth = Math.max(1, leftNaturalWidth);
            rightWidth = Math.max(1, availableWidth - leftWidth);
        } else if (rightNaturalWidth <= availableWidth - minimumColumnWidth) {
            rightWidth = Math.max(1, rightNaturalWidth);
            leftWidth = Math.max(1, availableWidth - rightWidth);
        } else {
            leftWidth = Math.max(minimumColumnWidth, availableWidth * 11 / 20);
            rightWidth = Math.max(1, availableWidth - leftWidth);
        }

        List<FormattedCharSequence> leftLines = context.split(row.left().component(), leftWidth);
        List<FormattedCharSequence> rightLines = context.split(row.right().component(), rightWidth);
        int leftMeasured = context.maxLineWidth(leftLines);
        int rightMeasured = context.maxLineWidth(rightLines);
        int width = iconWidth + leftMeasured + ZenithTooltipLayout.ROW_COLUMN_GAP + rightMeasured;
        int textHeight = Math.max(
                context.lineBlockHeight(leftLines.size(), ZenithTooltipLayout.LINE_GAP),
                context.lineBlockHeight(rightLines.size(), ZenithTooltipLayout.LINE_GAP)
        );
        int height = Math.max(textHeight, row.icon().map(net.zic.zenithlib.tooltip.api.ZenithTooltipInlineIcon::size).orElse(0));

        return Optional.of(new ZenithTooltipLayout.PreparedRow(
                leftLines,
                rightLines,
                row.leftColor().resolve(context.theme()),
                row.rightColor().resolve(context.theme()),
                row.icon(),
                width,
                height
        ));
    }

    private static Optional<ZenithTooltipLayout.PreparedElement> prepareBadge(LayoutContext context, BadgeElement badge) {
        ZenithTooltipTheme.BadgeStyle style = context.theme().badgeStyle();
        int iconWidth = badge.icon().map(icon -> icon.size() + ZenithTooltipLayout.INLINE_ICON_GAP).orElse(0);
        int textWidth = Math.max(1, context.innerWidth() - style.horizontalPadding() * 2 - iconWidth);
        List<FormattedCharSequence> lines = context.split(badge.text().component(), textWidth);
        int animationPadding = badge.effect()
                .map(effect -> ZenithTooltipTextAnimator.verticalPadding(effect, context.animationSettings()))
                .orElse(0);
        int width = iconWidth + context.maxLineWidth(lines) + style.horizontalPadding() * 2;
        int textHeight = context.lineBlockHeight(lines.size(), 0) + animationPadding * 2;
        int height = Math.max(textHeight, badge.icon().map(net.zic.zenithlib.tooltip.api.ZenithTooltipInlineIcon::size).orElse(0))
                + style.verticalPadding() * 2;
        return Optional.of(new ZenithTooltipLayout.PreparedBadge(
                lines,
                badge.textColor().resolve(context.theme()),
                badge.backgroundColor().resolve(context.theme()),
                badge.borderColor().resolve(context.theme()),
                badge.icon(),
                width,
                height,
                badge.effect(),
                animationPadding,
                context.seed(),
                badge.backgroundGradient().stream().map(color -> color.resolve(context.theme())).toList(),
                badge.gradientDirection()
        ));
    }

    private static Optional<ZenithTooltipLayout.PreparedElement> prepareBadgeRow(LayoutContext context, BadgeRowElement row) {
        if (row.badges().isEmpty()) {
            return Optional.empty();
        }

        List<ZenithTooltipLayout.PreparedBadgePlacement> placements = new java.util.ArrayList<>();
        int x = 0;
        int y = 0;
        int rowHeight = 0;
        int width = 0;

        for (BadgeElement badge : row.badges()) {
            Optional<ZenithTooltipLayout.PreparedElement> prepared = prepareBadge(context, badge);
            if (prepared.isEmpty() || !(prepared.orElseThrow() instanceof ZenithTooltipLayout.PreparedBadge preparedBadge)) {
                continue;
            }

            if (row.wrap() && x > 0 && x + preparedBadge.width() > context.innerWidth()) {
                x = 0;
                y += rowHeight + row.rowSpacing();
                rowHeight = 0;
            }

            placements.add(new ZenithTooltipLayout.PreparedBadgePlacement(preparedBadge, x, y));
            width = Math.max(width, x + preparedBadge.width());
            rowHeight = Math.max(rowHeight, preparedBadge.height());
            x += preparedBadge.width() + row.spacing();
        }

        if (placements.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new ZenithTooltipLayout.PreparedBadgeRow(placements, width, y + rowHeight));
    }

    private static Optional<ZenithTooltipLayout.PreparedElement> prepareBar(LayoutContext context, BarElement bar) {
        Component value = bar.valueText().isBlank()
                ? Component.literal(bar.value() + " / " + bar.max())
                : bar.valueText().component();
        int valueNaturalWidth = context.font().width(value);
        int labelWidth = Math.max(1, context.innerWidth() - ZenithTooltipLayout.ROW_COLUMN_GAP - valueNaturalWidth);
        int valueWidth = Math.max(1, Math.min(context.innerWidth() / 2, valueNaturalWidth));
        List<FormattedCharSequence> labelLines = context.split(bar.label().component(), labelWidth);
        List<FormattedCharSequence> valueLines = context.split(value, valueWidth);
        int width = context.maxLineWidth(labelLines) + ZenithTooltipLayout.ROW_COLUMN_GAP + context.maxLineWidth(valueLines);
        int labelHeight = Math.max(
                context.lineBlockHeight(labelLines.size(), ZenithTooltipLayout.LINE_GAP),
                context.lineBlockHeight(valueLines.size(), ZenithTooltipLayout.LINE_GAP)
        );
        int height = labelHeight + context.theme().barStyle().labelGap() + context.theme().barStyle().height();

        return Optional.of(new ZenithTooltipLayout.PreparedBar(
                labelLines,
                valueLines,
                bar.color().resolve(context.theme()),
                bar.progress(),
                labelHeight,
                width,
                height
        ));
    }

    private static Optional<ZenithTooltipLayout.PreparedElement> prepareEntityPreview(LayoutContext context, EntityPreviewElement preview) {
        if (!Config.SHOW_SPAWN_EGG_ENTITY_PREVIEWS.get() || SpawnEggItem.getType(context.stack()) == null) {
            return Optional.empty();
        }
        return Optional.of(ZenithTooltipEntityPreviewRenderer.prepare(context.stack(), preview, context.innerWidth()));
    }

    private static Optional<ZenithTooltipLayout.PreparedElement> prepareIcon(LayoutContext context, IconElement icon) {
        int boxSize = context.theme().iconHolder().boxSize();
        return Optional.of(new ZenithTooltipLayout.PreparedIcon(boxSize, boxSize + ZenithTooltipLayout.ICON_ELEMENT_BOTTOM_GAP));
    }

    private static int dividerOrnamentSize(ZenithTooltipTheme.DividerStyle style, int thickness) {
        return switch (style.decoration()) {
            case DIAMOND, DOUBLE_DIAMOND, CENTER_RUNE -> 5;
            case DOTTED, NONE -> thickness;
        };
    }

    private static Optional<ZenithTooltipLayout.PreparedElement> prepareTitleIcon(LayoutContext context, TitleIconElement titleIcon) {
        ZenithTooltipTheme.IconHolder holder = context.theme().iconHolder();
        int labelWidth = Math.max(1, context.innerWidth() - holder.boxSize() - holder.gap());
        List<FormattedCharSequence> titleLines = context.split(
                titleIcon.title().component().copy().withStyle(ChatFormatting.BOLD),
                labelWidth
        );
        List<FormattedCharSequence> subtitleLines = titleIcon.hasSubtitle()
                ? context.split(titleIcon.subtitle().component(), labelWidth)
                : List.of();
        int titlePadding = titleIcon.titleEffect()
                .map(effect -> ZenithTooltipTextAnimator.verticalPadding(effect, context.animationSettings()))
                .orElse(0);
        int subtitlePadding = titleIcon.subtitleEffect()
                .map(effect -> ZenithTooltipTextAnimator.verticalPadding(effect, context.animationSettings()))
                .orElse(0);
        int titleHeight = context.lineBlockHeight(titleLines.size(), ZenithTooltipLayout.ICON_LINE_GAP) + titlePadding * 2;
        int subtitleHeight = context.lineBlockHeight(subtitleLines.size(), ZenithTooltipLayout.ICON_LINE_GAP) + subtitlePadding * 2;
        int labelHeight = titleHeight + subtitleHeight;
        if (!titleLines.isEmpty() && !subtitleLines.isEmpty()) {
            labelHeight += ZenithTooltipLayout.ICON_LINE_GAP;
        }
        int height = Math.max(holder.boxSize(), labelHeight) + ZenithTooltipLayout.TITLE_ICON_BOTTOM_GAP;
        int textWidth = Math.max(context.maxLineWidth(titleLines), context.maxLineWidth(subtitleLines));
        int width = holder.boxSize() + holder.gap() + textWidth;
        return Optional.of(new ZenithTooltipLayout.PreparedTitleIcon(
                titleLines,
                subtitleLines,
                width,
                height,
                titleIcon.titleEffect(),
                titleIcon.subtitleEffect(),
                titlePadding,
                subtitlePadding,
                context.seed()
        ));
    }

    private record Entry<T extends ZenithTooltipElement>(
            Class<T> elementClass,
            ElementPreparer<T> preparer
    ) {
        private Optional<ZenithTooltipLayout.PreparedElement> prepare(LayoutContext context, ZenithTooltipElement element) {
            if (!elementClass.isInstance(element)) {
                return Optional.empty();
            }
            return preparer.prepare(context, elementClass.cast(element));
        }
    }
}
