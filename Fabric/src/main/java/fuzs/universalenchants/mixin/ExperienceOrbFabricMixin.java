package fuzs.universalenchants.mixin;

import fuzs.universalenchants.api.event.entity.player.PlayerXpEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceOrb.class)
abstract class ExperienceOrbFabricMixin extends Entity {

    public ExperienceOrbFabricMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "playerTouch", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Player;takeXpDelay:I", ordinal = 1), cancellable = true)
    public void playerTouch$inject$field$takeXpDelay(Player player, CallbackInfo callback) {
        PlayerXpEvents.PICKUP_XP.invoker().onPickupXp(player, (ExperienceOrb) (Object) this).ifPresent(unit -> callback.cancel());
    }
}
