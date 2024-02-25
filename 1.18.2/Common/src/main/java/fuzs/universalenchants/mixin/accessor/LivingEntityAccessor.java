package fuzs.universalenchants.mixin.accessor;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {

    @Invoker
    boolean callIsAlwaysExperienceDropper();

    @Invoker
    boolean callShouldDropExperience();

    @Invoker
    int callGetExperienceReward(Player player);

    @Accessor
    int getLastHurtByPlayerTime();

    @Nullable
    @Accessor
    Player getLastHurtByPlayer();
}
