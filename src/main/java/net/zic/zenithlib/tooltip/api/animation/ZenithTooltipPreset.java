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
    public ZenithTooltipPreset {
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
                .travellingBorderEnergy(travellingBorderEnergy)
                .frameAssembly(frameAssembly)
                .titleShimmer(titleShimmer)
                .iconFloat(iconFloat)
                .iconOrbit(iconOrbit)
                .segmentedBars(segmentedBars)
                .barEdgeSparks(barEdgeSparks)
                .dividerSweep(dividerSweep)
                .staggeredElements(staggeredElements);
    }

    public static final class Builder {
        private final Identifier id;
        private List<Identifier> parents = List.of();
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
        public Builder travellingBorderEnergy(boolean value) { this.travellingBorderEnergy = value; return this; }
        public Builder frameAssembly(boolean value) { this.frameAssembly = value; return this; }
        public Builder titleShimmer(boolean value) { this.titleShimmer = value; return this; }
        public Builder iconFloat(boolean value) { this.iconFloat = value; return this; }
        public Builder iconOrbit(boolean value) { this.iconOrbit = value; return this; }
        public Builder segmentedBars(boolean value) { this.segmentedBars = value; return this; }
        public Builder barEdgeSparks(boolean value) { this.barEdgeSparks = value; return this; }
        public Builder dividerSweep(boolean value) { this.dividerSweep = value; return this; }
        public Builder staggeredElements(boolean value) { this.staggeredElements = value; return this; }

        public ZenithTooltipPreset build() {
            return new ZenithTooltipPreset(
                    id,
                    parents,
                    starField,
                    driftingMist,
                    travellingBorderEnergy,
                    frameAssembly,
                    titleShimmer,
                    iconFloat,
                    iconOrbit,
                    segmentedBars,
                    barEdgeSparks,
                    dividerSweep,
                    staggeredElements
            );
        }
    }
}
