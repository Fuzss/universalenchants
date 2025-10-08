package fuzs.universalenchants.neoforge;

import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.neoforge.api.data.v2.core.DataProviderHelper;
import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.data.tags.ModBlockTagProvider;
import fuzs.universalenchants.data.ModDatapackRegistriesProvider;
import fuzs.universalenchants.data.tags.ModEnchantmentTagProvider;
import fuzs.universalenchants.data.tags.ModItemTagProvider;
import fuzs.universalenchants.handler.BetterEnchantsHandler;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;

@Mod(UniversalEnchants.MOD_ID)
public class UniversalEnchantsNeoForge {

    public UniversalEnchantsNeoForge() {
        ModConstructor.construct(UniversalEnchants.MOD_ID, UniversalEnchants::new);
        registerEventHandlers(NeoForge.EVENT_BUS);
        DataProviderHelper.registerDataProviders(UniversalEnchants.MOD_ID,
                ModDatapackRegistriesProvider::new,
                ModItemTagProvider::new,
                ModBlockTagProvider::new,
                ModEnchantmentTagProvider::new);
    }

    private static void registerEventHandlers(IEventBus eventBus) {
        eventBus.addListener((final BlockEvent.FarmlandTrampleEvent evt) -> {
            if (!(evt.getLevel() instanceof ServerLevel serverLevel)) return;
            if (BetterEnchantsHandler.onFarmlandTrample(serverLevel,
                    evt.getPos(),
                    evt.getState(),
                    evt.getFallDistance(),
                    evt.getEntity()).isInterrupt()) {
                evt.setCanceled(true);
            }
        });
    }
}
