package fuzs.universalenchants.client.core;

import fuzs.puzzleslib.api.core.v1.ServiceProviderHelper;
import fuzs.universalenchants.client.resources.language.NumericClientLanguage;

public interface ClientAbstractions {
    ClientAbstractions INSTANCE = ServiceProviderHelper.load(ClientAbstractions.class);

    NumericClientLanguage getNumericClientLanguage();
}
