package fuzs.universalenchants.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
abstract class EnchantmentHelperMixin {

    @Inject(method = "getKnockbackBonus", at = @At("HEAD"), cancellable = true)
    private static void getKnockbackBonus(LivingEntity player, CallbackInfoReturnable<Integer> callback) {
        // prevent shields from applying knockback when hitting targets
        if (player.getMainHandItem().getItem() instanceof ShieldItem) callback.setReturnValue(0);
    }
}
