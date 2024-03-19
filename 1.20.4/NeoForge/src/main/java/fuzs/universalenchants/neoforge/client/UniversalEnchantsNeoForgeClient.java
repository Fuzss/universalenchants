package fuzs.universalenchants.neoforge.client;

import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.neoforge.api.data.v2.core.DataProviderHelper;
import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.client.UniversalEnchantsClient;
import fuzs.universalenchants.data.client.ModLanguageProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;

@Mod.EventBusSubscriber(modid = UniversalEnchants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class UniversalEnchantsNeoForgeClient {

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        ClientModConstructor.construct(UniversalEnchants.MOD_ID, UniversalEnchantsClient::new);
        DataProviderHelper.registerDataProviders(UniversalEnchants.MOD_ID,
                ModLanguageProvider::new
        );
    }
}
