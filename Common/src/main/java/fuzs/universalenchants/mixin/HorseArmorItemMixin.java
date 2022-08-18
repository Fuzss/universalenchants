package fuzs.universalenchants.mixin;

import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(HorseArmorItem.class)
public abstract class HorseArmorItemMixin extends Item {

    public HorseArmorItemMixin(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isEnchantable(ItemStack itemStack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return this.getProtection();
    }

    @Shadow
    public abstract int getProtection();
}
