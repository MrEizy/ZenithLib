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

}
