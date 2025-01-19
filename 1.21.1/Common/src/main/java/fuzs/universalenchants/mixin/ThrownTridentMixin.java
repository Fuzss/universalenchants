package fuzs.universalenchants.mixin;

import fuzs.universalenchants.mixin.accessor.AbstractArrowAccessor;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownTrident.class)
abstract class ThrownTridentMixin extends AbstractArrow {
    @Shadow
    private boolean dealtDamage;

    protected ThrownTridentMixin(EntityType<? extends AbstractArrow> entityType, Level level, ItemStack pickupItemStack) {
        super(entityType, level, pickupItemStack);
    }

    @Inject(method = "onHitEntity", at = @At("TAIL"))
    protected void onHitEntity(EntityHitResult hitResult, CallbackInfo callback) {
        Entity entity = hitResult.getEntity();
        if (entity instanceof LivingEntity) {
            int knockbackStrength = ((AbstractArrowAccessor) this).universalenchants$getKnockback();
            if (knockbackStrength > 0) {
                // copied from punch behavior, motion multiplier is adjusted since trident is already stopped by vanilla code running before this
                Vec3 vector3d = this.getDeltaMovement()
                        .multiply(-100.0, 0.0, -100.0)
                        .normalize()
                        .scale(knockbackStrength * 0.6);
                if (vector3d.lengthSqr() > 0.0) {
                    entity.push(vector3d.x, 0.1, vector3d.z);
                }
            }
        }
        this.applyPiercing(entity);
    }

    @Unique
    private void applyPiercing(Entity entity) {
        int pierceLevel = this.getPierceLevel();
        if (pierceLevel > 0) {
            IntOpenHashSet piercedEntities = ((AbstractArrowAccessor) this).universalenchants$getPiercingIgnoreEntityIds();
            if (piercedEntities == null) {
                piercedEntities = new IntOpenHashSet(5);
                ((AbstractArrowAccessor) this).universalenchants$setPiercingIgnoreEntityIds(piercedEntities);
            }
            piercedEntities.add(entity.getId());
            // reverting previous motion change
            if (piercedEntities.size() <= pierceLevel) {
                this.setDeltaMovement(this.getDeltaMovement().multiply(-100.0, -10.0, -100.0));
                this.dealtDamage = false;
            }
        }
    }
}
