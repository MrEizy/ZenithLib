package net.zic.zenithlib.tooltip.api.animation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.zenithlib.ZenithLib;

import java.util.ArrayList;
import java.util.Arrays;
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

    private static final Map<Identifier, Preset> PRESETS = new LinkedHashMap<>();
    private static final Map<Identifier, Preset> DATA_DRIVEN_PRESETS = new LinkedHashMap<>();

    static {
        register(Preset.builder(CELESTIAL)
                .starField(true)
                .travellingBorderEnergy(true)
                .openingBloom(true)
                .titleShimmer(true)
                .iconFloat(true)
                .staggeredElements(true)
                .build());
        register(Preset.builder(CORRUPTED)
                .driftingMist(true)
                .scanlineBackground(true)
                .pulsingBorder(true)
                .titleShimmer(true)
                .pageWash(true)
                .dividerPulse(true)
                .build());
        register(Preset.builder(LIVING)
                .driftingMist(true)
                .moteField(true)
                .openingBloom(true)
                .iconFloat(true)
                .barPulse(true)
                .staggeredElements(true)
                .build());
        register(Preset.builder(MECHANICAL)
                .scanlineBackground(true)
                .frameAssembly(true)
                .pageSlide(true)
                .segmentedBars(true)
                .barScanline(true)
                .build());
        register(Preset.builder(NEBULA)
                .starField(true)
                .auroraBackground(true)
                .moteField(true)
                .titleShimmer(true)
                .iconOrbit(true)
                .build());
        register(Preset.builder(RUNIC)
                .pulsingBorder(true)
                .frameAssembly(true)
                .pageSlide(true)
                .dividerRunes(true)
                .titleShimmer(true)
                .build());
        register(Preset.builder(KINETIC)
                .travellingBorderEnergy(true)
                .pageSlide(true)
                .segmentedBars(true)
                .barEdgeSparks(true)
                .staggeredElements(true)
                .build());
    }

    private ZenithTooltipPresets() {}

    public static void register(Preset preset) {
        PRESETS.put(preset.id(), preset);
    }

    public static Preset compose(Identifier id, Identifier... parents) {
        Preset preset = Preset.builder(id).parents(parents).build();
        register(preset);
        return preset;
    }

    public static synchronized void replaceDataDriven(Map<Identifier, Data> presets) {
        for (Identifier id : DATA_DRIVEN_PRESETS.keySet()) {
            Preset existing = PRESETS.get(id);
            if (existing == DATA_DRIVEN_PRESETS.get(id)) {
                PRESETS.remove(id);
            }
        }

        DATA_DRIVEN_PRESETS.clear();

        for (Map.Entry<Identifier, Data> entry : presets.entrySet()) {
            Preset preset = entry.getValue().asPreset(entry.getKey());
            DATA_DRIVEN_PRESETS.put(entry.getKey(), preset);
            PRESETS.put(entry.getKey(), preset);
        }
    }

    public static Optional<Preset> get(Identifier id) {
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
        Preset preset = PRESETS.get(id);
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

    public record Data(
            List<Identifier> parents,
            List<String> effects
    ) {
        public static final Codec<Data> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Identifier.CODEC.listOf().optionalFieldOf("parents", List.of()).forGetter(Data::parents),
                        Codec.STRING.listOf().optionalFieldOf("effects", List.of()).forGetter(Data::effects)
                ).apply(instance, Data::new)
        );

        public Data {
            parents = parents == null ? List.of() : List.copyOf(parents);
            effects = effects == null ? List.of() : effects.stream()
                    .map(effect -> effect == null ? "" : effect.trim())
                    .filter(effect -> !effect.isBlank())
                    .toList();
        }

        public Preset asPreset(Identifier id) {
            Preset.Builder builder = Preset.builder(id).parents(parents);

            for (String effect : effects) {
                switch (effect) {
                    case "star_field" -> builder.starField(true);
                    case "drifting_mist" -> builder.driftingMist(true);
                    case "aurora_background" -> builder.auroraBackground(true);
                    case "mote_field" -> builder.moteField(true);
                    case "scanline_background" -> builder.scanlineBackground(true);
                    case "travelling_border_energy", "traveling_border_energy" -> builder.travellingBorderEnergy(true);
                    case "pulsing_border" -> builder.pulsingBorder(true);
                    case "corner_sparks" -> builder.cornerSparks(true);
                    case "twin_border_comets" -> builder.twinBorderComets(true);
                    case "frame_assembly" -> builder.frameAssembly(true);
                    case "opening_bloom" -> builder.openingBloom(true);
                    case "page_slide" -> builder.pageSlide(true);
                    case "page_wash" -> builder.pageWash(true);
                    case "title_shimmer" -> builder.titleShimmer(true);
                    case "icon_float" -> builder.iconFloat(true);
                    case "icon_orbit" -> builder.iconOrbit(true);
                    case "segmented_bars" -> builder.segmentedBars(true);
                    case "bar_scanline" -> builder.barScanline(true);
                    case "bar_pulse" -> builder.barPulse(true);
                    case "bar_edge_sparks" -> builder.barEdgeSparks(true);
                    case "divider_sweep" -> builder.dividerSweep(true);
                    case "divider_runes" -> builder.dividerRunes(true);
                    case "divider_pulse" -> builder.dividerPulse(true);
                    case "staggered_elements" -> builder.staggeredElements(true);
                    default -> {
                        // Unknown effect names are ignored so resource packs can stay forward-compatible.
                    }
                }
            }

            return builder.build();
        }
    }

    public record Preset(
            Identifier id,
            List<Identifier> parents,
            boolean starField,
            boolean driftingMist,
            boolean auroraBackground,
            boolean moteField,
            boolean scanlineBackground,
            boolean travellingBorderEnergy,
            boolean pulsingBorder,
            boolean cornerSparks,
            boolean twinBorderComets,
            boolean frameAssembly,
            boolean openingBloom,
            boolean pageSlide,
            boolean pageWash,
            boolean titleShimmer,
            boolean iconFloat,
            boolean iconOrbit,
            boolean segmentedBars,
            boolean barScanline,
            boolean barPulse,
            boolean barEdgeSparks,
            boolean dividerSweep,
            boolean dividerRunes,
            boolean dividerPulse,
            boolean staggeredElements
    ) {
        public Preset {
            parents = parents == null ? List.of() : List.copyOf(parents);
        }

        public static Builder builder(Identifier id) {
            return new Builder(id);
        }

        public Builder toBuilder(Identifier newId) {
            return new Builder(newId)
                    .parents(parents)
                    .starField(starField)
                    .driftingMist(driftingMist)
                    .auroraBackground(auroraBackground)
                    .moteField(moteField)
                    .scanlineBackground(scanlineBackground)
                    .travellingBorderEnergy(travellingBorderEnergy)
                    .pulsingBorder(pulsingBorder)
                    .cornerSparks(cornerSparks)
                    .twinBorderComets(twinBorderComets)
                    .frameAssembly(frameAssembly)
                    .openingBloom(openingBloom)
                    .pageSlide(pageSlide)
                    .pageWash(pageWash)
                    .titleShimmer(titleShimmer)
                    .iconFloat(iconFloat)
                    .iconOrbit(iconOrbit)
                    .segmentedBars(segmentedBars)
                    .barScanline(barScanline)
                    .barPulse(barPulse)
                    .barEdgeSparks(barEdgeSparks)
                    .dividerSweep(dividerSweep)
                    .dividerRunes(dividerRunes)
                    .dividerPulse(dividerPulse)
                    .staggeredElements(staggeredElements);
        }

        public static final class Builder {
            private final Identifier id;
            private List<Identifier> parents = List.of();
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

            private Builder(Identifier id) {
                this.id = id;
            }

            public Builder parents(List<Identifier> parents) {
                this.parents = parents == null ? List.of() : List.copyOf(parents);
                return this;
            }

            public Builder parent(Identifier parent) {
                this.parents = List.of(parent);
                return this;
            }

            public Builder parents(Identifier... parents) {
                this.parents = parents == null ? List.of() : Arrays.stream(parents).toList();
                return this;
            }

            public Builder addParent(Identifier parent) {
                ArrayList<Identifier> next = new ArrayList<>(this.parents);
                next.add(parent);
                this.parents = List.copyOf(next);
                return this;
            }

            public Builder starField(boolean value) { this.starField = value; return this; }
            public Builder driftingMist(boolean value) { this.driftingMist = value; return this; }
            public Builder auroraBackground(boolean value) { this.auroraBackground = value; return this; }
            public Builder moteField(boolean value) { this.moteField = value; return this; }
            public Builder scanlineBackground(boolean value) { this.scanlineBackground = value; return this; }
            public Builder travellingBorderEnergy(boolean value) { this.travellingBorderEnergy = value; return this; }
            public Builder pulsingBorder(boolean value) { this.pulsingBorder = value; return this; }
            public Builder cornerSparks(boolean value) { this.cornerSparks = value; return this; }
            public Builder twinBorderComets(boolean value) { this.twinBorderComets = value; return this; }
            public Builder frameAssembly(boolean value) { this.frameAssembly = value; return this; }
            public Builder openingBloom(boolean value) { this.openingBloom = value; return this; }
            public Builder pageSlide(boolean value) { this.pageSlide = value; return this; }
            public Builder pageWash(boolean value) { this.pageWash = value; return this; }
            public Builder titleShimmer(boolean value) { this.titleShimmer = value; return this; }
            public Builder iconFloat(boolean value) { this.iconFloat = value; return this; }
            public Builder iconOrbit(boolean value) { this.iconOrbit = value; return this; }
            public Builder segmentedBars(boolean value) { this.segmentedBars = value; return this; }
            public Builder barScanline(boolean value) { this.barScanline = value; return this; }
            public Builder barPulse(boolean value) { this.barPulse = value; return this; }
            public Builder barEdgeSparks(boolean value) { this.barEdgeSparks = value; return this; }
            public Builder dividerSweep(boolean value) { this.dividerSweep = value; return this; }
            public Builder dividerRunes(boolean value) { this.dividerRunes = value; return this; }
            public Builder dividerPulse(boolean value) { this.dividerPulse = value; return this; }
            public Builder staggeredElements(boolean value) { this.staggeredElements = value; return this; }

            public Preset build() {
                return new Preset(
                        id,
                        parents,
                        starField,
                        driftingMist,
                        auroraBackground,
                        moteField,
                        scanlineBackground,
                        travellingBorderEnergy,
                        pulsingBorder,
                        cornerSparks,
                        twinBorderComets,
                        frameAssembly,
                        openingBloom,
                        pageSlide,
                        pageWash,
                        titleShimmer,
                        iconFloat,
                        iconOrbit,
                        segmentedBars,
                        barScanline,
                        barPulse,
                        barEdgeSparks,
                        dividerSweep,
                        dividerRunes,
                        dividerPulse,
                        staggeredElements
                );
            }
        }
    }
}
