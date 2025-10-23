package fuzs.universalenchants;

import fuzs.puzzleslib.api.config.v3.ConfigHolder;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.api.core.v1.context.PackRepositorySourcesContext;
import fuzs.puzzleslib.api.core.v1.utility.ResourceLocationHelper;
import fuzs.puzzleslib.api.event.v1.FinalizeItemComponentsCallback;
import fuzs.puzzleslib.api.event.v1.core.EventPhase;
import fuzs.puzzleslib.api.event.v1.entity.living.*;
import fuzs.puzzleslib.api.event.v1.level.BlockEvents;
import fuzs.puzzleslib.api.event.v1.server.TagsUpdatedCallback;
import fuzs.universalenchants.config.ServerConfig;
import fuzs.universalenchants.handler.BetterEnchantsHandler;
import fuzs.universalenchants.handler.ItemCompatHandler;
import fuzs.universalenchants.init.ModRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UniversalEnchants implements ModConstructor {
    public static final String MOD_ID = "universalenchants";
    public static final String MOD_NAME = "Universal Enchants";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    public static final ConfigHolder CONFIG = ConfigHolder.builder(MOD_ID).server(ServerConfig.class);
    public static final ResourceLocation COMPATIBLE_DAMAGE_ENCHANTMENTS_LOCATION = id("compatible_damage_enchantments");
    public static final ResourceLocation COMPATIBLE_PROTECTION_ENCHANTMENTS_LOCATION = id(
            "compatible_protection_enchantments");

    @Override
    public void onConstructMod() {
        ModRegistry.bootstrap();
        registerEventHandlers();
    }

    private static void registerEventHandlers() {
        FinalizeItemComponentsCallback.EVENT.register(ItemCompatHandler::onFinalizeItemComponents);
        TagsUpdatedCallback.EVENT.register(ItemCompatHandler::onTagsUpdated);
        UseItemEvents.TICK.register(ItemCompatHandler::onUseItemTick);
        LivingHurtCallback.EVENT.register(BetterEnchantsHandler::onLivingHurt);
        PickProjectileCallback.EVENT.register(BetterEnchantsHandler::onPickProjectile);
        ShieldBlockCallback.EVENT.register(ItemCompatHandler::onShieldBlock);
        // run after other mods had a chance to change looting level
        LivingExperienceDropCallback.EVENT.register(EventPhase.AFTER, BetterEnchantsHandler::onLivingExperienceDrop);
        BlockEvents.DROP_EXPERIENCE.register(EventPhase.AFTER, BetterEnchantsHandler::onDropExperience);
    }

    @Override
    public void onAddDataPackFinders(PackRepositorySourcesContext context) {
        context.registerBuiltInPack(COMPATIBLE_DAMAGE_ENCHANTMENTS_LOCATION,
                Component.literal("Compatible Damage Enchantments"),
                false);
        context.registerBuiltInPack(COMPATIBLE_PROTECTION_ENCHANTMENTS_LOCATION,
                Component.literal("Compatible Protection Enchantments"),
                false);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocationHelper.fromNamespaceAndPath(MOD_ID, path);
    }
}
