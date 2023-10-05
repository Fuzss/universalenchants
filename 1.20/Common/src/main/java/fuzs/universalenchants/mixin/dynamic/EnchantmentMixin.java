package fuzs.universalenchants.mixin.dynamic;

import fuzs.universalenchants.world.item.enchantment.data.MaxLevelManager;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
abstract class EnchantmentMixin {

    @Inject(method = "getMaxLevel", at = @At("HEAD"), cancellable = true, require = 0)
    public void getMaxLevel(CallbackInfoReturnable<Integer> callback) {
        MaxLevelManager.getMaxLevel2(Enchantment.class.cast(this)).ifPresent(callback::setReturnValue);
    }
}
