package fuzs.universalenchants.neoforge.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(Player.class)
abstract class PlayerNeoForgeMixin extends LivingEntity {

    protected PlayerNeoForgeMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyVariable(method = "attack", at = @At("STORE"), ordinal = 3, slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getSpeed()F")))
    public boolean attack(boolean canPerformSweepAction) {
        // Forge's sweeping check has already been called here, as opposed to vanilla the variable is always set, not just when it's true
        if (canPerformSweepAction) return true;
        ItemStack itemstack = this.getItemInHand(InteractionHand.MAIN_HAND);
        return itemstack.getEnchantmentLevel(Enchantments.SWEEPING_EDGE) > 0;
    }
}
