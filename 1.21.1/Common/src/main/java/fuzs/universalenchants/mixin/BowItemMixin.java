package fuzs.universalenchants.mixin;

import fuzs.universalenchants.handler.ItemCompatHandler;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BowItem.class)
abstract class BowItemMixin extends ProjectileWeaponItem {

    public BowItemMixin(Properties properties) {
        super(properties);
    }

    @ModifyVariable(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    public AbstractArrow releaseUsing(AbstractArrow abstractArrow, ItemStack itemStack) {
        ItemCompatHandler.applyPiercingEnchantment(abstractArrow, itemStack);
        ItemCompatHandler.applyLootingEnchantment(abstractArrow, itemStack);
        return abstractArrow;
    }
}
