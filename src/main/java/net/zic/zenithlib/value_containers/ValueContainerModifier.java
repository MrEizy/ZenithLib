package net.zic.zenithlib.value_containers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.zic.zenithlib.ZenithLib;
import net.zic.zenithlib.network.ByteBufHelpers;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ValueContainerModifier {
    private final double val;
    private final ModifierOperation operation;
    private final Identifier modifierIdentifier;
    private final Identifier groupIdentifier;

    public static final Codec<ValueContainerModifier> CODEC = RecordCodecBuilder.create(instance -> // Given an instance
            instance.group(
                    Codec.DOUBLE.fieldOf("value").forGetter(ValueContainerModifier::getVal),
                    Codec.STRING.fieldOf("operation").forGetter((container)->container.getOperation().toString()),
                    Identifier.CODEC.fieldOf("id").forGetter(ValueContainerModifier::getIdentifier),
                    Identifier.CODEC.optionalFieldOf("group").forGetter((val)-> Optional.of(val.getGroupIdentifier()))
            ).apply(instance,
                    (value,operation,id,group)->group.map(identifier ->
                            new ValueContainerModifier(value, ModifierOperation.valueOf(operation), id, identifier)).orElseGet(() -> new ValueContainerModifier(value, ModifierOperation.valueOf(operation), id))
            )
    );

    public static final Codec<Map<Identifier, List<ValueContainerModifier>>> MAP_CODEC = Codec.unboundedMap(Identifier.CODEC, ValueContainerModifier.CODEC.listOf());
    public ValueContainerModifier(double val, ModifierOperation operation, Identifier modifierIdentifier) {
        this(val,operation,modifierIdentifier,Identifier.fromNamespaceAndPath(ZenithLib.MOD_ID,"default"));
    }
    public ValueContainerModifier(double val, ModifierOperation operation, Identifier modifierIdentifier, Identifier groupIdentifier) {
        this.val = val;
        this.operation = operation;
        this.modifierIdentifier = modifierIdentifier;
        this.groupIdentifier = groupIdentifier;
    }


    public ModifierOperation getOperation(){return operation;}
    public Identifier getIdentifier(){return this.modifierIdentifier;}
    public Identifier getGroupIdentifier(){return this.groupIdentifier;}
    public double getVal(){return this.val;}


    public void encode(ByteBuf buf){
        buf.writeDouble(val);
        ByteBufHelpers.encodeString(operation.toString(),buf);
        ByteBufHelpers.encodeIdentifier(modifierIdentifier,buf);
        ByteBufHelpers.encodeIdentifier(groupIdentifier,buf);
    }
    public static ValueContainerModifier decode(ByteBuf buf){
        double val = buf.readDouble();
        ModifierOperation operation = ModifierOperation.valueOf(ByteBufHelpers.decodeString(buf));
        Identifier id = ByteBufHelpers.decodeIdentifier(buf);
        Identifier group = ByteBufHelpers.decodeIdentifier(buf);
        return new ValueContainerModifier(val,operation,id,group);
    }

    //copies this modifier with a new id
    public ValueContainerModifier copy(Identifier identifier){
        return new ValueContainerModifier(getVal(),getOperation(),identifier,getGroupIdentifier());
    }
}
