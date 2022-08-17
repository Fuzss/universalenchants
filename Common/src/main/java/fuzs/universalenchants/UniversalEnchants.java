package fuzs.universalenchants;

import com.mojang.brigadier.CommandDispatcher;
import fuzs.puzzleslib.config.ConfigHolder;
import fuzs.puzzleslib.core.CoreServices;
import fuzs.puzzleslib.core.ModConstructor;
import fuzs.universalenchants.config.ClientConfig;
import fuzs.universalenchants.config.ServerConfig;
import fuzs.universalenchants.handler.EnchantCompatManager;
import fuzs.universalenchants.handler.ItemCompatManager;
import fuzs.universalenchants.init.ModRegistry;
import fuzs.universalenchants.server.commands.ModEnchantCommand;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UniversalEnchants implements ModConstructor {
    public static final String MOD_ID = "universalenchants";
    public static final String MOD_NAME = "Universal Enchants";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    @SuppressWarnings("Convert2MethodRef")
    public static final ConfigHolder CONFIG = CoreServices.FACTORIES
            .serverConfig(ServerConfig.class, () -> new ServerConfig())
            .clientConfig(ClientConfig.class, () -> new ClientConfig());

    @Override
    public void onConstructMod() {
        CONFIG.bakeConfigs(MOD_ID);
        ModRegistry.touch();
        CONFIG.getHolder(ServerConfig.class).accept(EnchantCompatManager.INSTANCE::init);
        CONFIG.getHolder(ServerConfig.class).accept(ItemCompatManager.INSTANCE::buildData);
    }

    @Override
    public void onRegisterCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, Commands.CommandSelection environment) {
        ModEnchantCommand.register(dispatcher);
    }
}
