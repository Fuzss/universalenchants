package fuzs.universalenchants.client;

import com.mojang.blaze3d.platform.InputConstants;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.client.core.v1.context.KeyMappingsContext;
import fuzs.puzzleslib.api.client.event.v1.gui.ItemTooltipCallback;
import fuzs.puzzleslib.api.client.event.v1.gui.ScreenEvents;
import fuzs.puzzleslib.api.client.event.v1.gui.ScreenTooltipEvents;
import fuzs.puzzleslib.api.client.key.v1.KeyActivationContext;
import fuzs.puzzleslib.api.client.key.v1.KeyMappingHelper;
import fuzs.puzzleslib.api.core.v1.context.AddReloadListenersContext;
import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.client.handler.StoredEnchantmentsTooltipHandler;
import fuzs.universalenchants.client.handler.TriggerLockRenderHandler;
import fuzs.universalenchants.client.resources.language.NumericClientLanguage;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public class UniversalEnchantsClient implements ClientModConstructor {
    public static final KeyMapping EDIT_ENCHANTMENTS_KEY_MAPPING = KeyMappingHelper.registerKeyMapping(UniversalEnchants.id(
            "edit_enchantments"), InputConstants.UNKNOWN.getValue());

    @Override
    public void onConstructMod() {
        registerEventHandlers();
    }

    private static void registerEventHandlers() {
        ItemTooltipCallback.EVENT.register(StoredEnchantmentsTooltipHandler::onItemTooltip);
        ScreenEvents.afterRender(AbstractContainerScreen.class).register(TriggerLockRenderHandler::onAfterRender);
        ScreenTooltipEvents.RENDER.register(TriggerLockRenderHandler::onRenderTooltip);
    }

    @Override
    public void onRegisterResourcePackReloadListeners(AddReloadListenersContext context) {
        context.registerReloadListener("roman_numerals", NumericClientLanguage::injectLanguage);
    }

    @Override
    public void onRegisterKeyMappings(KeyMappingsContext context) {
        context.registerKeyMapping(EDIT_ENCHANTMENTS_KEY_MAPPING, KeyActivationContext.SCREEN);
    }
}
