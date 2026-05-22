package net.zic.zenithlib.value_containers;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.zic.zenithlib.network.ByteBufHelpers;

import java.util.Collection;

public class NamedValueContainer extends ValueContainer{

    private final Component name;

    public NamedValueContainer(Component name, Identifier valueIdentifier, double base) {
        super(valueIdentifier, base);
        this.name = name;

    }
    public Component getName(){return name;}

    public static void encode(NamedValueContainer container, ByteBuf buf){
        ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.encode(buf,container.name);
        ValueContainer.encode(container,buf);
    }

    public static NamedValueContainer decode(ByteBuf buf){
        Component name = ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.decode(buf);
        Identifier identifier = ByteBufHelpers.decodeIdentifier(buf);
        double base = buf.readDouble();
        NamedValueContainer container = new NamedValueContainer(name,identifier,base);

        Collection<ValueContainerModifier> modifiers = ByteBufHelpers.decodeArray(buf,ValueContainerModifier::decode);
        for(ValueContainerModifier modifier : modifiers){
            container.addModifierNoCacheUpdate(modifier);
        }

        container.calculateCachedVal();
        return container;
    }
}
