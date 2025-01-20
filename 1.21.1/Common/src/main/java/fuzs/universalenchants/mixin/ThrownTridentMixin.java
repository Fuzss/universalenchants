package fuzs.universalenchants.mixin;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownTrident.class)
abstract class ThrownTridentMixin extends AbstractArrow {
    @Shadow
    private boolean dealtDamage;

    protected ThrownTridentMixin(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyArg(
            method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)V"
            ),
            index = 4
    )
    private static ItemStack init(EntityType<? extends AbstractArrow> entityType, LivingEntity owner, Level level, ItemStack pickupItemStack, @Nullable ItemStack firedFromWeapon) {
        // set the weapon stack to the trident itself, so that piercing and flame may apply
        return pickupItemStack;
    }

    @Inject(
            method = "onHitEntity", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/projectile/ThrownTrident;getDeltaMovement()Lnet/minecraft/world/phys/Vec3;"
    ), cancellable = true
    )
    protected void onHitEntity(EntityHitResult hitResult, CallbackInfo callback) {
        // support piercing for tridents, since the super call to AbstractArrow is missing
        if (this.getPierceLevel() > 0) {
            if (this.piercingIgnoreEntityIds == null) {
                this.piercingIgnoreEntityIds = new IntOpenHashSet(5);
            }
            if (this.piercedAndKilledEntities == null) {
                this.piercedAndKilledEntities = Lists.<Entity>newArrayListWithCapacity(5);
            }
            this.piercingIgnoreEntityIds.add(hitResult.getEntity().getId());
            if (this.piercingIgnoreEntityIds.size() <= this.getPierceLevel()) {
                this.dealtDamage = false;
                callback.cancel();
            }
        }
    }
}
