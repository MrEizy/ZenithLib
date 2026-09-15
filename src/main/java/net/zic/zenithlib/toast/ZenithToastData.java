package net.zic.zenithlib.toast;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.zic.zenithlib.ZenithLib;

import java.util.Objects;
import java.util.Optional;

/** Network description of a ZenithLib toast */
public record ZenithToastData(
        Component title,
        Component message,
        ItemStack icon,
        Identifier background,
        int durationMs,
        int titleColor,
        int messageColor,
        Optional<SoundEvent> sound
) {
    public static final Identifier DEFAULT_BACKGROUND =
            Identifier.fromNamespaceAndPath(ZenithLib.MOD_ID, "toast/default");

    public static final int DEFAULT_DURATION_MS = 5000;
    public static final int DEFAULT_TITLE_COLOR = 0xFFFFFFFF;
    public static final int DEFAULT_MESSAGE_COLOR = 0xFFB8B8B8;

    private static final StreamCodec<RegistryFriendlyByteBuf, Optional<SoundEvent>> SOUND_STREAM_CODEC =
            ByteBufCodecs.optional(ByteBufCodecs.registry(Registries.SOUND_EVENT));

    public static final StreamCodec<RegistryFriendlyByteBuf, ZenithToastData> STREAM_CODEC = StreamCodec.composite(
            ComponentSerialization.STREAM_CODEC,
            ZenithToastData::title,
            ComponentSerialization.STREAM_CODEC,
            ZenithToastData::message,
            ItemStack.OPTIONAL_STREAM_CODEC,
            ZenithToastData::icon,
            Identifier.STREAM_CODEC,
            ZenithToastData::background,
            ByteBufCodecs.VAR_INT,
            ZenithToastData::durationMs,
            ByteBufCodecs.INT,
            ZenithToastData::titleColor,
            ByteBufCodecs.INT,
            ZenithToastData::messageColor,
            SOUND_STREAM_CODEC,
            ZenithToastData::sound,
            ZenithToastData::new
    );

    public ZenithToastData {
        title = Objects.requireNonNull(title, "title");
        message = Objects.requireNonNull(message, "message");
        icon = Objects.requireNonNull(icon, "icon").copy();
        background = Objects.requireNonNull(background, "background");
        durationMs = Math.max(1, durationMs);
        sound = Objects.requireNonNull(sound, "sound");
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Component title = Component.empty();
        private Component message = Component.empty();
        private ItemStack icon = ItemStack.EMPTY;
        private Identifier background = DEFAULT_BACKGROUND;
        private int durationMs = DEFAULT_DURATION_MS;
        private int titleColor = DEFAULT_TITLE_COLOR;
        private int messageColor = DEFAULT_MESSAGE_COLOR;
        private Optional<SoundEvent> sound = Optional.empty();

        private Builder() {}

        public Builder title(Component title) {
            this.title = Objects.requireNonNull(title, "title");
            return this;
        }

        public Builder message(Component message) {
            this.message = Objects.requireNonNull(message, "message");
            return this;
        }

        public Builder icon(ItemStack icon) {
            this.icon = Objects.requireNonNull(icon, "icon");
            return this;
        }

        public Builder background(Identifier background) {
            this.background = Objects.requireNonNull(background, "background");
            return this;
        }

        public Builder duration(int durationMs) {
            this.durationMs = Math.max(1, durationMs);
            return this;
        }

        public Builder titleColor(int titleColor) {
            this.titleColor = titleColor;
            return this;
        }

        public Builder messageColor(int messageColor) {
            this.messageColor = messageColor;
            return this;
        }

        public Builder sound(SoundEvent sound) {
            this.sound = Optional.of(Objects.requireNonNull(sound, "sound"));
            return this;
        }

        public Builder sound(Holder<SoundEvent> sound) {
            return sound(Objects.requireNonNull(sound, "sound").value());
        }

        public ZenithToastData build() {
            return new ZenithToastData(
                    title,
                    message,
                    icon,
                    background,
                    durationMs,
                    titleColor,
                    messageColor,
                    sound
            );
        }
    }
}
