package fuzs.universalenchants.mixin.injected;

import fuzs.universalenchants.world.item.enchantment.EnchantmentFeature;
import fuzs.universalenchants.world.item.enchantment.data.MaxLevelManager;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
abstract class EnchantmentMixin implements EnchantmentFeature {

    @Inject(method = "getMaxLevel", at = @At("HEAD"), cancellable = true, require = 0)
    public void getMaxLevel(CallbackInfoReturnable<Integer> callback) {
        MaxLevelManager.getMaxLevel(Enchantment.class.cast(this)).ifPresent(callback::setReturnValue);
    }

    @Inject(method = "getDamageProtection", at = @At("HEAD"), cancellable = true, require = 0)
    public void getDamageProtection(int level, DamageSource source, CallbackInfoReturnable<Integer> callback) {
        if (!this.universalenchants$isEnabled()) callback.setReturnValue(0);
    }

    @Inject(method = "getDamageBonus", at = @At("HEAD"), cancellable = true, require = 0)
    public void getDamageBonus(int level, MobType type, CallbackInfoReturnable<Float> callback) {
        if (!this.universalenchants$isEnabled()) callback.setReturnValue(0.0F);
    }

    @Inject(method = "canEnchant", at = @At("HEAD"), cancellable = true, require = 0)
    public void canEnchant(ItemStack stack, CallbackInfoReturnable<Boolean> callback) {
        if (!this.universalenchants$isEnabled()) callback.setReturnValue(false);
    }

    @Inject(method = "doPostAttack", at = @At("HEAD"), cancellable = true, require = 0)
    public void doPostAttack(LivingEntity attacker, Entity target, int level, CallbackInfo callback) {
        if (!this.isEnabled(target.level().enabledFeatures())) callback.cancel();
    }

    @Inject(method = "doPostHurt", at = @At("HEAD"), cancellable = true, require = 0)
    public void doPostHurt(LivingEntity target, Entity attacker, int level, CallbackInfo callback) {
        if (!this.isEnabled(target.level().enabledFeatures())) callback.cancel();
    }

    @Inject(method = "isTreasureOnly", at = @At("HEAD"), cancellable = true, require = 0)
    public void isTreasureOnly(CallbackInfoReturnable<Boolean> callback) {
        // TODO make configurable
    }

    @Inject(method = "isCurse", at = @At("HEAD"), cancellable = true, require = 0)
    public void isCurse(CallbackInfoReturnable<Boolean> callback) {
        // TODO make configurable
    }

    @Inject(method = "isTradeable", at = @At("HEAD"), cancellable = true, require = 0)
    public void isTradeable(CallbackInfoReturnable<Boolean> callback) {
        if (!this.universalenchants$isEnabled()) callback.setReturnValue(false);
    }

    @Inject(method = "isDiscoverable", at = @At("HEAD"), cancellable = true, require = 0)
    public void isDiscoverable(CallbackInfoReturnable<Boolean> callback) {
        if (!this.universalenchants$isEnabled()) callback.setReturnValue(false);
    }
}
