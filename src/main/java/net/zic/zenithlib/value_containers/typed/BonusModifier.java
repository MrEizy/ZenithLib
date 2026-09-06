package net.zic.zenithlib.value_containers.typed;

import net.minecraft.resources.Identifier;

public record BonusModifier<T extends Number>(Identifier id,int operationGroup,T val) implements Modifier{

    private static final Identifier GROUP = Identifier.parse("none");
    //used for a hacky codec
    public BonusModifier(String temp,Identifier id,int operationGroup,T val){
        this(id,operationGroup,val);
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public Identifier getGroup() {
        return GROUP;
    }

    @Override
    public int getOperationGroup() {
        return operationGroup;
    }


}
