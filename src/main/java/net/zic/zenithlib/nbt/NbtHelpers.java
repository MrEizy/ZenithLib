package net.zic.zenithlib.nbt;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.*;
import java.util.function.IntFunction;

public class NbtHelpers {
    //─────IDENTIFIERS───────────────────────────────────────────────
    public static void writeIdentifier(ValueOutput output, String id, Identifier value){
        output.putString(id,value.toString());
    }
    public static Identifier readIdentifier(ValueInput input,String id){
        return Identifier.parse(input.getStringOr(id,""));
    }

    //─────ARRAYS───────────────────────────────────────────────
    public static <T> void writeArray(ValueOutput output,String id,T[] input,Writer<T> writer){
        ValueOutput.ValueOutputList list = output.childrenList(id);
        for(T value : input){
            writer.write(list.addChild(),"value",value);
        }
    }
    public static <T> void writeCollection(ValueOutput output,String id,Collection<T> input,Writer<T> writer){
        ValueOutput.ValueOutputList list = output.childrenList(id);
        for(T value : input){
            writer.write(list.addChild(),"value",value);
        }
    }

    public static <T> List<T> readList(ValueInput input,String id, Reader<T> reader){
        ArrayList<T> list = new ArrayList<>();
        ValueInput.ValueInputList values =  input.childrenListOrEmpty(id);

        for(ValueInput field : values){
            list.add(reader.read(field,"value"));
        }

        return List.copyOf(list);
    }

    //─────MAPS───────────────────────────────────────────────


    public static <T,V> void writeMap(ValueOutput output,String id,Map<T,V> map, Writer<T> keyWriter,Writer<V> valueWriter){

        Set<Map.Entry<T,V>> entries = map.entrySet();
        Writer<Map.Entry<T,V>> entryWriter = (out,entryId,val) -> {
            keyWriter.write(out,"key",val.getKey());
            valueWriter.write(out,"value",val.getValue());
        };
        writeCollection(output,id,entries,entryWriter);

    }

    public static <T,V> void readMap(ValueInput input,String id, HashMap<T,V> existingMap,Reader<T> keyReader,Reader<V> valueReader){

        ValueInput.ValueInputList values =  input.childrenListOrEmpty(id);

        for(ValueInput field : values){
            existingMap.put(keyReader.read(field,"key"),valueReader.read(field,"value"));
        }
    }
    public static <T,V> Map<T,V> readMap(ValueInput input,String id,Reader<T> keyReader,Reader<V> valueReader){
        HashMap<T,V> newMap = new HashMap<>();
        readMap(input,id,newMap,keyReader,valueReader);
        return newMap;
    }



}
