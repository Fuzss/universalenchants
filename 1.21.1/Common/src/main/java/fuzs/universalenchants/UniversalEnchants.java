package fuzs.universalenchants;

import fuzs.enchantmentcontrol.api.v1.data.EnchantmentDataHelper;
import fuzs.puzzleslib.api.config.v3.ConfigHolder;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.api.core.v1.ModLoaderEnvironment;
import fuzs.puzzleslib.api.core.v1.context.PackRepositorySourcesContext;
import fuzs.puzzleslib.api.event.v1.core.EventPhase;
import fuzs.puzzleslib.api.event.v1.entity.living.*;
import fuzs.puzzleslib.api.event.v1.entity.player.ArrowLooseCallback;
import fuzs.puzzleslib.api.event.v1.entity.player.PlayerInteractEvents;
import fuzs.puzzleslib.api.event.v1.level.BlockEvents;
import fuzs.puzzleslib.api.event.v1.server.RegisterCommandsCallback;
import fuzs.puzzleslib.api.network.v3.NetworkHandlerV3;
import fuzs.puzzleslib.api.resources.v1.DynamicPackResources;
import fuzs.puzzleslib.api.resources.v1.PackResourcesHelper;
import fuzs.universalenchants.config.ClientConfig;
import fuzs.universalenchants.config.CommonConfig;
import fuzs.universalenchants.config.ServerConfig;
import fuzs.universalenchants.data.DynamicEnchantmentTagProvider;
import fuzs.universalenchants.handler.BetterEnchantsHandler;
import fuzs.universalenchants.handler.ItemCompatHandler;
import fuzs.universalenchants.init.ModRegistry;
import fuzs.universalenchants.network.client.ServerboundSetEnchantmentsMessage;
import fuzs.universalenchants.server.commands.ModEnchantCommand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.util.Map;

public class UniversalEnchants implements ModConstructor {
    public static final String MOD_ID = "universalenchants";
    public static final String MOD_NAME = "Universal Enchants";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    public static final NetworkHandlerV3 NETWORK = NetworkHandlerV3.builder(MOD_ID).registerServerbound(
            ServerboundSetEnchantmentsMessage.class);
    public static final ConfigHolder CONFIG = ConfigHolder.builder(MOD_ID)
            .client(ClientConfig.class)
            .common(CommonConfig.class)
            .server(ServerConfig.class);

    @Override
    public void onConstructMod() {
        ModRegistry.touch();
        registerHandlers();
    }

    private static void registerHandlers() {
        RegisterCommandsCallback.EVENT.register(ModEnchantCommand::register);
        ArrowLooseCallback.EVENT.register(ItemCompatHandler::onArrowLoose);
        UseItemEvents.TICK.register(ItemCompatHandler::onUseItemTick);
        LootingLevelCallback.EVENT.register(ItemCompatHandler::onLootingLevel);
        PlayerInteractEvents.USE_ITEM.register(BetterEnchantsHandler::onUseItem);
        LivingHurtCallback.EVENT.register(BetterEnchantsHandler::onLivingHurt);
        BlockEvents.FARMLAND_TRAMPLE.register(BetterEnchantsHandler::onFarmlandTrample);
        // run after other mods had a chance to change looting level
        LivingExperienceDropCallback.EVENT.register(EventPhase.AFTER, BetterEnchantsHandler::onLivingExperienceDrop);
        BlockEvents.DROP_EXPERIENCE.register(EventPhase.AFTER, BetterEnchantsHandler::onDropExperience);
        ShieldBlockCallback.EVENT.register(ItemCompatHandler::onShieldBlock);
        LivingTickCallback.EVENT.register(BetterEnchantsHandler::onLivingTick);
    }

    @Override
    public void onAddDataPackFinders(PackRepositorySourcesContext context) {
        // on Fabric, we need to regenerate and replace incompatibility tags since we are unable to remove existing values from the tags
        // on Forge & NeoForge, this is not necessary, we can simply supply values to remove from the tags via the added 'remove' field
        // also makes sure this has pack position set to top to guarantee it being added above the Enchantment Control dynamic pack
        // update: we add the 'remove' field to tags on Fabric ourselves, so this is no longer necessary
        if (false && ModLoaderEnvironment.INSTANCE.getModLoader().isFabricLike()) {
            context.addRepositorySource(PackResourcesHelper.buildServerPack(id("enchantment_compatibilities"),
                    () -> new DynamicPackResources(DynamicEnchantmentTagProvider::new) {

                        @Override
                        protected Map<PackType, Map<ResourceLocation, IoSupplier<InputStream>>> generatePathsFromProviders() {
                            EnchantmentDataHelper.unbindAll();
                            Map<PackType, Map<ResourceLocation, IoSupplier<InputStream>>> paths = super.generatePathsFromProviders();
                            EnchantmentDataHelper.bindAll();
                            return paths;
                        }
                    },
                    false
            ));
        }
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
