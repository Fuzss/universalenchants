package fuzs.universalenchants.mixin;

import fuzs.universalenchants.util.StoredEnchantmentHelper;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(targets = "net.minecraft.world.inventory.GrindstoneMenu$4")
abstract class GrindstoneMenu$SlotMixin extends Slot {

    public GrindstoneMenu$SlotMixin(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @ModifyVariable(method = "getExperienceFromItem", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private int getExperienceFromItem(int experienceReward, ItemStack itemStack) {
        // add stored enchantments experience reward
        return experienceReward +
                EnchantmentHelper.deserializeEnchantments(StoredEnchantmentHelper.getStoredEnchantments(itemStack))
                        .entrySet()
                        .stream()
                        .mapToInt(entry -> entry.getKey().getMinCost(entry.getValue()))
                        .sum();
    }
}
