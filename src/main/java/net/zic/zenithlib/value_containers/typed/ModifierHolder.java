package net.zic.zenithlib.value_containers.typed;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.common.Mod;

import java.util.List;

public record ModifierHolder<T extends Number>(List<Modifier<T>> flat, List<Modifier<Double>> multiplier){


}
