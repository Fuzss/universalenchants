package fuzs.universalenchants.mixin;

import fuzs.universalenchants.util.StoredEnchantmentHelper;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Collections;

@Mixin(GrindstoneMenu.class)
abstract class GrindstoneMenuMixin extends AbstractContainerMenu {

    protected GrindstoneMenuMixin(@Nullable MenuType<?> menuType, int containerId) {
        super(menuType, containerId);
    }

    @ModifyVariable(method = "removeNonCurses", at = @At(value = "STORE", ordinal = 0), ordinal = 1)
    private ItemStack removeNonCurses(ItemStack itemStack) {
        // also remove stored enchantments in grind stone
        // no need to increase repair cost for remaining enchantments as vanilla does, that's only necessary for curses which cannot be stored
        StoredEnchantmentHelper.setStoredEnchantments(Collections.emptyMap(), itemStack);
        return itemStack;
    }
}
