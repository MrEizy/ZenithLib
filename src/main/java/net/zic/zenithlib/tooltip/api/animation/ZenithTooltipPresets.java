package net.zic.zenithlib.tooltip.api.animation;

import net.minecraft.resources.Identifier;
import net.zic.zenithlib.ZenithLib;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Global registry for semantic tooltip animation presets. */
public final class ZenithTooltipPresets {
    public static final Identifier CELESTIAL = id("celestial");
    public static final Identifier CORRUPTED = id("corrupted");
    public static final Identifier LIVING = id("living");
    public static final Identifier MECHANICAL = id("mechanical");

    private static final Map<Identifier, ZenithTooltipPreset> PRESETS = new LinkedHashMap<>();

    static {
        register(ZenithTooltipPreset.builder(CELESTIAL)
                .starField(true)
                .travellingBorderEnergy(true)
                .frameAssembly(true)
                .titleShimmer(true)
                .iconFloat(true)
                .dividerSweep(true)
                .staggeredElements(true)
                .build());
        register(ZenithTooltipPreset.builder(CORRUPTED)
                .driftingMist(true)
                .travellingBorderEnergy(true)
                .frameAssembly(true)
                .titleShimmer(true)
                .barEdgeSparks(true)
                .dividerSweep(true)
                .build());
        register(ZenithTooltipPreset.builder(LIVING)
                .driftingMist(true)
                .iconFloat(true)
                .barEdgeSparks(true)
                .staggeredElements(true)
                .build());
        register(ZenithTooltipPreset.builder(MECHANICAL)
                .travellingBorderEnergy(true)
                .segmentedBars(true)
                .dividerSweep(true)
                .frameAssembly(true)
                .build());
    }

    private ZenithTooltipPresets() {}

    public static void register(ZenithTooltipPreset preset) {
        PRESETS.put(preset.id(), preset);
    }

    public static Optional<ZenithTooltipPreset> get(Identifier id) {
        return Optional.ofNullable(PRESETS.get(id));
    }

    public static Resolved resolve(List<Identifier> ids) {
        Resolved resolved = new Resolved();
        for (Identifier id : ids) {
            apply(id, resolved, 0);
        }
        return resolved;
    }

    private static void apply(Identifier id, Resolved resolved, int depth) {
        if (depth > 8) {
            return;
        }
        ZenithTooltipPreset preset = PRESETS.get(id);
        if (preset == null) {
            return;
        }
        for (Identifier parent : preset.parents()) {
            apply(parent, resolved, depth + 1);
        }
        resolved.starField |= preset.starField();
        resolved.driftingMist |= preset.driftingMist();
        resolved.travellingBorderEnergy |= preset.travellingBorderEnergy();
        resolved.frameAssembly |= preset.frameAssembly();
        resolved.titleShimmer |= preset.titleShimmer();
        resolved.iconFloat |= preset.iconFloat();
        resolved.iconOrbit |= preset.iconOrbit();
        resolved.segmentedBars |= preset.segmentedBars();
        resolved.barEdgeSparks |= preset.barEdgeSparks();
        resolved.dividerSweep |= preset.dividerSweep();
        resolved.staggeredElements |= preset.staggeredElements();
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(ZenithLib.MOD_ID, path);
    }

    public static final class Resolved {
        private boolean starField;
        private boolean driftingMist;
        private boolean travellingBorderEnergy;
        private boolean frameAssembly;
        private boolean titleShimmer;
        private boolean iconFloat;
        private boolean iconOrbit;
        private boolean segmentedBars;
        private boolean barEdgeSparks;
        private boolean dividerSweep;
        private boolean staggeredElements;

        private Resolved() {}

        public boolean starField() {
            return starField;
        }
        public boolean driftingMist() {
            return driftingMist;
        }
        public boolean travellingBorderEnergy() {
            return travellingBorderEnergy;
        }
        public boolean frameAssembly() {
            return frameAssembly;
        }
        public boolean titleShimmer() {
            return titleShimmer;
        }
        public boolean iconFloat() {
            return iconFloat;
        }
        public boolean iconOrbit() {
            return iconOrbit;
        }
        public boolean segmentedBars() {
            return segmentedBars;
        }
        public boolean barEdgeSparks() {
            return barEdgeSparks;
        }
        public boolean dividerSweep() {
            return dividerSweep;
        }
        public boolean staggeredElements() {
            return staggeredElements;
        }
        public boolean hasAmbient() {
            return starField || driftingMist;
        }
        public boolean isEmpty() {
            return !(starField || driftingMist || travellingBorderEnergy || frameAssembly || titleShimmer
                    || iconFloat || iconOrbit || segmentedBars || barEdgeSparks || dividerSweep || staggeredElements);
        }
    }
}
