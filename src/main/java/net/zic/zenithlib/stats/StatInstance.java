package net.zic.zenithlib.stats;

import io.netty.buffer.ByteBuf;
import net.minecraft.resources.Identifier;
import net.zic.zenithlib.network.ByteBufHelpers;
import net.zic.zenithlib.registry.RegistryHelper;
import net.zic.zenithlib.common.ZenithRegistries;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

import java.util.Collection;

public class StatInstance extends ValueContainer {

    public StatInstance(Stat stat, double base) {
        super(ZenithRegistries.STAT_REGISTRY.getKey(stat), base);
    }
    private StatInstance(Identifier identifier, double base) {
        super(identifier, base);
    }

    public Stat getStat(){
        return RegistryHelper.getRegistryObject(ZenithRegistries.STAT_REGISTRY,getIdentifier());
    }

    public static void encode(StatInstance instance, ByteBuf buf){
        ValueContainer.encode(buf,instance);
    }
    public static StatInstance decode(ByteBuf buf){
        Identifier identifier = ByteBufHelpers.decodeIdentifier(buf);
        double base = buf.readDouble();
        StatInstance container = new StatInstance(identifier,base);

        ValueContainer.decode(buf,container);
        return container;
    }
}
