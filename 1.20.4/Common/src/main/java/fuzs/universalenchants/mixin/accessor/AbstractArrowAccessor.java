package fuzs.universalenchants.mixin.accessor;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractArrow.class)
public interface AbstractArrowAccessor {

    @Accessor("piercingIgnoreEntityIds")
    IntOpenHashSet universalenchants$getPiercingIgnoreEntityIds();

    @Accessor("piercingIgnoreEntityIds")
    void universalenchants$setPiercingIgnoreEntityIds(IntOpenHashSet piercingIgnoreEntityIds);

    @Accessor("knockback")
    int universalenchants$getKnockback();
}
