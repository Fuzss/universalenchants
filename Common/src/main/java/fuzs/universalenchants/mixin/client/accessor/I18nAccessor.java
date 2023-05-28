package fuzs.universalenchants.mixin.client.accessor;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.locale.Language;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(I18n.class)
public interface I18nAccessor {

    @Invoker("setLanguage")
    static void universalenchants$callSetLanguage(Language language) {
        throw new RuntimeException();
    }
}
