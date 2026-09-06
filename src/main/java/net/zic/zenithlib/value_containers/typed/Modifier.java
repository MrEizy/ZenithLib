package net.zic.zenithlib.value_containers.typed;

import net.minecraft.resources.Identifier;
/*
    While MultiplierModifier IS just Double bonus modifier, the key is that it is used to differentiate
    them internally. e.g a double value container would need a way to differentiate flat vs multiplier
 */
public sealed interface Modifier permits BonusModifier,MultiplierModifier {
    Identifier getId();
    Identifier getGroup();
    int getOperationGroup();

    static <T extends Number> BonusModifier<T> bonus(Identifier id,T val){
        return new BonusModifier<>(id,0,val);
    }
    static <T extends Number> BonusModifier<T> bonus(Identifier id,int operationGroup,T val){
        return new BonusModifier<>(id,operationGroup,val);
    }
    static MultiplierModifier multiplier(Identifier id, double val){
        return new MultiplierModifier(id,Identifier.parse("none"),0,val);
    }
    static MultiplierModifier multiplier(Identifier id,Identifier group, double val){
        return new MultiplierModifier(id,group,0,val);
    }
    static MultiplierModifier multiplier(Identifier id,int operationGroup, double val){
        return new MultiplierModifier(id,Identifier.parse("none"),operationGroup,val);
    }
    static MultiplierModifier multiplier(Identifier id,Identifier group,int operationGroup, double val){
        return new MultiplierModifier(id,group,operationGroup,val);
    }

}
