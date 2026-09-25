package net.zic.zenithlib.value_containers.typed;

import java.util.List;

public record ModifierHolder<T extends Number>(List<Modifier<T>> flat, List<Modifier<Double>> multiplier){
}
