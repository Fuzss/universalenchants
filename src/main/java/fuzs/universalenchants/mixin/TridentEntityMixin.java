package fuzs.universalenchants.mixin;

import fuzs.universalenchants.mixin.accessor.AbstractArrowAccessor;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownTrident.class)
public abstract class TridentEntityMixin extends AbstractArrow {
    @Shadow
    private ItemStack thrownStack;
    @Shadow
    private boolean dealtDamage;

    protected TridentEntityMixin(EntityType<? extends AbstractArrow> p_36721_, Level p_36722_) {
        super(p_36721_, p_36722_);
    }

    @Inject(method = "onEntityHit", at = @At("TAIL"))
    protected void onEntityHit(EntityRayTraceResult rayTraceResult, CallbackInfo callbackInfo) {
        
        Entity target = rayTraceResult.getEntity();
        if (target instanceof LivingEntity) {

            int knockbackStrength = ((AbstractArrowAccessor) this).getKnockbackStrength();
            if (knockbackStrength > 0) {

                // copied from punch behavior
                Vector3d vector3d = this.getMotion().mul(1.0, 0.0, 1.0).normalize().scale(knockbackStrength * 0.6);
                if (vector3d.lengthSquared() > 0.0) {

                    target.addVelocity(vector3d.x, 0.1, vector3d.z);
                }
            }
        }
        
        if (this.applyPiercing(target)) {

            this.dealtDamage = false;
        }
    }
    
    private boolean applyPiercing(Entity target) {

        int pierceLevel = this.getPierceLevel();
        if (pierceLevel > 0) {

            IntOpenHashSet piercedEntities = ((AbstractArrowAccessor) this).getPiercedEntities();
            if (piercedEntities == null) {

                piercedEntities = new IntOpenHashSet(5);
                ((AbstractArrowAccessor) this).setPiercedEntities(piercedEntities);
            }

            piercedEntities.add(target.getEntityId());
            if (piercedEntities.size() <= pierceLevel) {

                // reverting previous motion change
                this.setMotion(this.getMotion().mul(-100.0, -10.0, -100.0));

                return true;
            }
        }

        return false;
    }
}
