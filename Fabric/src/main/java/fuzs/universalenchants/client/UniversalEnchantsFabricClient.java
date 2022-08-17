package fuzs.universalenchants.client;

import com.google.common.collect.ImmutableList;
import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.client.resources.language.NumericClientLanguage;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class UniversalEnchantsFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new IdentifiableResourceReloadListener() {

            @Override
            public ResourceLocation getFabricId() {
                return new ResourceLocation(UniversalEnchants.MOD_ID, "roman_numerals_language");
            }

            @Override
            public Collection<ResourceLocation> getFabricDependencies() {
                return ImmutableList.of(ResourceReloadListenerKeys.LANGUAGES);
            }

            @Override
            public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller profilerFiller, ProfilerFiller profilerFiller2, Executor executor, Executor executor2) {
                return preparationBarrier.wait(Unit.INSTANCE).thenRunAsync(NumericClientLanguage::injectLanguage, executor2);
            }
        });
    }
}
