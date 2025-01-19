package fuzs.universalenchants.mixin;

import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.config.ServerConfig;
import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(HorseArmorItem.class)
abstract class HorseArmorItemMixin extends Item {

    public HorseArmorItemMixin(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isEnchantable(ItemStack itemStack) {
        // need to override this as enchantable items usually must have durability
        return UniversalEnchants.CONFIG.get(ServerConfig.class).enchantableHorseArmor || super.isEnchantable(itemStack);
    }

    @Override
    public int getEnchantmentValue() {
        // just use this value, it's similar enough to other item's enchantment value
        return UniversalEnchants.CONFIG.get(ServerConfig.class).enchantableHorseArmor ?
                this.getProtection() :
                super.getEnchantmentValue();
    }

    @Shadow
    public abstract int getProtection();
}
