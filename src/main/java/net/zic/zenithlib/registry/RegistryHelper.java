package net.zic.zenithlib.registry;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;
import net.zic.zenithlib.ZenithLib;

import java.util.Optional;
import java.util.function.Supplier;

public class RegistryHelper {

    public record DataPackRegistry<T>(ResourceKey<Registry<T>> key, Supplier<Codec<T>> codec){

        public Registry<T> get(RegistryAccess access){
            Optional<Registry<T>> registry = access.lookup(key);
            if(registry.isEmpty()){
                ZenithLib.LOGGER.error("error when trying to access registry with key {}", key.identifier());
                return null;
            }
            return registry.orElse(null);
        }
    }

    public static <T> DataPackRegistry<T> dataPackRegistry(String namespace, String key, Supplier<Codec<T>> codec){
        return new DataPackRegistry<>(key(namespace,key),codec);
    }

    public static <T> ResourceKey<Registry<T>> key(String namespace,String key){
        return ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(namespace,key));
    }
    public static <T> ResourceKey<T> key(ResourceKey<Registry<T>> resourceKey,String namespace,String path){
        return key(resourceKey,Identifier.fromNamespaceAndPath(namespace,path));
    }
    public static <T> ResourceKey<T> key(ResourceKey<Registry<T>> registryKey,Identifier identifier){
        return ResourceKey.create(registryKey,identifier);
    }

    public static <T> Registry<T> registry(String namespace,String key){
        return registry(namespace,key,"none");
    }
    public static <T> Registry<T> registry(String namespace,String key,String defaultKey){
        Identifier identifier = Identifier.fromNamespaceAndPath(namespace,key);
        Identifier defaultIdentifier = Identifier.fromNamespaceAndPath(namespace,defaultKey);

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
