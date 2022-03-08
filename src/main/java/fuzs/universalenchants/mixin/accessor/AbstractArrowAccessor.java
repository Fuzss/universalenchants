package fuzs.universalenchants.mixin.accessor;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractArrow.class)
public interface AbstractArrowAccessor {
    @Invoker
    ItemStack callGetArrowStack();

    @Accessor
    IntOpenHashSet getPiercedEntities();

    @Accessor
    void setPiercedEntities(IntOpenHashSet piercedEntities);

    @Accessor
    int getKnockbackStrength();
}
