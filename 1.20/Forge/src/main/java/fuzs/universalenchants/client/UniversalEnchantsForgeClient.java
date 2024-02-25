package fuzs.universalenchants.client;

import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.client.resources.language.NumericClientLanguage;
import net.minecraft.locale.Language;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.concurrent.Executor;

@Mod.EventBusSubscriber(modid = UniversalEnchants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class UniversalEnchantsForgeClient {

    @SubscribeEvent
    public static void onRegisterClientReloadListeners(final RegisterClientReloadListenersEvent evt) {
        evt.registerReloadListener((PreparableReloadListener.PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller profilerFiller, ProfilerFiller profilerFiller2, Executor executor, Executor executor2) -> {
            return preparationBarrier.wait(Unit.INSTANCE).thenRunAsync(() -> {
                // store this, will be the instance from here when Language::getLanguageData is called
                Language language = Language.getInstance();
                NumericClientLanguage.injectLanguage(new NumericClientLanguage(language) {

                    @Override
                    public Map<String, String> getLanguageData() {
                        return language.getLanguageData();
                    }
                });
            }, executor2);
        });
    }
}
