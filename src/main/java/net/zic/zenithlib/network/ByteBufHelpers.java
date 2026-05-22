package net.zic.zenithlib.network;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.Hash;
import net.minecraft.resources.Identifier;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;

public class ByteBufHelpers {
    //─────STRING───────────────────────────────────────────────
    public static void encodeString(String input, ByteBuf buf){
        buf.writeInt(input.length());
        buf.writeCharSequence(input, Charset.defaultCharset());
    }
    public static String decodeString(ByteBuf buf){
        return (String) buf.readCharSequence(buf.readInt(),Charset.defaultCharset());
    }

    //─────IDENTIFIER───────────────────────────────────────────────
    public static void encodeIdentifier(Identifier input, ByteBuf buf){
        encodeString(input.toString(),buf);
    }
    public static Identifier decodeIdentifier(ByteBuf buf){
        return Identifier.parse(decodeString(buf));
    }
    //─────ARRAYS───────────────────────────────────────────────
    public static <T> void encodeArray(T[] input, ByteBuf buf, Encoder<T> encoder){
        buf.writeInt(input.length);
        for(T value : input){
            encoder.encode(value,buf);
        }
    }
    //encodes an array that can have null fields
    public static <T> void encodeNullableArray(T[] input,ByteBuf buf,Encoder<T> encoder){
        buf.writeInt(input.length);
        for(T value : input){
            buf.writeBoolean(value != null);
            if(value != null) encoder.encode(value,buf);
        }
    }


    public static <T> List<T> decodeArray(ByteBuf buf,Decoder<T> decoder){
        int size = buf.readInt();
        ArrayList<T> output = new ArrayList<>();

        for(int i = 0;i<size;i++){
            output.add(decoder.decode(buf));
        }
        return output;
    }

    //decodes an array as a List that can have null fields
    public static <T> List<T> decodeNullableArray(ByteBuf buf,Decoder<T> decoder){
        int size = buf.readInt();
        ArrayList<T> output = new ArrayList<>();

        for(int i = 0;i<size;i++){
            if(!buf.readBoolean()){
                output.add(null);
                continue;
            }
            output.add(decoder.decode(buf));
        }
        return output;
    }


    //we cannot create generic arrays so we need a factory for it
    public static <T> T[] decodeArray(ByteBuf buf, Decoder<T> decoder, IntFunction<T[]> factory){
        int size = buf.readInt();
        T[] output = factory.apply(size);
        for(int i = 0;i<size;i++){
            output[i] = decoder.decode(buf);
        }
        return output;
    }
    //decodes an array that can have null fields
    public static <T> T[] decodeNullableArray(ByteBuf buf, Decoder<T> decoder, IntFunction<T[]> factory){
        int size = buf.readInt();
        T[] output = factory.apply(size);
        for(int i = 0;i<size;i++){
            if(!buf.readBoolean()){
                output[i] = null;
                continue;
            }
            output[i] = decoder.decode(buf);
        }
        return output;
    }

    public static String[] decodeStringArray(ByteBuf buf){
        int size = buf.readInt();
        String[] output = new String[size];
        for(int i = 0;i<size;i++){
            output[i] = decodeString(buf);
        }
        return output;
    }

    //─────MAPS───────────────────────────────────────────────
    public static <T,V> void encodeMap(Map<T,V> map,Encoder<T> keyEncoder,Encoder<V> valEncoder,ByteBuf buf){
        buf.writeInt(map.size());
        for(Map.Entry<T,V> entry : map.entrySet()){
            keyEncoder.encode(entry.getKey(),buf);
            valEncoder.encode(entry.getValue(),buf);
        }
    }
    // creates an immutable map holding the data
    public static <T,V> Map<T,V> decodeMap(Decoder<T> keyDecoder,Decoder<V> valDecoder,ByteBuf buf){
        HashMap<T,V> map = new HashMap<>();
        decodeMap(map,keyDecoder,valDecoder,buf);
        return Map.copyOf(map);
    }

    public static  <T,V> void decodeMap(HashMap<T,V> existingMap, Decoder<T> keyDecoder,Decoder<V> valDecoder,ByteBuf buf){
        int size = buf.readInt();
        for(int i = 0;i <size;i++){
            existingMap.put(keyDecoder.decode(buf),valDecoder.decode(buf));
        }
    }
}
