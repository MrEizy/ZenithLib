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
    public static final Identifier NEBULA = id("nebula");
    public static final Identifier RUNIC = id("runic");
    public static final Identifier KINETIC = id("kinetic");

    private static final Map<Identifier, ZenithTooltipPreset> PRESETS = new LinkedHashMap<>();

    static {
        register(ZenithTooltipPreset.builder(CELESTIAL)
                .starField(true)
                .travellingBorderEnergy(true)
                .openingBloom(true)
                .titleShimmer(true)
                .iconFloat(true)
                .staggeredElements(true)
                .build());
        register(ZenithTooltipPreset.builder(CORRUPTED)
                .driftingMist(true)
                .scanlineBackground(true)
                .pulsingBorder(true)
                .titleShimmer(true)
                .pageWash(true)
                .dividerPulse(true)
                .build());
        register(ZenithTooltipPreset.builder(LIVING)
                .driftingMist(true)
                .moteField(true)
                .openingBloom(true)
                .iconFloat(true)
                .barPulse(true)
                .staggeredElements(true)
                .build());
        register(ZenithTooltipPreset.builder(MECHANICAL)
                .scanlineBackground(true)
                .frameAssembly(true)
                .pageSlide(true)
                .segmentedBars(true)
                .barScanline(true)
                .build());
        register(ZenithTooltipPreset.builder(NEBULA)
                .starField(true)
                .auroraBackground(true)
                .moteField(true)
                .titleShimmer(true)
                .iconOrbit(true)
                .build());
        register(ZenithTooltipPreset.builder(RUNIC)
                .pulsingBorder(true)
                .frameAssembly(true)
                .pageSlide(true)
                .dividerRunes(true)
                .titleShimmer(true)
                .build());
        register(ZenithTooltipPreset.builder(KINETIC)
                .travellingBorderEnergy(true)
                .pageSlide(true)
                .segmentedBars(true)
                .barEdgeSparks(true)
                .staggeredElements(true)
                .build());
    }

    private ZenithTooltipPresets() {}

    public static void register(ZenithTooltipPreset preset) {
        PRESETS.put(preset.id(), preset);
    }

    public static ZenithTooltipPreset compose(Identifier id, Identifier... parents) {
        ZenithTooltipPreset preset = ZenithTooltipPreset.builder(id).parents(parents).build();
        register(preset);
        return preset;
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
        resolved.auroraBackground |= preset.auroraBackground();
        resolved.moteField |= preset.moteField();
        resolved.scanlineBackground |= preset.scanlineBackground();
        resolved.travellingBorderEnergy |= preset.travellingBorderEnergy();
        resolved.pulsingBorder |= preset.pulsingBorder();
        resolved.cornerSparks |= preset.cornerSparks();
        resolved.twinBorderComets |= preset.twinBorderComets();
        resolved.frameAssembly |= preset.frameAssembly();
        resolved.openingBloom |= preset.openingBloom();
        resolved.pageSlide |= preset.pageSlide();
        resolved.pageWash |= preset.pageWash();
        resolved.titleShimmer |= preset.titleShimmer();
        resolved.iconFloat |= preset.iconFloat();
        resolved.iconOrbit |= preset.iconOrbit();
        resolved.segmentedBars |= preset.segmentedBars();
        resolved.barScanline |= preset.barScanline();
        resolved.barPulse |= preset.barPulse();
        resolved.barEdgeSparks |= preset.barEdgeSparks();
        resolved.dividerSweep |= preset.dividerSweep();
        resolved.dividerRunes |= preset.dividerRunes();
        resolved.dividerPulse |= preset.dividerPulse();
        resolved.staggeredElements |= preset.staggeredElements();
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(ZenithLib.MOD_ID, path);
    }

    public static final class Resolved {
        private boolean starField;
        private boolean driftingMist;
        private boolean auroraBackground;
        private boolean moteField;
        private boolean scanlineBackground;
        private boolean travellingBorderEnergy;
        private boolean pulsingBorder;
        private boolean cornerSparks;
        private boolean twinBorderComets;
        private boolean frameAssembly;
        private boolean openingBloom;
        private boolean pageSlide;
        private boolean pageWash;
        private boolean titleShimmer;
        private boolean iconFloat;
        private boolean iconOrbit;
        private boolean segmentedBars;
        private boolean barScanline;
        private boolean barPulse;
        private boolean barEdgeSparks;
        private boolean dividerSweep;
        private boolean dividerRunes;
        private boolean dividerPulse;
        private boolean staggeredElements;

        private Resolved() {}

        public boolean starField() { return starField; }
        public boolean driftingMist() { return driftingMist; }
        public boolean auroraBackground() { return auroraBackground; }
        public boolean moteField() { return moteField; }
        public boolean scanlineBackground() { return scanlineBackground; }
        public boolean travellingBorderEnergy() { return travellingBorderEnergy; }
        public boolean pulsingBorder() { return pulsingBorder; }
        public boolean cornerSparks() { return cornerSparks; }
        public boolean twinBorderComets() { return twinBorderComets; }
        public boolean frameAssembly() { return frameAssembly; }
        public boolean openingBloom() { return openingBloom; }
        public boolean pageSlide() { return pageSlide; }
        public boolean pageWash() { return pageWash; }
        public boolean titleShimmer() { return titleShimmer; }
        public boolean iconFloat() { return iconFloat; }
        public boolean iconOrbit() { return iconOrbit; }
        public boolean segmentedBars() { return segmentedBars; }
        public boolean barScanline() { return barScanline; }
        public boolean barPulse() { return barPulse; }
        public boolean barEdgeSparks() { return barEdgeSparks; }
        public boolean dividerSweep() { return dividerSweep; }
        public boolean dividerRunes() { return dividerRunes; }
        public boolean dividerPulse() { return dividerPulse; }
        public boolean staggeredElements() { return staggeredElements; }
        public boolean hasAmbient() {
            return starField || driftingMist || auroraBackground || moteField || scanlineBackground;
        }
        public boolean isEmpty() {
            return !(starField || driftingMist || auroraBackground || moteField || scanlineBackground
                    || travellingBorderEnergy || pulsingBorder || cornerSparks || twinBorderComets
                    || frameAssembly || openingBloom || pageSlide || pageWash || titleShimmer
                    || iconFloat || iconOrbit || segmentedBars || barScanline || barPulse || barEdgeSparks
                    || dividerSweep || dividerRunes || dividerPulse || staggeredElements);
        }
    }
}
