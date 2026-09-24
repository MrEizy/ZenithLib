package net.zic.zenithlib.value_containers.typed;

import net.minecraft.resources.Identifier;

public record MultiplierModifier(Identifier id, Identifier group,int operationGroup,double val) implements Modifier{

    //used for a hacky codec
    public MultiplierModifier(String temp,Identifier id, Identifier group,int operationGroup,double val){
        this(id,group,operationGroup,val);
    }
    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public Identifier getGroup() {
        return group;
    }

    @Override
    public int getOperationGroup() {
        return operationGroup;
    }


}
