package net.zic.zenithlib.tooltip.client.render;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.zic.zenithlib.tooltip.api.TooltipProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Custom tooltip provider for Diamond Sword with 3 pages of content.
 * Replaces vanilla attack damage/speed with custom formatted stats.
 */
public class SwordTooltipProvider implements TooltipProvider {

    public static final SwordTooltipProvider INSTANCE = new SwordTooltipProvider();

    private SwordTooltipProvider() {}

    @Override
    public boolean canProvideFor(ItemStack stack) {
        return stack.is(Items.DIAMOND_SWORD);
    }

    @Override
    public Optional<String> getThemeKey(ItemStack stack) {
        return Optional.of("enigma");
    }

    @Override
    public List<Section> getSections(ItemStack stack) {
        List<Section> sections = new ArrayList<>();

        // SECTION 1: Custom Stats (replaces vanilla "When in Main Hand")
        // These will appear on page 1 after the item name
        sections.add(new Section(
                "§6⚔ Combat Statistics",
                List.of(
                        "§7Base Damage: §c+7 Attack Damage",
                        "§7Attack Speed: §e1.6",
                        "§7Reach: §a3.0 blocks",
                        "",
                        "§8◆ Diamond Edge §7- Enhanced sharpness",
                        "§8  grants bonus critical damage"
                ),
                10,
                false
        ));

        // SECTION 2: Active Ability (Page 2)
        sections.add(new Section(
                "§b✦ Galeforce",
                List.of(
                        "§7While sprinting, release to lunge",
                        "§7forward and deal §c+3§7 bonus",
                        "§7damage on impact.",
                        "",
                        "§6Cooldown: §e8 seconds",
                        "§6Mana Cost: §915"
                ),
                20,
                true // New page
        ));

        // SECTION 3: Passive Ability (still Page 2)
        sections.add(new Section(
                "§d❖ Diamond Edge",
                List.of(
                        "§7Attacks bypass §a20%§7 of enemy",
                        "§7armor. Critical hits deal §c+50%",
                        "§7bonus damage."
                ),
                15,
                false
        ));

        // SECTION 4: Lore (Page 3)
        sections.add(new Section(
                "§5✦ Ancient Legend",
                List.of(
                        "\"Forged in the depths where",
                        "pressure turns coal into",
                        "eternal brilliance.\"",
                        "",
                        "§8Only the worthy may wield",
                        "§8its unbreaking light."
                ),
                30,
                true // New page
        ));

        // SECTION 5: Rarity footer (Page 3)
        sections.add(new Section(
                "",
                List.of(
                        "",
                        "§6◆ §eRare Quality §6◆",
                        "§8Item Level: §725"
                ),
                5,
                false
        ));

        return sections;
    }

    @Override
    public int getPriority() {
        // High priority to override other providers for diamond swords
        return 100;
    }

    /**
     * Filters vanilla tooltip lines to remove unwanted stats.
     * Call this from your renderer to clean up the vanilla tooltip.
     */
    private static List<Component> filterVanillaLines(List<Component> lines, TooltipProvider provider, ItemStack stack) {
        // Check if this provider is the sword provider by checking if it handles diamond swords
        // This is more reliable than instanceof check
        boolean isSwordProvider = provider.getClass().getSimpleName().equals("SwordTooltipProvider") ||
                (provider.canProvideFor(new ItemStack(Items.DIAMOND_SWORD)) &&
                        provider.getPriority() == 100);

        if (!isSwordProvider) {
            return new ArrayList<>(lines);
        }

        List<Component> filtered = new ArrayList<>();
        boolean skipStatsSection = false;

        for (Component line : lines) {
            String text = line.getString();

            // Always keep the title (first line)
            if (filtered.isEmpty()) {
                filtered.add(line);
                continue;
            }

            // Detect start of "When in Main Hand:" section
            if (text.contains("When in Main Hand:")) {
                skipStatsSection = true;
                continue;
            }

            // Skip lines while in stats section
            if (skipStatsSection) {
                // More aggressive filtering - skip anything that looks like a stat
                boolean isStatLine = text.contains("Attack Damage") ||
                        text.contains("Attack Speed") ||
                        text.matches(".*\\d+\\.?\\d*\\s*(Attack|Damage|Speed).*") || // Matches "7 Attack Damage" etc
                        text.trim().isEmpty() ||
                        text.startsWith(" "); // Most vanilla stat lines are indented

                if (isStatLine) {
                    continue;
                } else {
                    skipStatsSection = false;
                }
            }

            // Keep all other lines
            filtered.add(line);
        }

        return filtered;
    }
}