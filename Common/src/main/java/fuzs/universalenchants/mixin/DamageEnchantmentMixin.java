package fuzs.universalenchants.mixin;

import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.config.ServerConfig;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.DamageEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.TridentImpalerEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DamageEnchantment.class)
public abstract class DamageEnchantmentMixin extends Enchantment {

    protected DamageEnchantmentMixin(Rarity p_44676_, EnchantmentCategory p_44677_, EquipmentSlot[] p_44678_) {
        super(p_44676_, p_44677_, p_44678_);
    }

    @Inject(method = "checkCompatibility", at = @At("HEAD"), cancellable = true)
    public void checkCompatibility$head(Enchantment enchantment, CallbackInfoReturnable<Boolean> callbackInfo) {
        if (UniversalEnchants.CONFIG.get(ServerConfig.class).damageEnchantmentsFix) return;
        if (enchantment instanceof TridentImpalerEnchantment) {
            callbackInfo.setReturnValue(false);
        }
    }
}
