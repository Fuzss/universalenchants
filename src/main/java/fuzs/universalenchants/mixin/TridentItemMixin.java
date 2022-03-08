package fuzs.universalenchants.mixin;

import fuzs.universalenchants.handler.CompatibilityElement;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TridentItem.class)
public abstract class TridentItemMixin extends Item {
    public TridentItemMixin(Properties p_41383_) {
        super(p_41383_);
    }

    @Redirect(method = "onPlayerStoppedUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addEntity(Lnet/minecraft/entity/Entity;)Z"))
    public boolean addEntity(World worldToAddIn, Entity tridentEntity, ItemStack stack, World worldIn2, LivingEntity itemUserEntity) {

        // add bow and crossbow enchantments
        CompatibilityElement.applyPiercingEnchantment((AbstractArrowEntity) tridentEntity, stack);
        int knockbackLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.KNOCKBACK, stack);
        if (knockbackLevel > 0) {

            ((AbstractArrow) tridentEntity).setKnockbackStrength(knockbackLevel);
        }

        return worldToAddIn.addEntity(tridentEntity);
    }

}
