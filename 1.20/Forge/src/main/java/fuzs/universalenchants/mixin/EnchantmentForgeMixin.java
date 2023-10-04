package fuzs.universalenchants.mixin;

import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.config.ServerConfig;
import fuzs.universalenchants.handler.EnchantmentDataHandler;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
abstract class EnchantmentForgeMixin {

    @Inject(method = "canApplyAtEnchantingTable", at = @At("HEAD"), cancellable = true, remap = false)
    public void canApplyAtEnchantingTable(ItemStack itemStack, CallbackInfoReturnable<Boolean> callback) {
        if (!UniversalEnchants.CONFIG.get(ServerConfig.class).adjustEnchantingTableEnchantments) return;
        callback.setReturnValue(EnchantmentDataHandler.canApplyAtEnchantingTable(Enchantment.class.cast(this), itemStack.getItem()));
    }
}
