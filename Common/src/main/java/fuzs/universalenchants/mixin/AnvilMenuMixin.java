package fuzs.universalenchants.mixin;

import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.config.ServerConfig;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin extends ItemCombinerMenu {

    public AnvilMenuMixin(@Nullable MenuType<?> menuType, int i, Inventory inventory, ContainerLevelAccess containerLevelAccess) {
        super(menuType, i, inventory, containerLevelAccess);
    }

    @Inject(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AnvilMenu;broadcastChanges()V"))
    public void createResult$inject$invoke(CallbackInfo callback) {
        if (!UniversalEnchants.CONFIG.get(ServerConfig.class).mendingCraftingRepair) return;
        ItemStack resultStack = this.resultSlots.getItem(0);
        if (!resultStack.isEmpty() && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MENDING, resultStack) > 0) {
            ItemStack inputStack = this.inputSlots.getItem(0);
            if (resultStack.getEnchantmentTags().equals(inputStack.getEnchantmentTags())) {
                if (resultStack.getHoverName().getString().equals(inputStack.getHoverName().getString())) {
                    // just reverse vanilla's repair cost calculation
                    resultStack.setRepairCost((resultStack.getBaseRepairCost() - 1) / 2);
                }
            }
        }
    }
}
