package net.zic.zenithlib.datagen;

import net.neoforged.neoforge.data.event.GatherDataEvent;

public final class ZenithLibDataGenerators {
    private ZenithLibDataGenerators() {}

    public static void gatherData(GatherDataEvent.Client event) {

        // Tooltip Provider for Zenithlib, other mods that use Zenithlib can extend ZenithTooltipDataProvider
        event.createProvider(ZenithLibTooltipDataProvider::new);

    }
}
