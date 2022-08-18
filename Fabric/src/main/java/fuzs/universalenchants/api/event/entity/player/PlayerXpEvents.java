package fuzs.universalenchants.api.event.entity.player;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

public final class PlayerXpEvents {
    public static final Event<PickupXp> PICKUP_XP = EventFactory.createArrayBacked(PickupXp.class, listeners -> (Player player, ExperienceOrb orb) -> {
        for (PickupXp event : listeners) {
            if (event.onPickupXp(player, orb).isPresent()) {
                return Optional.of(Unit.INSTANCE);
            }
        }
        return Optional.empty();
    });

    public interface PickupXp {

        /**
         * called when a player collides with an {@link ExperienceOrb} entity, just before it is added to the player
         * (either as levels or for repairing mending gear)
         *
         * @param player    the player colliding with the orb
         * @param orb       the orb that's being collided with
         * @return          should the player collect the orb
         */
        Optional<Unit> onPickupXp(Player player, ExperienceOrb orb);
    }
}
