package net.zic.zenithlib.value_containers.typed;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.function.BiFunction;

public class NamedValueContainer<T extends Number> extends ValueContainer<T>{
    private final Component name;
    public NamedValueContainer(Component name,Identifier containerId, T baseValue, BiFunction<T, T, T> adder, BiFunction<T, Double, T> multiplier) {
        super(containerId,baseValue, adder, multiplier);
        this.name = name;
    }

    public Component getName() {
        return name;
    }
}
