package net.zic.zenithlib.nbt;

import net.minecraft.nbt.Tag;

@FunctionalInterface
public interface Reader<T>{
    T read(Tag tag);
}
