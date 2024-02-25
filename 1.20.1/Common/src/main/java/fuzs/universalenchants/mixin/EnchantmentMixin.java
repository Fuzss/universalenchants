package fuzs.universalenchants.mixin;

import fuzs.universalenchants.world.item.enchantment.serialize.EnchantmentHoldersManager;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
abstract class EnchantmentMixin {

    @Inject(method = "isCompatibleWith", at = @At("TAIL"), cancellable = true)
    public void isCompatibleWith(Enchantment other, CallbackInfoReturnable<Boolean> callback) {
        callback.setReturnValue(EnchantmentHoldersManager.isCompatibleWith(Enchantment.class.cast(this), other, callback.getReturnValue()));
    }
}
