package fuzs.universalenchants.mixin;

import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.config.ServerConfig;
import net.minecraft.core.Holder;
import net.minecraft.world.item.AnimalArmorItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnimalArmorItem.class)
abstract class AnimalArmorItemMixin extends ArmorItem {

    public AnimalArmorItemMixin(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Inject(method = "isEnchantable", at = @At("HEAD"), cancellable = true)
    public void isEnchantable(ItemStack itemStack, CallbackInfoReturnable<Boolean> callback) {
        // need to override this as enchantable items usually must have durability
        if (UniversalEnchants.CONFIG.get(ServerConfig.class).enchantableHorseArmor) {
            callback.setReturnValue(true);
        }
    }

    @Override
    public int getEnchantmentValue() {
        // just use this value, it's similar enough to other item's enchantment value
        return UniversalEnchants.CONFIG.get(ServerConfig.class).enchantableHorseArmor ? this.getDefense() :
                super.getEnchantmentValue();
    }
}
