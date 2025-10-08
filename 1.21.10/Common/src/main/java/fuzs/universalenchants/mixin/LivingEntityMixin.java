package fuzs.universalenchants.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "getKnockback", at = @At("HEAD"), cancellable = true)
    protected void getKnockback(Entity attacker, DamageSource damageSource, CallbackInfoReturnable<Float> callback) {
        if (this.getWeaponItem() != null && this.getWeaponItem().getItem() instanceof ShieldItem) {
            // prevent shields from applying knockback when hitting targets by setting the knockback value without any enchantment effects applied
            callback.setReturnValue((float) this.getAttributeValue(Attributes.ATTACK_KNOCKBACK));
        }
    }

    @Shadow
    public abstract double getAttributeValue(Holder<Attribute> attribute);
}
