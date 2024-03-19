package fuzs.universalenchants.neoforge;

import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.neoforge.api.data.v2.core.DataProviderHelper;
import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.data.ModBlockTagProvider;
import fuzs.universalenchants.data.ModEnchantmentTagProvider;
import fuzs.universalenchants.data.ModItemTagProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;

@Mod(UniversalEnchants.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class UniversalEnchantsNeoForge {

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        ModConstructor.construct(UniversalEnchants.MOD_ID, UniversalEnchants::new);
        DataProviderHelper.registerDataProviders(UniversalEnchants.MOD_ID,
                ModEnchantmentTagProvider::new,
                ModItemTagProvider::new,
                ModBlockTagProvider::new
        );
    }
}
