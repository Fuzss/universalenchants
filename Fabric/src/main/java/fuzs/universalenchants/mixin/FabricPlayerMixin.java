package fuzs.universalenchants.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Player.class)
public abstract class FabricPlayerMixin extends LivingEntity {

    protected FabricPlayerMixin(EntityType<? extends LivingEntity> p_20966_, Level p_20967_) {
        super(p_20966_, p_20967_);
    }

    @ModifyVariable(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"), ordinal = 3)
    public boolean attack$modifyVariable$invoke(boolean canPerformSweepAction) {
        // the injection we use on Forge would only be called when sweeping is allowed (which doesn't work for us since we want to enable additional items),
        // so we need this alternative mixin
        // this will only be called when sweeping is allowed to happen and only the check for SwordItem is missing, since that's the behavior we want to change
        // next line should always be false since the variable is not modified before this, but just keep it to be safe with other mods
        if (canPerformSweepAction) return true;
        // we only handle sweeping edge case, all swords independent of enchantment are still handled by vanilla after this
        ItemStack itemstack = this.getItemInHand(InteractionHand.MAIN_HAND);
        return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SWEEPING_EDGE, itemstack) > 0;
    }
}
