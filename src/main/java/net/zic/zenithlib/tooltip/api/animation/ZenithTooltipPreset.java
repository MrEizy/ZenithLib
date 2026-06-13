package net.zic.zenithlib.tooltip.api.animation;

import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * Semantic bundle of reusable tooltip animations and procedural effects.
 */
public record ZenithTooltipPreset(
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
    public ZenithTooltipPreset {
        parents = parents == null ? List.of() : List.copyOf(parents);
    }

    public ZenithTooltipPreset(
            Identifier id,
            List<Identifier> parents,
            boolean starField,
            boolean driftingMist,
            boolean travellingBorderEnergy,
            boolean frameAssembly,
            boolean titleShimmer,
            boolean iconFloat,
            boolean iconOrbit,
            boolean segmentedBars,
            boolean barEdgeSparks,
            boolean dividerSweep,
            boolean staggeredElements
    ) {
        this(
                id,
                parents,
                starField,
                driftingMist,
                false,
                false,
                false,
                travellingBorderEnergy,
                false,
                false,
                false,
                frameAssembly,
                false,
                false,
                false,
                titleShimmer,
                iconFloat,
                iconOrbit,
                segmentedBars,
                false,
                false,
                barEdgeSparks,
                dividerSweep,
                false,
                false,
                staggeredElements
        );
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

        public ZenithTooltipPreset build() {
            return new ZenithTooltipPreset(
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
