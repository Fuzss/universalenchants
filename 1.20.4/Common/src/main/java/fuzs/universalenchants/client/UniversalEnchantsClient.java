package fuzs.universalenchants.client;

import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.core.v1.context.AddReloadListenersContext;
import fuzs.universalenchants.client.resources.language.NumericClientLanguage;

public class UniversalEnchantsClient implements ClientModConstructor {

    @Override
    public void onRegisterResourcePackReloadListeners(AddReloadListenersContext context) {
        context.registerReloadListener("roman_numerals", NumericClientLanguage::injectLanguage);
    }
}
