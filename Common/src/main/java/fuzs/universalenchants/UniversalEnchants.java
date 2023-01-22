package fuzs.universalenchants;

import fuzs.puzzleslib.config.ConfigHolder;
import fuzs.puzzleslib.core.CoreServices;
import fuzs.puzzleslib.core.ModConstructor;
import fuzs.universalenchants.config.ClientConfig;
import fuzs.universalenchants.config.CommonConfig;
import fuzs.universalenchants.config.ServerConfig;
import fuzs.universalenchants.init.ModRegistry;
import fuzs.universalenchants.server.commands.ModEnchantCommand;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UniversalEnchants implements ModConstructor {
    public static final String MOD_ID = "universalenchants";
    public static final String MOD_NAME = "Universal Enchants";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    @SuppressWarnings("Convert2MethodRef")
    public static final ConfigHolder CONFIG = CoreServices.FACTORIES
            .clientConfig(ClientConfig.class, () -> new ClientConfig())
            .commonConfig(CommonConfig.class, () -> new CommonConfig())
            .serverConfig(ServerConfig.class, () -> new ServerConfig());

    @Override
    public void onConstructMod() {
        CONFIG.bakeConfigs(MOD_ID);
        ModRegistry.touch();
    }

    @Override
    public void onRegisterCommands(RegisterCommandsContext context) {
        if (CONFIG.get(CommonConfig.class).enchantCommand.replaceVanillaCommand()) {
            ModEnchantCommand.register(context.dispatcher());
        }
    }
}
