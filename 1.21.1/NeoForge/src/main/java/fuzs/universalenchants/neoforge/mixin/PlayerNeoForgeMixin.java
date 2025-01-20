package fuzs.universalenchants.neoforge.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
abstract class PlayerNeoForgeMixin extends LivingEntity {

    protected PlayerNeoForgeMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyExpressionValue(
            method = "attack", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;canPerformAction(Lnet/neoforged/neoforge/common/ItemAbility;)Z"
    )
    )
    public boolean attack(boolean isSweepingSupported) {
        // we cannot use the NeoForge event for controlling sweeping attacks, as it ignores all the requirements vanilla imposes,
        // which cannot easily be checked again
        return isSweepingSupported || this.getAttributeValue(Attributes.SWEEPING_DAMAGE_RATIO) > 0.0;
    }
}
