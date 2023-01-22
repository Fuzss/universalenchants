package fuzs.universalenchants.mixin.accessor;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Enchantment.class)
public interface EnchantmentAccessor {

    @Accessor("category")
    @Mutable
    void universalenchants$setCategory(EnchantmentCategory category);

    @Accessor("slots")
    @Mutable
    void universalenchants$setSlots(EquipmentSlot[] slots);
}
