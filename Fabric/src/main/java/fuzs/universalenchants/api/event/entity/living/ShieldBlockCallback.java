package fuzs.universalenchants.api.event.entity.living;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.Unit;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;

@FunctionalInterface
public interface ShieldBlockCallback {
    Event<ShieldBlockCallback> EVENT = EventFactory.createArrayBacked(ShieldBlockCallback.class, listeners -> (LivingEntity blocker, DamageSource source, float amount) -> {
        for (ShieldBlockCallback event : listeners) {
            if (event.onShieldBlock(blocker, source, amount).isPresent()) {
                return Optional.of(Unit.INSTANCE);
            }
        }
        return Optional.empty();
    });

    /**
     * Called right before damage from an incoming attack is negated via blocking using a shield.
     * <p>On Forge this event also allows to change the damage amount as well as prevent the shield from taking damage, these behaviors are not supported right now.
     *
     * @param blocker the entity that is using a shield to block an incoming attack
     * @param source the damage source attacking the <code>blocker</code>
     * @param amount the amount of damage that will be blocked
     * @return is the shield block allowed to happen
     */
    Optional<Unit> onShieldBlock(LivingEntity blocker, DamageSource source, float amount);
}
