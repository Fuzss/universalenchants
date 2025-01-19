package fuzs.universalenchants;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class CommonAbstractions {

    @ExpectPlatform
    public static boolean isArrowInfinite(LivingEntity entity, ItemStack rangedStack, ItemStack arrowStack) {
        throw new RuntimeException();
    }
}
