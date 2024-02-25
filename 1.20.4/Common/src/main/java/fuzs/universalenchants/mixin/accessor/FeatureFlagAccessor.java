package fuzs.universalenchants.mixin.accessor;

import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagUniverse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FeatureFlag.class)
public interface FeatureFlagAccessor {

    @Invoker("<init>")
    static FeatureFlag universalenchants$callInit(FeatureFlagUniverse universe, int maskBit) {
        throw new RuntimeException();
    }
}
