package net.zic.zenithlib.nbt;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;

import java.util.*;
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
    //─────ARRAYS───────────────────────────────────────────────

    //creates a list of compound tags with key/val fields
    public static <T,V> ListTag writeMap(Map<T,V> map,Writer<T> keyWriter,Writer<V> valWriter){
        Set<Map.Entry<T,V>> entries = map.entrySet();
        Writer<Map.Entry<T,V>> entryWriter = value -> {
            CompoundTag tag = new CompoundTag();
            tag.put("key",keyWriter.write(value.getKey()));
            tag.put("value",valWriter.write(value.getValue()));
            return tag;
        };

        return writeCollection(entries,entryWriter);
    }

    public static <T,V> Map<T,V> readMap(ListTag tag,Reader<T> keyReader,Reader<V> valueReader){
        HashMap<T,V> newMap = new HashMap<>();
        readMap(tag,newMap,keyReader,valueReader);
        return Map.copyOf(newMap);
    }

    public static <T,V> void readMap(ListTag tag,HashMap<T,V> existingMap, Reader<T> keyReader,Reader<V> valueReader){
        for(Tag subTag : tag){
            if(!(subTag instanceof CompoundTag compoundTag)) continue;
            existingMap.put(keyReader.read(compoundTag.get("key")),valueReader.read(compoundTag.get("value")));
        }
    }
}
