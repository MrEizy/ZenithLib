package net.zic.zenithlib.stats;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.zic.zenithlib.Config;
import net.zic.zenithlib.value_containers.typed.ValueContainer;

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

    public Component getName() {
        return name;
    }

    public Component getDescription() {
        return description;
    }

    public Component getShortName() {
        return shortName;
    }

    public ValueContainer<Double> statInstance(double base){
        return ZenithStatHelper.statInstance(this,base);
    }


}
