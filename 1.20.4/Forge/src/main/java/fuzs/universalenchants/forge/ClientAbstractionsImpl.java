package fuzs.universalenchants.forge;

import fuzs.universalenchants.client.resources.language.NumericClientLanguage;
import net.minecraft.locale.Language;

import java.util.Map;

public final class ClientAbstractionsImpl {

    public static NumericClientLanguage getNumericClientLanguage() {
        // store this, will be the instance from here when Language::getLanguageData is called
        Language language = Language.getInstance();
        return new NumericClientLanguage(Language.getInstance()) {

            @Override
            public Map<String, String> getLanguageData() {
                return language.getLanguageData();
            }
        };
    }
}
