package fuzs.universalenchants;

import fuzs.puzzleslib.api.config.v3.ConfigHolder;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.api.core.v1.utility.ResourceLocationHelper;
import fuzs.puzzleslib.api.event.v1.core.EventPhase;
import fuzs.puzzleslib.api.event.v1.entity.living.*;
import fuzs.puzzleslib.api.event.v1.level.BlockEvents;
import fuzs.puzzleslib.api.event.v1.server.TagsUpdatedCallback;
import fuzs.universalenchants.config.ServerConfig;
import fuzs.universalenchants.handler.BetterEnchantsHandler;
import fuzs.universalenchants.handler.ItemCompatHandler;
import fuzs.universalenchants.init.ModRegistry;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UniversalEnchants implements ModConstructor {
    public static final String MOD_ID = "universalenchants";
    public static final String MOD_NAME = "Universal Enchants";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    public static final ConfigHolder CONFIG = ConfigHolder.builder(MOD_ID).server(ServerConfig.class);

    @Override
    public void onConstructMod() {
        ModRegistry.bootstrap();
        registerEventHandlers();
    }

    private static void registerEventHandlers() {
        TagsUpdatedCallback.EVENT.register(ItemCompatHandler::onTagsUpdated);
        UseItemEvents.TICK.register(ItemCompatHandler::onUseItemTick);
        ComputeEnchantedLootBonusCallback.EVENT.register(ItemCompatHandler::onComputeEnchantedLootBonus);
        LivingHurtCallback.EVENT.register(BetterEnchantsHandler::onLivingHurt);
        BlockEvents.FARMLAND_TRAMPLE.register(BetterEnchantsHandler::onFarmlandTrample);
        ShieldBlockCallback.EVENT.register(ItemCompatHandler::onShieldBlock);
        // run after other mods had a chance to change looting level
        LivingExperienceDropCallback.EVENT.register(EventPhase.AFTER, BetterEnchantsHandler::onLivingExperienceDrop);
        BlockEvents.DROP_EXPERIENCE.register(EventPhase.AFTER, BetterEnchantsHandler::onDropExperience);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocationHelper.fromNamespaceAndPath(MOD_ID, path);
    }
}
