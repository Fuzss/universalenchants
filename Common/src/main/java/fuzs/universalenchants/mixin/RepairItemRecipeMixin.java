package fuzs.universalenchants.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RepairItemRecipe;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RepairItemRecipe.class)
public abstract class RepairItemRecipeMixin extends CustomRecipe {

    public RepairItemRecipeMixin(ResourceLocation resourceLocation) {
        super(resourceLocation);
    }

    @Inject(method = "canCraftInDimensions", at = @At("HEAD"), cancellable = true)
    public void canCraftInDimensions$inject$head(int i, int j, CallbackInfoReturnable<Boolean> callback) {
//        callback.setReturnValue(false);
    }


    @Inject(method = "matches", at = @At("HEAD"), cancellable = true)
    public void matches$inject$head(CraftingContainer craftingContainer, Level level, CallbackInfoReturnable<Boolean> callback) {
//        callback.setReturnValue(false);
    }
}
