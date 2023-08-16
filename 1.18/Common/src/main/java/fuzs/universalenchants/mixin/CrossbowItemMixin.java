package fuzs.universalenchants.mixin;

import fuzs.universalenchants.core.CommonAbstractions;
import fuzs.universalenchants.handler.ItemCompatHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrossbowItem.class)
public abstract class CrossbowItemMixin extends ProjectileWeaponItem {

    public CrossbowItemMixin(Properties builder) {
        super(builder);
    }

    @ModifyVariable(method = "tryLoadProjectiles", at = @At("STORE"), ordinal = 0)
    private static boolean tryLoadProjectiles$storeHasInfiniteAmmo(boolean hasInfiniteAmmo, LivingEntity entityIn, ItemStack stack) {
        if (hasInfiniteAmmo) return true;
        ItemStack arrowStack = entityIn.getProjectile(stack);
        if (arrowStack.isEmpty() || CommonAbstractions.INSTANCE.isArrowInfinite(entityIn, stack, arrowStack)) {
            return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0;
        }
        return false;
    }

    @Inject(method = "getArrow", at = @At("TAIL"))
    private static void getArrow$tail(Level level, LivingEntity entity, ItemStack stack, ItemStack arrowStack, CallbackInfoReturnable<AbstractArrow> callback) {
        AbstractArrow abstractarrowentity = callback.getReturnValue();
        ItemCompatHandler.applyPowerEnchantment(abstractarrowentity, stack);
        ItemCompatHandler.applyPunchEnchantment(abstractarrowentity, stack);
        ItemCompatHandler.applyFlameEnchantment(abstractarrowentity, stack);
        ItemCompatHandler.applyLootingEnchantment(abstractarrowentity, stack);
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0 && CommonAbstractions.INSTANCE.isArrowInfinite(entity, stack, arrowStack)) {
            abstractarrowentity.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        }
    }
}
