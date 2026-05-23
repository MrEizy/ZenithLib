package net.zic.zenithlib.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.custom_attributes.ZenithAttributeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "getAttributeValue",at=@At("HEAD"),cancellable = true)
    private void getAttributeValue(Holder<Attribute> attribute, CallbackInfoReturnable<Double> cir){
        LivingEntity self = (LivingEntity) (Object) this;

        if(self.hasData(ZenithAttachments.ATTRIBUTE_HOLDER)){
            ZenithAttributeHolder holder = self.getData(ZenithAttachments.ATTRIBUTE_HOLDER);
            if(holder.hasAttribute(attribute)) cir.setReturnValue(holder.getAttribute(attribute).getValue());
        }
    }
    @Inject(method = "getSpeed", at = @At("HEAD"), cancellable = true)
    private void overrideSpeed(CallbackInfoReturnable<Float> cir) {
        LivingEntity self = (LivingEntity) (Object) this;

        if(self.hasData(ZenithAttachments.ATTRIBUTE_HOLDER)){
            ZenithAttributeHolder holder = self.getData(ZenithAttachments.ATTRIBUTE_HOLDER);
            if(holder.hasAttribute(Attributes.MOVEMENT_SPEED)) cir.setReturnValue((float) holder.getAttribute(Attributes.MOVEMENT_SPEED).getValue());
        }

    }
}
