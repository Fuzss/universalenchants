package fuzs.universalenchants.mixin;

import fuzs.universalenchants.api.event.entity.living.LootingLevelCallback;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootingEnchantFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LootingEnchantFunction.class)
abstract class LootingEnchantFunctionFabricMixin {

    @ModifyVariable(method = "run", at = @At("STORE"), ordinal = 0)
    public int run$modifyVariable$store$lootingLevel(int lootingLevel, ItemStack itemStack, LootContext lootContext) {
        Entity target = lootContext.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (!(target instanceof LivingEntity livingEntity)) return lootingLevel;
        DamageSource damageSource = lootContext.getParamOrNull(LootContextParams.DAMAGE_SOURCE);
        return LootingLevelCallback.EVENT.invoker().onLootingLevel(livingEntity, damageSource, lootingLevel).orElseThrow();
    }
}
