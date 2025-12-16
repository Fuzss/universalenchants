package fuzs.universalenchants.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
abstract class PlayerMixin extends LivingEntity {

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyReturnValue(method = "isSweepAttack", at = @At(value = "RETURN", ordinal = 0))
    public boolean isSweepAttack(boolean isSweepAttack) {
        // Sweeping is hardcoded to the swords item tag, so we check for the attribute value instead.
        // Also, all swords should still support sweeping regardless of the sweeping edge enchantment being present.
        // There is also SweepAttackEvent on NeoForge, but that does not pass all required parameters for determining whether to perform the sweep attack.
        return isSweepAttack || this.getAttributeValue(Attributes.SWEEPING_DAMAGE_RATIO) > 0.0;
    }
}
