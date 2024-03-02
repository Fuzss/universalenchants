package fuzs.universalenchants;

import dev.architectury.injectables.annotations.ExpectPlatform;
import fuzs.universalenchants.client.resources.language.NumericClientLanguage;

public final class ClientAbstractions {

    @ExpectPlatform
    public static NumericClientLanguage getNumericClientLanguage() {
        throw new RuntimeException();
    }
}
