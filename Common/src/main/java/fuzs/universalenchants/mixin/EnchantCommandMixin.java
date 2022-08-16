package fuzs.universalenchants.mixin;

import net.minecraft.server.commands.EnchantCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EnchantCommand.class)
public abstract class EnchantCommandMixin {
    @Unique
    private static int enchantmentLevel;

    @ModifyVariable(method = "enchant", at = @At("HEAD"), ordinal = 0)
    private static int enchant$modifyVariable$head(int level) {
        enchantmentLevel = level;
        return 0;
    }

    @ModifyVariable(method = "enchant", at = @At(value = "CONSTANT", args = "intValue=0", ordinal = 0), ordinal = 0)
    private static int enchant$modifyVariable$constant(int level) {
        return enchantmentLevel;
    }
}
