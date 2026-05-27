package net.zic.zenithlib.stats;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

// CONSIDER making this datapackable
public class Stat {
    private final Component name;
    private Component shortName = Component.empty();
    private  Component description = Component.empty();

    public Stat(Component name){
        this.name = name;
    }

    public Stat setShortName(Component component){
        shortName = component;
        return this;
    }
    public Stat setDescription(Component component){
        description =component;
        return this;
    }


    public StatInstance newInstance(double base){
        return new StatInstance(this,base);
    }


}
