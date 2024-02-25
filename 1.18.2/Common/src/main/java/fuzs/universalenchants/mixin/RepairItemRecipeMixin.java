package fuzs.universalenchants.mixin;

import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.config.ServerConfig;
import fuzs.universalenchants.world.item.crafting.MendingRepairItemRecipeHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RepairItemRecipe;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RepairItemRecipe.class)
abstract class RepairItemRecipeMixin extends CustomRecipe {

    public RepairItemRecipeMixin(ResourceLocation id) {
        super(id);
    }

    @Inject(method = "matches", at = @At("RETURN"), cancellable = true)
    public void matches(CraftingContainer craftingContainer, Level level, CallbackInfoReturnable<Boolean> callback) {
        if (!UniversalEnchants.CONFIG.get(ServerConfig.class).mendingCraftingRepair) return;
        if (!callback.getReturnValue()) {
            if (MendingRepairItemRecipeHelper.matches(craftingContainer, level)) {
                callback.setReturnValue(true);
            }
        }
    }

    @Inject(method = "assemble", at = @At("HEAD"), cancellable = true)
    public void assemble(CraftingContainer craftingContainer, CallbackInfoReturnable<ItemStack> callback) {
        if (!UniversalEnchants.CONFIG.get(ServerConfig.class).mendingCraftingRepair) return;
        callback.setReturnValue(MendingRepairItemRecipeHelper.assemble(craftingContainer));
    }
}
