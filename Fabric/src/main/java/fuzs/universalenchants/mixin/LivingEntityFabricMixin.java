package fuzs.universalenchants.mixin;

import fuzs.universalenchants.api.event.entity.living.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
abstract class LivingEntityFabricMixin extends Entity {
    @Shadow
    protected ItemStack useItem = ItemStack.EMPTY;
    @Shadow
    protected int useItemRemaining;
    @Shadow
    @Nullable
    protected Player lastHurtByPlayer;
    @Unique
    private float universalenchants$hurtAmount;

    public LivingEntityFabricMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "updatingUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;updateUsingItem(Lnet/minecraft/world/item/ItemStack;)V"))
    private void updatingUsingItem$invokeUpdateUsingItem(CallbackInfo callback) {
        LivingEntityUseItemEvents.TICK.invoker().onUseItemTick((LivingEntity) (Object) this, this.useItem, this.useItemRemaining).ifPresent(newDuration -> {
            this.useItemRemaining = newDuration;
        });
    }

    @ModifyVariable(method = "dropAllDeathLoot", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;lastHurtByPlayerTime:I"), ordinal = 0)
    protected int dropAllDeathLoot$storeLootingLevel(int lootingLevel, DamageSource damageSource) {
        return LootingLevelCallback.EVENT.invoker().onLootingLevel((LivingEntity) (Object) this, damageSource, lootingLevel).orElseThrow();
    }

    @Inject(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getDamageAfterArmorAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F"), cancellable = true)
    protected void actuallyHurt$invokeGetDamageAfterArmorAbsorb(DamageSource damageSource, float damageAmount, CallbackInfo callback) {
        LivingHurtCallback.EVENT.invoker().onLivingHurt((LivingEntity) (Object) this, damageSource, damageAmount).ifPresent(unit -> callback.cancel());
    }

    @Inject(method = "dropExperience", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ExperienceOrb;award(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;I)V"), cancellable = true)
    protected void dropExperience$inject$invoke(CallbackInfo callback) {
        int experienceReward = this.getExperienceReward();
        int newExperienceReward = LivingExperienceDropCallback.EVENT.invoker().onLivingExperienceDrop((LivingEntity) (Object) this, this.lastHurtByPlayer, experienceReward, experienceReward).orElseThrow();
        if (experienceReward != newExperienceReward) {
            ExperienceOrb.award((ServerLevel) this.level, this.position(), newExperienceReward);
            callback.cancel();
        }
    }

    @Shadow
    protected abstract int getExperienceReward();

    @ModifyVariable(method = "hurt", at = @At(value = "LOAD", ordinal = 1), ordinal = 0)
    public float hurt$0(float amount, DamageSource source) {
        // hook in before any blocking checks are done, there is no good way to cancel the block after this
        // check everything again, it shouldn't affect anything
        if (amount > 0.0F && this.isDamageSourceBlocked(source)) {
            if (ShieldBlockCallback.EVENT.invoker().onShieldBlock(LivingEntity.class.cast(this), source, amount).isPresent()) {
                this.universalenchants$hurtAmount = amount;
                // prevent vanilla shield logic from running when the callback was cancelled
                return 0.0F;
            }
        }
        return amount;
    }

    @Shadow
    public abstract boolean isDamageSourceBlocked(DamageSource damageSource);

    @ModifyVariable(method = "hurt", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;animationSpeed:F"), ordinal = 0)
    public float hurt$1(float amount, DamageSource source) {
        if (this.universalenchants$hurtAmount != 0.0F) {
            // restore original amount when the shield blocking callback was cancelled
            amount = this.universalenchants$hurtAmount;
            this.universalenchants$hurtAmount = 0.0F;
        }
        return amount;
    }
}
