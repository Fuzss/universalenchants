package fuzs.universalenchants.mixin;

import fuzs.universalenchants.util.StoredEnchantmentHelper;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
abstract class ItemStackMixin {

    @Inject(method = "isEnchanted", at = @At("HEAD"), cancellable = true)
    public void isEnchanted(CallbackInfoReturnable<Boolean> callback) {
        // items with only stored enchantments are still enchanted, prevents them from being enchanted at enchanting table again
        if (StoredEnchantmentHelper.hasStoredEnchantments(ItemStack.class.cast(this))) {
            callback.setReturnValue(true);
        }
    }
}
