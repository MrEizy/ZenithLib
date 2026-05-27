package net.zic.zenithlib.tooltip.manager;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.zic.zenithlib.tooltip.api.ZenithTooltipColor;
import net.zic.zenithlib.tooltip.api.ZenithTooltipDocument;
import net.zic.zenithlib.tooltip.api.ZenithTooltipPage;
import net.zic.zenithlib.tooltip.api.ZenithTooltipText;
import net.zic.zenithlib.tooltip.api.ZenithTooltipTheme;
import net.zic.zenithlib.tooltip.api.element.BarElement;
import net.zic.zenithlib.tooltip.api.element.DividerElement;
import net.zic.zenithlib.tooltip.api.element.HeaderElement;
import net.zic.zenithlib.tooltip.api.element.TextElement;
import net.zic.zenithlib.tooltip.api.element.TitleIconElement;
import net.zic.zenithlib.tooltip.api.element.ZenithTooltipElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Converts an item's ordinary Minecraft tooltip data into the Zenith document model
 * when no explicit data-driven rule matches that item.
 *
 * <p>The converter builds an overview page from the item name and remaining vanilla
 * lines, adds a live durability bar for damageable stacks, and creates an additional
 * enchantment page when enchantments are present. Attribute lines are preserved from
 * Minecraft's normal tooltip so effective equipment values and modded calculations are
 * not replaced with raw modifier amounts. Generated
 * content uses the default mana-blue theme and semantic colours so fallback tooltips
 * retain the same visual language as configured documents.</p>
 *
 * <p>Its line classification is intentionally best-effort because vanilla or modded
 * tooltip strings may vary; authored JSON documents remain the precise route for
 * custom item presentation.</p>
 */


public final class ZenithVanillaTooltipConverter {
    private static final ZenithTooltipColor ENCHANT_COLOR = ZenithTooltipColor.ACCENT;
    private static final ZenithTooltipColor STAT_COLOR = ZenithTooltipColor.WARNING;
    private static final ZenithTooltipColor DETAIL_COLOR = ZenithTooltipColor.MUTED;

    private ZenithVanillaTooltipConverter() {}

    public static ZenithTooltipDocument convert(ItemStack stack, List<FormattedText> vanillaLines) {
        String itemName = stack.getHoverName().getString();
        ClassifiedLines lines = classifyLines(itemName, stack, vanillaLines);

        List<ZenithTooltipPage> pages = new ArrayList<>();
        pages.add(buildOverviewPage(itemName, stack, lines));

        if (!lines.enchantments().isEmpty()) {
            pages.add(buildEnchantmentsPage(lines.enchantments()));
        }

        return new ZenithTooltipDocument(ZenithTooltipTheme.defaultTheme(), pages);
    }

    private static ClassifiedLines classifyLines(String itemName, ItemStack stack, List<FormattedText> vanillaLines) {
        List<String> overview = new ArrayList<>();
        List<String> enchantments = new ArrayList<>(extractEnchantments(stack));
        List<String> stats = new ArrayList<>();
        List<String> details = new ArrayList<>();

        boolean hasDirectEnchantments = !enchantments.isEmpty();

        for (FormattedText vanillaLine : vanillaLines) {
            String text = vanillaLine.getString();

            if (text.isBlank() || text.equals(itemName)) {
                continue;
            }

            switch (classify(text)) {
                case ENCHANTMENT -> {
                    if (!hasDirectEnchantments) {
                        enchantments.add(text);
                    }
                }
                case STAT -> addUnique(stats, text);
                case DETAIL -> {
                    if (!isEquipmentSectionHeading(text) && !text.contains("Durability")) {
                        addUnique(details, text);
                    }
                }
                case OVERVIEW -> overview.add(text);
            }
        }

        return new ClassifiedLines(overview, enchantments, stats, details);
    }

    private static List<String> extractEnchantments(ItemStack stack) {
        ItemEnchantments enchantments = stack.get(DataComponents.ENCHANTMENTS);

        if (enchantments == null || enchantments.isEmpty()) {
            return List.of();
        }

        List<String> lines = new ArrayList<>();

        for (Map.Entry<Holder<Enchantment>, Integer> entry : enchantments.entrySet()) {
            Component name = Enchantment.getFullname(entry.getKey(), entry.getValue());
            lines.add(name.getString());
        }

        return lines;
    }

    private static boolean hasDurability(ItemStack stack) {
        Integer maxDamage = stack.get(DataComponents.MAX_DAMAGE);
        return maxDamage != null && maxDamage > 0;
    }

    private static void addUnique(List<String> lines, String text) {
        if (!lines.contains(text)) {
            lines.add(text);
        }
    }

    private static boolean isEquipmentSectionHeading(String text) {
        return text.startsWith("When ") && text.endsWith(":");
    }

    private static LineKind classify(String text) {
        if (isLikelyEnchantmentLine(text)) {
            return LineKind.ENCHANTMENT;
        }

        if (isLikelyStatLine(text)) {
            return LineKind.STAT;
        }

        if (isLikelyDetailLine(text)) {
            return LineKind.DETAIL;
        }

        return LineKind.OVERVIEW;
    }

    private static boolean isLikelyEnchantmentLine(String text) {
        return text.matches(".*\\b(I|II|III|IV|V|VI|VII|VIII|IX|X)\\b.*")
                && !isLikelyStatLine(text)
                && !text.startsWith("When ");
    }

    private static boolean isLikelyStatLine(String text) {
        return text.contains("Attack Damage")
                || text.contains("Attack Speed")
                || text.contains("Armor")
                || text.contains("Armor Toughness")
                || text.contains("Knockback Resistance")
                || text.contains("Mining Speed")
                || text.contains("Mining Efficiency");
    }

    private static boolean isLikelyDetailLine(String text) {
        return text.startsWith("When ")
                || text.contains("Durability")
                || text.contains("NBT")
                || text.contains("components");
    }

    private static ZenithTooltipPage buildOverviewPage(String itemName, ItemStack stack, ClassifiedLines lines) {
        List<ZenithTooltipElement> elements = new ArrayList<>();

        elements.add(new TitleIconElement(
                ZenithTooltipText.literal(itemName),
                ZenithTooltipText.translatable("tooltip.zenithlib.vanilla.converted")
        ));
        appendTextSection(elements, lines.overview(), ZenithTooltipColor.TEXT, false, "");
        appendStatsSection(elements, stack, lines.stats());
        appendTextSection(elements, lines.details(), DETAIL_COLOR, true, "tooltip.zenithlib.vanilla.details");

        return new ZenithTooltipPage(ZenithTooltipText.literal(itemName), elements);
    }

    private static ZenithTooltipPage buildEnchantmentsPage(List<String> enchantments) {
        List<ZenithTooltipElement> elements = new ArrayList<>();

        elements.add(new DividerElement());

        for (String line : enchantments) {
            elements.add(TextElement.literal(line, ENCHANT_COLOR));
        }

        return new ZenithTooltipPage(ZenithTooltipText.translatable("tooltip.zenithlib.vanilla.enchantments"), elements);
    }

    private static void appendStatsSection(
            List<ZenithTooltipElement> elements,
            ItemStack stack,
            List<String> stats
    ) {
        if (stats.isEmpty() && !hasDurability(stack)) {
            return;
        }

        elements.add(new DividerElement());
        elements.add(new HeaderElement("tooltip.zenithlib.vanilla.stats", STAT_COLOR));

        for (String line : stats) {
            elements.add(TextElement.literal(line, STAT_COLOR));
        }

        if (hasDurability(stack)) {
            elements.add(BarElement.dynamic(
                    ZenithTooltipText.translatable("tooltip.zenithlib.vanilla.durability"),
                    "durability",
                    ZenithTooltipColor.POSITIVE
            ));
        }
    }

    private static void appendTextSection(
            List<ZenithTooltipElement> elements,
            List<String> lines,
            ZenithTooltipColor color,
            boolean includeHeader,
            String headerKey
    ) {
        if (lines.isEmpty()) {
            return;
        }

        elements.add(new DividerElement());

        if (includeHeader) {
            elements.add(new HeaderElement(headerKey, color));
        }

        for (String line : lines) {
            elements.add(TextElement.literal(line, color));
        }
    }

    private enum LineKind {
        OVERVIEW,
        ENCHANTMENT,
        STAT,
        DETAIL
    }

    private record ClassifiedLines(
            List<String> overview,
            List<String> enchantments,
            List<String> stats,
            List<String> details
    ) {}
}
