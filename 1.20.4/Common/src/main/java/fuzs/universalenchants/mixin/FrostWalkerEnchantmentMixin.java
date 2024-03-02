package fuzs.universalenchants.mixin;

import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.config.ServerConfig;
import fuzs.universalenchants.handler.BetterEnchantsHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.FrostWalkerEnchantment;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FrostWalkerEnchantment.class)
abstract class FrostWalkerEnchantmentMixin extends Enchantment {

    protected FrostWalkerEnchantmentMixin(Rarity rarity, EnchantmentCategory category, EquipmentSlot[] applicableSlots) {
        super(rarity, category, applicableSlots);
    }

    @Inject(method = "onEntityMoved", at = @At("HEAD"), cancellable = true)
    private static void onEntityMoved(LivingEntity living, Level level, BlockPos pos, int levelConflicting, CallbackInfo callback) {
        if (!UniversalEnchants.CONFIG.get(ServerConfig.class).betterFrostWalker) return;
        BetterEnchantsHandler.onEntityMoved(living, level, pos, levelConflicting);
        callback.cancel();
    }
}
