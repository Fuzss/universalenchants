package fuzs.universalenchants.api.event.entity.living;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.OptionalInt;

@FunctionalInterface
public interface LivingExperienceDropCallback {
    Event<LivingExperienceDropCallback> EVENT = EventFactory.createArrayBacked(LivingExperienceDropCallback.class, listeners -> (LivingEntity entity, @Nullable Player attackingPlayer, int originalExperience, int droppedExperience) -> {
        for (LivingExperienceDropCallback event : listeners) {
            OptionalInt optional = event.onLivingExperienceDrop(entity, attackingPlayer, originalExperience, droppedExperience);
            if (optional.isPresent()) droppedExperience = optional.getAsInt();
        }
        return OptionalInt.of(droppedExperience);
    });

    /**
     * called right before xp drops are spawned in the world
     * @param entity the entity that died
     * @param attackingPlayer the player that killed <code>entity</code>
     * @param originalExperience amount of xp dropped by vanilla
     * @param droppedExperience current amount of xp as changed by callbacks
     * @return the amount of xp that should be dropped, if nothing is supposed to change return <code>droppedExperience</code>, not <code>originalExperience</code> since it is only for reference
     */
    OptionalInt onLivingExperienceDrop(LivingEntity entity, @Nullable Player attackingPlayer, int originalExperience, int droppedExperience);
}
