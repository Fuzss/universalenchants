package fuzs.universalenchants.client.core;

import fuzs.universalenchants.client.resources.language.NumericClientLanguage;
import net.minecraft.locale.Language;

public final class FabricClientAbstractions implements ClientAbstractions {

    @Override
    public NumericClientLanguage getNumericClientLanguage() {
        return new NumericClientLanguage(Language.getInstance());
    }
}
