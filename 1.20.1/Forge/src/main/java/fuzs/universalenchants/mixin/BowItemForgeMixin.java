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
abstract class BowItemForgeMixin extends ProjectileWeaponItem {

    public BowItemForgeMixin(Properties properties) {
        super(properties);
    }

    @ModifyVariable(method = "releaseUsing", at = @At("STORE"), ordinal = 0, slice = @Slice(to = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BowItem;customArrow(Lnet/minecraft/world/entity/projectile/AbstractArrow;)Lnet/minecraft/world/entity/projectile/AbstractArrow;", remap = false)))
    public AbstractArrow releaseUsing(AbstractArrow arrow, ItemStack stack) {
        // Forge patches in another method that overrides the arrow again, we only want to apply once
        ItemCompatHandler.applyPiercingEnchantment(arrow, stack);
        ItemCompatHandler.applyLootingEnchantment(arrow, stack);
        return arrow;
    }

}
