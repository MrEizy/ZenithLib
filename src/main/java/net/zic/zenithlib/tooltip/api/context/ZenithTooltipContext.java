package net.zic.zenithlib.tooltip.api.context;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable runtime context used by contextual document providers and tooltip value
 * sources.
 */
public final class ZenithTooltipContext {
    private final ItemStack stack;
    private final Identifier itemId;
    private final Optional<RegistryAccess> registryAccess;
    private final Optional<Player> player;
    private final Optional<SubjectReference> subject;
    private final Map<Identifier, Object> data;

    private ZenithTooltipContext(
            ItemStack stack,
            Identifier itemId,
            Optional<RegistryAccess> registryAccess,
            Optional<Player> player,
            Optional<SubjectReference> subject,
            Map<Identifier, Object> data
    ) {
        this.stack = Objects.requireNonNull(stack, "stack");
        this.itemId = Objects.requireNonNull(itemId, "itemId");
        this.registryAccess = registryAccess == null ? Optional.empty() : registryAccess;
        this.player = player == null ? Optional.empty() : player;
        this.subject = subject == null ? Optional.empty() : subject;
        this.data = data == null ? Map.of() : Map.copyOf(data);
    }

    public static ZenithTooltipContext of(
            ItemStack stack,
            Identifier itemId,
            Optional<RegistryAccess> registryAccess
    ) {
        return new ZenithTooltipContext(stack, itemId, registryAccess, Optional.empty(), Optional.empty(), Map.of());
    }

    public static ZenithTooltipContext of(
            ItemStack stack,
            Identifier itemId,
            Optional<RegistryAccess> registryAccess,
            Optional<Player> player
    ) {
        return new ZenithTooltipContext(stack, itemId, registryAccess, player, Optional.empty(), Map.of());
    }

    public ItemStack stack() {
        return stack;
    }

    public Identifier itemId() {
        return itemId;
    }

    public Optional<RegistryAccess> registryAccess() {
        return registryAccess;
    }

    public Optional<Player> player() {
        return player;
    }

    public Optional<SubjectReference> subject() {
        return subject;
    }

    public ZenithTooltipContext withSubject(
            Identifier subjectId,
            Object value,
            ZenithTooltipSubject presentation
    ) {
        return withSubject(Optional.of(Objects.requireNonNull(subjectId, "subjectId")), value, presentation);
    }

    public ZenithTooltipContext withSubject(Object value, ZenithTooltipSubject presentation) {
        return withSubject(Optional.empty(), value, presentation);
    }

    public ZenithTooltipContext withSubject(Identifier subjectId, ZenithTooltipSubject subject) {
        return withSubject(subjectId, subject, subject);
    }

    public ZenithTooltipContext withSubject(ZenithTooltipSubject subject) {
        return withSubject(subject, subject);
    }

    private ZenithTooltipContext withSubject(
            Optional<Identifier> subjectId,
            Object value,
            ZenithTooltipSubject presentation
    ) {
        SubjectReference reference = new SubjectReference(subjectId, value, presentation);
        return new ZenithTooltipContext(stack, itemId, registryAccess, player, Optional.of(reference), data);
    }

    public ZenithTooltipContext withData(Identifier key, Object value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        java.util.LinkedHashMap<Identifier, Object> next = new java.util.LinkedHashMap<>(data);
        next.put(key, value);
        return new ZenithTooltipContext(stack, itemId, registryAccess, player, subject, next);
    }

    public Optional<Object> data(Identifier key) {
        Objects.requireNonNull(key, "key");
        return Optional.ofNullable(data.get(key));
    }

    public <T> Optional<T> data(Identifier key, Class<T> type) {
        Objects.requireNonNull(type, "type");
        return data(key).filter(type::isInstance).map(type::cast);
    }

    public Map<Identifier, Object> dataView() {
        return data;
    }

    public Optional<Identifier> subjectId() {
        return subject.flatMap(SubjectReference::id);
    }

    public Optional<ZenithTooltipSubject> subjectPresentation() {
        return subject.map(SubjectReference::presentation);
    }

    public Optional<Object> subjectValue() {
        return subject.map(SubjectReference::value);
    }

    public <T> Optional<T> subject(Class<T> type) {
        Objects.requireNonNull(type, "type");
        return subjectValue().filter(type::isInstance).map(type::cast);
    }

    public record SubjectReference(
            Optional<Identifier> id,
            Object value,
            ZenithTooltipSubject presentation
    ) {
        public SubjectReference {
            id = id == null ? Optional.empty() : id;
            Objects.requireNonNull(value, "value");
            Objects.requireNonNull(presentation, "presentation");
        }
    }
}
