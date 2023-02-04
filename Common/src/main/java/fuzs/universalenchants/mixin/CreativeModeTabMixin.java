package fuzs.universalenchants.mixin;

import fuzs.universalenchants.world.item.enchantment.data.BuiltInEnchantmentDataManager;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(CreativeModeTab.class)
abstract class CreativeModeTabMixin {

    @ModifyVariable(method = "hasEnchantmentCategory", at = @At("HEAD"))
    public EnchantmentCategory hasEnchantmentCategory$modifyVariable$head(EnchantmentCategory category) {
        return BuiltInEnchantmentDataManager.INSTANCE.convertToVanillaCategory(category);
    }
}
