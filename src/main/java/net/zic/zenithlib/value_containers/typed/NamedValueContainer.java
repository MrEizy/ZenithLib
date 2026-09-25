package net.zic.zenithlib.value_containers.typed;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.zic.zenithlib.network.Decoder;
import net.zic.zenithlib.network.Encoder;

import java.util.function.BiFunction;

public class NamedValueContainer<T extends Number> extends ValueContainer<T>{
    private final Component name;
    public NamedValueContainer(
            Component name,
            Identifier containerId,
            T baseValue,
            BiFunction<T, T, T> adder,
            BiFunction<T, Double, T> multiplier,
            Encoder<T> encoder,
            Decoder<T> decoder,
            T defaultValue) {
        super(containerId,baseValue, adder, multiplier,encoder,decoder,defaultValue);
        this.name = name;
    }

    public Component getName() {
        return name;
    }
}
