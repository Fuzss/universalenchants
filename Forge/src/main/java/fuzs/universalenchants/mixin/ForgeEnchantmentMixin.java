package fuzs.universalenchants.mixin;

import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.config.ServerConfig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
abstract class ForgeEnchantmentMixin {
    @Shadow
    @Final
    public EnchantmentCategory category;

    @Inject(method = "canApplyAtEnchantingTable", at = @At("HEAD"), cancellable = true, remap = false)
    public void canApplyAtEnchantingTable$inject$head(ItemStack stack, CallbackInfoReturnable<Boolean> callback) {
        // this is not good as it significantly alters the behavior of this method, but:
        // for modded items that would naturally not be enchantable, they often check for a specific enchantment category in this method implemented on the item
        // this fails as we exchange all of them, but there is a custom implementation in ForgeAbstractions::defaultEnchantmentDataBuilder to find all affected modded items manually
        // so this theoretically shouldn't be relevant anyway...
        if (!UniversalEnchants.CONFIG.get(ServerConfig.class).allowModItemSupport) return;
        callback.setReturnValue(this.category.canEnchant(stack.getItem()));
    }
}
