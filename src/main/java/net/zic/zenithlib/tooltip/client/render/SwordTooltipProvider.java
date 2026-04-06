package net.zic.zenithlib.tooltip.client.render;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.zic.zenithlib.tooltip.api.TooltipProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

        // SECTION 1: Custom Stats (page 1, after item name)
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

        // SECTION 2: Enchantments (page 2) — read live from the stack
        List<String> enchantLines = buildEnchantmentLines(stack);
        sections.add(new Section(
                "§b✧ Enchantments",
                enchantLines,
                11,
                true // new page
        ));


        // SECTION 4: Passive Ability (still page 2)
        sections.add(new Section(
                "§d❖ Diamond Edge",
                List.of(
                        "§7Attacks bypass §a20%§7 of enemy",
                        "§7armor. Critical hits deal §c+50%",
                        "§7bonus damage."
                ),
                15,
                true
        ));

        // SECTION 5: Lore (page 3)
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
                true // new page
        ));

        // SECTION 6: Rarity footer (page 3)
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

    /**
     * Reads enchantments from the stack and formats them as display lines.
     * Returns a "No enchantments" line if the stack has none.
     */
    private static List<String> buildEnchantmentLines(ItemStack stack) {
        List<String> lines = new ArrayList<>();

        var enchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack);
        if (enchantments.isEmpty()) {
            lines.add("§8No enchantments");
            return lines;
        }

        enchantments.keySet().forEach(enchHolder -> {
            int level = enchantments.getLevel(enchHolder);
            // getFullname() returns the formatted "Sharpness V" style Component
            Component name = enchHolder.value().getFullname(enchHolder, level);
            // Prefix with the standard enchantment cyan color §b
            lines.add("§b" + name.getString());
        });

        return lines;
    }

    @Override
    public int getPriority() {
        return 100;
    }
}