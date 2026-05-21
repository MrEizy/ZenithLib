package net.zic.zenithlib.cooldown;


import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

@FunctionalInterface
public interface CooldownListener {

    void finished(Entity entity, Identifier cooldown);
}
