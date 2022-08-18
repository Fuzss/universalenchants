package fuzs.universalenchants.mixin.accessor;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Enchantment.class)
public interface EnchantmentAccessor {

    @Accessor
    @Mutable
    void setCategory(EnchantmentCategory category);

    @Accessor
    @Mutable
    void setSlots(EquipmentSlot[] slots);
}
