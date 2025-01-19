package fuzs.universalenchants.mixin;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "net/minecraft/world/inventory/HorseInventoryMenu$2")
abstract class HorseInventoryMenu$SlotMixin extends Slot {

    public HorseInventoryMenu$SlotMixin(Container container, int i, int j, int k) {
        super(container, i, j, k);
    }

    @Override
    public boolean mayPickup(Player player) {
        return (this.getItem().isEmpty() || player.isCreative() ||
                !EnchantmentHelper.hasBindingCurse(this.getItem())) && super.mayPickup(player);
    }
}
