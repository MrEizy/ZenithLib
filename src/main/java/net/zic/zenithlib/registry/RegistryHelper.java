package net.zic.zenithlib.registry;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.RegistryBuilder;
import net.zic.zenithlib.ZenithLib;

import java.util.Optional;
import java.util.function.Supplier;

public class RegistryHelper {

    public record DataPackRegistry<T>(ResourceKey<Registry<T>> key, Supplier<Codec<T>> codec){

        public Registry<T> get(Level level){
            Optional<Registry<T>> registry = level.registryAccess().lookup(key);
            if(registry.isEmpty()){
                ZenithLib.LOGGER.error("error when trying to access registry with key {}", key.identifier());
                return null;
            }
            return level.registryAccess().lookup(key).orElse(null);
        }
    }

    public static <T> DataPackRegistry<T> dataPackRegistry(String key, String modId, Supplier<Codec<T>> codec){
        return new DataPackRegistry<>(key(key,modId),codec);
    }

    public static <T> ResourceKey<Registry<T>> key(String key,String modId){
        return ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(modId,key));
    }


    public static <T> Registry<T> registry(String key){
        return registry(key,"none");
    }
    public static <T> Registry<T> registry(String key,String defaultKey){
        Identifier identifier = Identifier.fromNamespaceAndPath(ZenithLib.MOD_ID,key);
        Identifier defaultIdentifier = Identifier.fromNamespaceAndPath(ZenithLib.MOD_ID,defaultKey);

        ResourceKey<Registry<T>> registryKey = ResourceKey.createRegistryKey(identifier);
        return new RegistryBuilder<>(registryKey)
                .defaultKey(defaultIdentifier)
                .create();
    }


    public static <T> T getRegistryObject(Registry<T> registry,Identifier identifier){

        try{
            return registry.getValue(identifier);
        }catch (Throwable exception){
            ZenithLib.LOGGER.error("unable to get registry object : ",exception);
        }
        return null;
    }
}
