package net.zic.zenithlib.nbt;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.IntFunction;

public class NbtHelpers {
    //─────IDENTIFIERS───────────────────────────────────────────────
    public static Tag writeIdentifier(Identifier input){
        return StringTag.valueOf(input.toString());
    }
    //IDEA consider throwing an error here
    public static Identifier readIdentifier(Tag input){
        if(input instanceof StringTag stringTag) return Identifier.parse(stringTag.toString());
        return null;
    }

    //─────ARRAYS───────────────────────────────────────────────
    public static <T> ListTag writeArray(T[] input,Writer<T> writer){
        ListTag listTag = new ListTag();
        for(T value : input){
            listTag.add(writer.write(value));
        }
        return listTag;
    }
    public static <T> ListTag writeCollection(Collection<T> input,Writer<T> writer){
        ListTag listTag = new ListTag();
        for(T value : input){
            listTag.add(writer.write(value));
        }
        return listTag;
    }

    public static <T> List<T> readList(ListTag tag,Reader<T> reader){
        ArrayList<T> list = new ArrayList<>();
        for (Tag value : tag) {
            list.add(reader.read(value));
        }
        return List.copyOf(list);
    }

    public static <T> T[] readArray(ListTag tag, Reader<T> reader, IntFunction<T[]> builder){
        T[] output = builder.apply(tag.size());
        for(int i = 0; i<tag.size();i++){
            output[i] = reader.read(tag.get(i));
        }
        return output;
    }


}
