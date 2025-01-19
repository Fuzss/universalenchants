package fuzs.universalenchants.fabric;

import fuzs.universalenchants.client.resources.language.NumericClientLanguage;
import net.minecraft.locale.Language;

public final class ClientAbstractionsImpl  {

    public static NumericClientLanguage getNumericClientLanguage() {
        return new NumericClientLanguage(Language.getInstance());
    }
}
