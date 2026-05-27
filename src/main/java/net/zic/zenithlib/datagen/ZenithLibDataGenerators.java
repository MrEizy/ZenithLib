package net.zic.zenithlib.datagen;

import net.neoforged.neoforge.data.event.GatherDataEvent;

public final class ZenithLibDataGenerators {
    private ZenithLibDataGenerators() {}

    public static void gatherData(GatherDataEvent.Client event) {

        event.createProvider(ZenithLibTooltipDataProvider::new);
        event.createProvider(ZenithLanguageProvider::new);

    }
}
