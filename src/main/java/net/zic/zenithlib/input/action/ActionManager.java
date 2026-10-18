package net.zic.zenithlib.input.action;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;

import java.util.HashSet;

public final class ActionManager {
    private final LivingEntity entity;
    private final HashSet<Identifier> activeActions = new HashSet<>();
    public ActionManager(LivingEntity entity){
        this.entity = entity;
    }


    public boolean isActive(Identifier identifier){
        return activeActions.contains(identifier);
    }

    public void actionStart(Identifier identifier){
        if(activeActions.contains(identifier)) return;
        activeActions.add(identifier);


        NeoForge.EVENT_BUS.post(new ActionEvent.Start(entity,identifier));
    }
    public void actionEnd(Identifier identifier){
        if(!activeActions.contains(identifier))return;
        activeActions.remove(identifier);
        NeoForge.EVENT_BUS.post(new ActionEvent.End(entity,identifier));
    }
    public void handleActionUpdate(Identifier identifier,boolean isDown){
        if(isDown) actionStart(identifier);
        else actionEnd(identifier);
    }
}
