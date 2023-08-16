package fuzs.universalenchants.mixin;

import com.mojang.brigadier.CommandDispatcher;
import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.config.CommonConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.EnchantCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantCommand.class)
abstract class EnchantCommandMixin {

    @Inject(method = "register", at = @At("HEAD"), cancellable = true)
    private static void register(CommandDispatcher<CommandSourceStack> dispatcher, CallbackInfo callback) {
        // we provide our own version of the command which is generally possible and will just override vanilla as it'll be registered afterwards
        // just to make sure really everything is replaced and vanilla doesn't interfere we also disable vanilla's command
        if (UniversalEnchants.CONFIG.get(CommonConfig.class).enchantCommand.replaceVanillaCommand()) callback.cancel();
    }
}
