package fuzs.universalenchants.neoforge.client;

import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.client.UniversalEnchantsClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = UniversalEnchants.MOD_ID, dist = Dist.CLIENT)
public class UniversalEnchantsNeoForgeClient {

    public UniversalEnchantsNeoForgeClient() {
        ClientModConstructor.construct(UniversalEnchants.MOD_ID, UniversalEnchantsClient::new);
    }
}
