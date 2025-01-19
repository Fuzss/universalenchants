package fuzs.universalenchants.data.client;

import fuzs.puzzleslib.api.client.data.v2.AbstractLanguageProvider;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.client.UniversalEnchantsClient;
import fuzs.universalenchants.client.gui.screens.inventory.EditEnchantmentsScreen;
import fuzs.universalenchants.server.commands.ModEnchantCommand;

public class ModLanguageProvider extends AbstractLanguageProvider {

    public ModLanguageProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    public void addTranslations(TranslationBuilder builder) {
        builder.add(ModEnchantCommand.KEY_REMOVE_SUCCESS_SINGLE, "Removed enchantment %s from %s's item");
        builder.add(ModEnchantCommand.KEY_REMOVE_SUCCESS_MULTIPLE, "Removed enchantment %s from %s entities");
        builder.add(ModEnchantCommand.KEY_REMOVE_FAILED_MISSING, "%s does not have that enchantment");
        builder.add(EditEnchantmentsScreen.COMPONENT_EDIT_ENCHANTMENTS, "Edit Enchantments");
        builder.addKeyCategory(UniversalEnchants.MOD_ID, UniversalEnchants.MOD_NAME);
        builder.add(UniversalEnchantsClient.EDIT_ENCHANTMENTS_KEY_MAPPING, "Edit Enchantments");
        builder.add(EditEnchantmentsScreen.KEY_INCOMPATIBLE_ENCHANTMENTS, "This enchantment is incompatible with: %s");
    }
}
