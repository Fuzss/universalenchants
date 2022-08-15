package fuzs.universalenchants;

import fuzs.puzzleslib.config.ConfigHolder;
import fuzs.puzzleslib.core.CoreServices;
import fuzs.puzzleslib.core.ModConstructor;
import fuzs.universalenchants.config.ServerConfig;
import fuzs.universalenchants.handler.EnchantCompatManager;
import fuzs.universalenchants.handler.ItemCompatManager;
import fuzs.universalenchants.init.ModRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UniversalEnchants implements ModConstructor {
    public static final String MOD_ID = "universalenchants";
    public static final String MOD_NAME = "Universal Enchants";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    @SuppressWarnings("Convert2MethodRef")
    public static final ConfigHolder CONFIG = CoreServices.FACTORIES.serverConfig(ServerConfig.class, () -> new ServerConfig());

    @Override
    public void onConstructMod() {
        CONFIG.bakeConfigs(MOD_ID);
        ModRegistry.touch();
        CONFIG.getHolder(ServerConfig.class).accept(EnchantCompatManager.INSTANCE::init);
        CONFIG.getHolder(ServerConfig.class).accept(ItemCompatManager.INSTANCE::buildData);
    }
}
