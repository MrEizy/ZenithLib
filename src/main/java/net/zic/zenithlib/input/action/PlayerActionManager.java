package net.zic.zenithlib.input.action;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;

import java.util.HashSet;

public class PlayerActionManager {

    private final Player player;
    private final HashSet<Identifier> activeActions = new HashSet<>();
    public PlayerActionManager(Player player){
        this.player = player;
    }


    public boolean isActive(Identifier identifier){
        return activeActions.contains(identifier);
    }

    protected void actionStart(Identifier identifier){
        if(activeActions.contains(identifier)) return;
        activeActions.add(identifier);


        NeoForge.EVENT_BUS.post(new ActionEvent.Start(player,identifier));
    }
    protected void actionEnd(Identifier identifier){
        if(!activeActions.contains(identifier))return;
        activeActions.remove(identifier);
        NeoForge.EVENT_BUS.post(new ActionEvent.End(player,identifier));
    }
    public void handleActionUpdate(Identifier identifier,boolean isDown){
        if(isDown) actionStart(identifier);
        else actionEnd(identifier);
    }
}
