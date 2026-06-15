package net.zic.zenithlib.datagen;

import net.minecraft.data.PackOutput;
import net.zic.zenithlib.ZenithLib;
import net.zic.zenithlib.classification.datagen.ZenithClassificationDataProvider;
import net.zic.zenithlib.tooltip.api.ZenithTooltipColor;

/** Generates the classifications. */
public final class ZenithLibClassificationDataProvider extends ZenithClassificationDataProvider {
    public ZenithLibClassificationDataProvider(PackOutput output) {
        super(output, ZenithLib.MOD_ID);
    }

    @Override
    protected void addClassifications() {
        category("crafting_ingredient")
                .label("classification.zenithlib.category.crafting_ingredient")
                .color(ZenithTooltipColor.ACCENT);

        rank("rare")
                .label("classification.zenithlib.rank.rare")
                .color(ZenithTooltipColor.POSITIVE);

        classification("diamond")
                .priority(100)
                .items(minecraft("diamond"))
                .category(id("crafting_ingredient"))
                .rank(id("rare"));
    }
}
