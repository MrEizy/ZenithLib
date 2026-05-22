package net.zic.zenithlib.nbt;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

@FunctionalInterface
public interface Writer<T>{
    Tag write(T value);
}
