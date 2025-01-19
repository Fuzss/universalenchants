package fuzs.universalenchants.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "blockedByShield", at = @At("HEAD"), cancellable = true)
    protected void blockedByShield(LivingEntity defender, CallbackInfo callback) {
        int level = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.KNOCKBACK, defender.getUseItem());
        LivingEntity.class.cast(this)
                .knockback(0.5 * (level + 1), defender.getX() - this.getX(), defender.getZ() - this.getZ());
    }
}
