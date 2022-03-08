package fuzs.universalenchants;

import fuzs.puzzleslib.config.AbstractConfig;
import fuzs.puzzleslib.config.ConfigHolder;
import fuzs.puzzleslib.config.ConfigHolderImpl;
import fuzs.universalenchants.config.ServerConfig;
import fuzs.universalenchants.handler.EnchantCompatHandler;
import fuzs.universalenchants.handler.TrueInfinityHandler;
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
        registerHandlers();
        ModRegistry.touch();
        CONFIG.addServerCallback(EnchantCompatHandler.INSTANCE::init);
    }

    private static void registerHandlers() {
        TrueInfinityHandler trueInfinityHandler = new TrueInfinityHandler();
        MinecraftForge.EVENT_BUS.addListener(trueInfinityHandler::onArrowNock);
        MinecraftForge.EVENT_BUS.addListener(trueInfinityHandler::onRightClickItem);
        // run after other mods had a chance to change looting level
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOW, trueInfinityHandler::onLootingLevel);
    }
}
