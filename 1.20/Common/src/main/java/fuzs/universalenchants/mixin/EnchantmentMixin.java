package fuzs.universalenchants.mixin;

import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.config.ServerConfig;
import fuzs.universalenchants.handler.EnchantmentDataHandler;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
abstract class EnchantmentMixin {

    @Inject(method = "isCompatibleWith", at = @At("HEAD"), cancellable = true)
    public void isCompatibleWith(Enchantment other, CallbackInfoReturnable<Boolean> callback) {
        if (!UniversalEnchants.CONFIG.get(ServerConfig.class).adjustEnchantmentCompatibilities) return;
        callback.setReturnValue(EnchantmentDataHandler.isCompatibleWith(Enchantment.class.cast(this), other));
    }
}
