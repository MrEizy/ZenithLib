package net.zic.zenithlib.creative.api;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Public registration and lookup API for creative-tab sections. */
public final class CreativeTabSections {
    private static final Map<ResourceKey<CreativeModeTab>, LinkedHashMap<Identifier, CreativeTabSection>> SECTIONS =
            new LinkedHashMap<>();

    private CreativeTabSections() {}

    public static synchronized CreativeTabSection register(
            ResourceKey<@NotNull CreativeModeTab> tab,
            CreativeTabSection section
    ) {
        Objects.requireNonNull(tab, "tab");
        Objects.requireNonNull(section, "section");

        LinkedHashMap<Identifier, CreativeTabSection> tabSections =
                SECTIONS.computeIfAbsent(tab, ignored -> new LinkedHashMap<>());
        CreativeTabSection previous = tabSections.putIfAbsent(section.id(), section);
        if (previous != null) {
            throw new IllegalStateException(
                    "Duplicate creative section " + section.id() + " for tab " + tab.identifier()
            );
        }

        return section;
    }

    public static synchronized void register(
            ResourceKey<CreativeModeTab> tab,
            CreativeTabSection... sections
    ) {
        Objects.requireNonNull(sections, "sections");
        for (CreativeTabSection section : sections) {
            register(tab, section);
        }
    }

    public static synchronized List<CreativeTabSection> get(ResourceKey<CreativeModeTab> tab) {
        Map<Identifier, CreativeTabSection> registered = SECTIONS.get(tab);
        if (registered == null || registered.isEmpty()) {
            return List.of();
        }

        List<CreativeTabSection> result = new ArrayList<>(registered.size());
        for (CreativeTabSection section : registered.values()) {
            if (section.isVisible()) {
                result.add(section);
            }
        }

        result.sort(Comparator.comparingInt(CreativeTabSection::order));
        return List.copyOf(result);
    }
}
