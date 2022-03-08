package fuzs.universalenchants.mixin;

import fuzs.universalenchants.handler.EnchantCompatHandler;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public abstract class EnchantmentMixin {
    @Inject(method = "isCompatibleWith", at = @At("HEAD"), cancellable = true)
    public void isCompatibleWith$head(Enchantment other, CallbackInfoReturnable<Boolean> callbackInfo) {
        if (EnchantCompatHandler.INSTANCE.isCompatibleWith((Enchantment) (Object) this, other)) callbackInfo.setReturnValue(true);
    }
}
