package fuzs.universalenchants.mixin;

import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.config.ServerConfig;
import fuzs.universalenchants.world.item.enchantment.data.AdditionalEnchantmentDataProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
abstract class EnchantmentHelperMixin {

    @Inject(method = "getFireAspect", at = @At("HEAD"), cancellable = true)
    private static void getFireAspect(LivingEntity player, CallbackInfoReturnable<Integer> callback) {
        if (!UniversalEnchants.CONFIG.get(ServerConfig.class).nerfFireAspectOnTools) return;
        ItemStack stack = Enchantments.FIRE_ASPECT.getSlotItems(player).getOrDefault(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        if (!stack.isEmpty() && EnchantmentCategory.DIGGER.canEnchant(stack.getItem()) && !AdditionalEnchantmentDataProvider.AXE_ENCHANTMENT_CATEGORY.canEnchant(stack.getItem())) callback.setReturnValue(0);
    }

    @Inject(method = "getKnockbackBonus", at = @At("HEAD"), cancellable = true)
    private static void getKnockbackBonus(LivingEntity player, CallbackInfoReturnable<Integer> callback) {
        // prevent shields from applying knockback when hitting targets
        if (player.getMainHandItem().getItem() instanceof ShieldItem) callback.setReturnValue(0);
    }
}
