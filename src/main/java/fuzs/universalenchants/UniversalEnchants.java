package fuzs.universalenchants;

import fuzs.puzzleslib.config.AbstractConfig;
import fuzs.puzzleslib.config.ConfigHolder;
import fuzs.puzzleslib.config.ConfigHolderImpl;
import fuzs.universalenchants.config.ServerConfig;
import fuzs.universalenchants.handler.BetterEnchantsHandler;
import fuzs.universalenchants.handler.EnchantCompatManager;
import fuzs.universalenchants.handler.ItemCompatHandler;
import fuzs.universalenchants.handler.ItemCompatManager;
import fuzs.universalenchants.registry.ModRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(UniversalEnchants.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class UniversalEnchants {
    public static final String MOD_ID = "universalenchants";
    public static final String MOD_NAME = "Universal Enchants";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    @SuppressWarnings("Convert2MethodRef")
    public static final ConfigHolder<AbstractConfig, ServerConfig> CONFIG = ConfigHolder.server(() -> new ServerConfig());

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        ((ConfigHolderImpl<?, ?>) CONFIG).addConfigs(MOD_ID);
        ModRegistry.touch();
        registerHandlers();
        CONFIG.addServerCallback(EnchantCompatManager.INSTANCE::init);
        CONFIG.addServerCallback(ItemCompatManager.INSTANCE::buildData);
    }

    private static void registerHandlers() {
        ItemCompatHandler itemCompatHandler = new ItemCompatHandler();
        MinecraftForge.EVENT_BUS.addListener(itemCompatHandler::onArrowLoose);
        MinecraftForge.EVENT_BUS.addListener(itemCompatHandler::onItemUseTick);
        MinecraftForge.EVENT_BUS.addListener(itemCompatHandler::onLootingLevel);
        BetterEnchantsHandler betterEnchantsHandler = new BetterEnchantsHandler();
        MinecraftForge.EVENT_BUS.addListener(betterEnchantsHandler::onArrowNock);
        MinecraftForge.EVENT_BUS.addListener(betterEnchantsHandler::onRightClickItem);
        MinecraftForge.EVENT_BUS.addListener(betterEnchantsHandler::onLivingHurt);
        MinecraftForge.EVENT_BUS.addListener(betterEnchantsHandler::onFarmlandTrample);
        // run after other mods had a chance to change looting level
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOW, betterEnchantsHandler::onLivingExperienceDrop);
    }
}
