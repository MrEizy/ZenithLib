package net.zic.zenithlib.nbt;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.storage.ValueOutput;

@FunctionalInterface
public interface Writer<T>{
    void write(ValueOutput output,String id, T value);
}
