package net.zic.zenithlib.datagen;

import net.neoforged.neoforge.data.event.GatherDataEvent;

public final class ZenithLibDataGenerators {
    private ZenithLibDataGenerators() {}

    public static void gatherData(GatherDataEvent.Client event) {

        // TOOLTIPS - THE SHOWCASE ONES WILL BE REMOVED AFTER I MAKE A GUIDE FOR THE TOOLTIPS
        event.createProvider(ZenithLibTooltipDataProvider::new);
        event.createProvider(ZenithLibTooltipThemeDataProvider::new);


        event.createProvider(ZenithLanguageProvider::new);

    }
}
