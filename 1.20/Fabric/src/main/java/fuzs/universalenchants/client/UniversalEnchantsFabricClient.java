package fuzs.universalenchants.client;

import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.universalenchants.UniversalEnchants;
import net.fabricmc.api.ClientModInitializer;

public class UniversalEnchantsFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientModConstructor.construct(UniversalEnchants.MOD_ID, UniversalEnchantsClient::new);
    }
}
