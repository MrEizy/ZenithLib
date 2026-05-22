package net.zic.zenithlib.nbt;

import net.minecraft.nbt.Tag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

@FunctionalInterface
public interface Reader<T>{
    T read(ValueInput input, String id);
}
