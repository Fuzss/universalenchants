package fuzs.universalenchants.mixin;

import fuzs.universalenchants.handler.CompatibilityElement;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShootableItem;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@SuppressWarnings("unused")
@Mixin(CrossbowItem.class)
public abstract class CrossbowItemMixin extends ShootableItem {

    public CrossbowItemMixin(Properties builder) {

        super(builder);
    }

    @ModifyVariable(method = "hasAmmo", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;findAmmo(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;"), ordinal = 0)
    private static boolean hasInfiniteAmmo(boolean isCreativeMode, LivingEntity entityIn, ItemStack stack) {

        return isCreativeMode || EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, stack) > 0;
    }

    @Redirect(method = "fireProjectile", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/CrossbowItem;createArrow(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/entity/projectile/AbstractArrowEntity;"))
    private static AbstractArrowEntity createEnchantedArrow(World worldIn, LivingEntity shooter, ItemStack crossbow, ItemStack ammo) {

        AbstractArrowEntity abstractarrowentity = createArrow(worldIn, shooter, crossbow, ammo);
        CompatibilityElement.applyPowerEnchantment(abstractarrowentity, crossbow);
        CompatibilityElement.applyPunchEnchantment(abstractarrowentity, crossbow);
        CompatibilityElement.applyFlameEnchantment(abstractarrowentity, crossbow);
        CompatibilityElement.applyLootingEnchantment(abstractarrowentity, crossbow);
        if (EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, crossbow) > 0) {

            abstractarrowentity.pickupStatus = AbstractArrowEntity.PickupStatus.CREATIVE_ONLY;
        }

        return abstractarrowentity;
    }

    @Shadow
    private static AbstractArrowEntity createArrow(World worldIn, LivingEntity shooter, ItemStack crossbow, ItemStack ammo) {

        throw new IllegalStateException();
    }

}
