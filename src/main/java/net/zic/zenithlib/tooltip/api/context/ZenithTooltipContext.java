package net.zic.zenithlib.tooltip.api.context;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.Optional;

/**
 * Immutable runtime context used by contextual document providers and tooltip value
 * sources.
 *
 * <p>A context always describes the hovered stack and may additionally expose client
 * registry access, the viewing player, and a registry-backed or otherwise meaningful
 * subject. The subject reference keeps the original typed object beside a generic
 * presentation adapter, allowing ZenithLib and dependent mods to consume the same
 * context at different levels of specificity.</p>
 */
public final class ZenithTooltipContext {
    private final ItemStack stack;
    private final Identifier itemId;
    private final Optional<RegistryAccess> registryAccess;
    private final Optional<Player> player;
    private final Optional<SubjectReference> subject;

    private ZenithTooltipContext(
            ItemStack stack,
            Identifier itemId,
            Optional<RegistryAccess> registryAccess,
            Optional<Player> player,
            Optional<SubjectReference> subject
    ) {
        this.stack = Objects.requireNonNull(stack, "stack");
        this.itemId = Objects.requireNonNull(itemId, "itemId");
        this.registryAccess = registryAccess == null ? Optional.empty() : registryAccess;
        this.player = player == null ? Optional.empty() : player;
        this.subject = subject == null ? Optional.empty() : subject;
    }

    public static ZenithTooltipContext of(
            ItemStack stack,
            Identifier itemId,
            Optional<RegistryAccess> registryAccess
    ) {
        return new ZenithTooltipContext(stack, itemId, registryAccess, Optional.empty(), Optional.empty());
    }

    public static ZenithTooltipContext of(
            ItemStack stack,
            Identifier itemId,
            Optional<RegistryAccess> registryAccess,
            Optional<Player> player
    ) {
        return new ZenithTooltipContext(stack, itemId, registryAccess, player, Optional.empty());
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
        return new ZenithTooltipContext(stack, itemId, registryAccess, player, Optional.of(reference));
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
