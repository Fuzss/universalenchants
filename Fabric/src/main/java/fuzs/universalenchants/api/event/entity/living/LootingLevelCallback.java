package fuzs.universalenchants.api.event.entity.living;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.OptionalInt;

@FunctionalInterface
public interface LootingLevelCallback {
    Event<LootingLevelCallback> EVENT = EventFactory.createArrayBacked(LootingLevelCallback.class, listeners -> (LivingEntity entity, @Nullable DamageSource damageSource, int lootingLevel) -> {
        for (LootingLevelCallback event : listeners) {
            OptionalInt optional = event.onLootingLevel(entity, damageSource, lootingLevel);
            if (optional.isPresent()) lootingLevel = optional.getAsInt();
        }
        return OptionalInt.of(lootingLevel);
    });

    /**
     * allows modifying used looting level for calculating bonus drops when an entity is killed
     * @param entity the target entity
     * @param damageSource the damage source the target is killed by, contains killer
     * @param lootingLevel vanilla looting level
     * @return new looting level or <code>lootingLevel</code> if nothing changed
     */
    OptionalInt onLootingLevel(LivingEntity entity, @Nullable DamageSource damageSource, int lootingLevel);
}
