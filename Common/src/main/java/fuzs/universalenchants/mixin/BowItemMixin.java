package fuzs.universalenchants.mixin;

import fuzs.universalenchants.handler.ItemCompatHandler;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(BowItem.class)
public abstract class BowItemMixin extends ProjectileWeaponItem {

    public BowItemMixin(Properties p_43009_) {
        super(p_43009_);
    }

    @ModifyVariable(method = "releaseUsing", at = @At("STORE"), ordinal = 0, slice = @Slice(to = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BowItem;customArrow(Lnet/minecraft/world/entity/projectile/AbstractArrow;)Lnet/minecraft/world/entity/projectile/AbstractArrow;", remap = false)))
    public AbstractArrow releaseUsing$storeArrow(AbstractArrow arrow, ItemStack stack) {
        ItemCompatHandler.applyPiercingEnchantment(arrow, stack);
        ItemCompatHandler.applyLootingEnchantment(arrow, stack);
        return arrow;
    }

}
