package fuzs.universalenchants.mixin;

import fuzs.universalenchants.api.event.entity.living.LivingEntityUseItemEvents;
import fuzs.universalenchants.api.event.entity.living.LivingExperienceDropCallback;
import fuzs.universalenchants.api.event.entity.living.LivingHurtCallback;
import fuzs.universalenchants.api.event.entity.living.LootingLevelCallback;
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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.OptionalInt;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow
    protected ItemStack useItem = ItemStack.EMPTY;
    @Shadow
    protected int useItemRemaining;
    @Shadow
    @Nullable
    protected Player lastHurtByPlayer;

    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "updatingUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;updateUsingItem(Lnet/minecraft/world/item/ItemStack;)V"))
    private void updatingUsingItem$invokeUpdateUsingItem(CallbackInfo callbackInfo) {
        LivingEntityUseItemEvents.TICK.invoker().onUseItemTick((LivingEntity) (Object) this, this.useItem, this.useItemRemaining).ifPresent(newDuration -> {
            this.useItemRemaining = newDuration;
        });
    }

    @ModifyVariable(method = "dropAllDeathLoot", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;lastHurtByPlayerTime:I"), ordinal = 0)
    protected int dropAllDeathLoot$storeLootingLevel(int lootingLevel, DamageSource damageSource) {
        return LootingLevelCallback.EVENT.invoker().onLootingLevel((LivingEntity) (Object) this, damageSource, lootingLevel).orElseThrow();
    }

    @Inject(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getDamageAfterArmorAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F"), cancellable = true)
    protected void actuallyHurt$invokeGetDamageAfterArmorAbsorb(DamageSource damageSource, float damageAmount, CallbackInfo callbackInfo) {
        if (LivingHurtCallback.EVENT.invoker().onLivingHurt((LivingEntity) (Object) this, damageSource, damageAmount).isPresent()) callbackInfo.cancel();
    }

    @Inject(method = "dropExperience", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ExperienceOrb;award(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;I)V"), cancellable = true)
    protected void dropExperience(CallbackInfo callbackInfo) {
        int experienceReward = this.getExperienceReward(this.lastHurtByPlayer);
        int newExperienceReward = LivingExperienceDropCallback.EVENT.invoker().onLivingExperienceDrop((LivingEntity) (Object) this, this.lastHurtByPlayer, experienceReward, experienceReward).orElseThrow();
        if (experienceReward != newExperienceReward) {
            ExperienceOrb.award((ServerLevel) this.level, this.position(), experienceReward);
            callbackInfo.cancel();
        }
    }

    @Shadow
    protected abstract int getExperienceReward(Player player);
}
