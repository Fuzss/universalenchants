package fuzs.universalenchants;

import fuzs.puzzleslib.api.capability.v2.ForgeCapabilityHelper;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.api.data.v2.core.DataProviderHelper;
import fuzs.universalenchants.capability.ArrowLootingCapability;
import fuzs.universalenchants.data.ModEnchantmentTagProvider;
import fuzs.universalenchants.data.ModItemTagProvider;
import fuzs.universalenchants.init.ModRegistry;
import fuzs.universalenchants.world.item.enchantment.data.EnchantmentData;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

@Mod(UniversalEnchants.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class UniversalEnchantsForge {

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        ModConstructor.construct(UniversalEnchants.MOD_ID, UniversalEnchants::new);
        registerCapabilities();
        DataProviderHelper.registerDataProviders(UniversalEnchants.MOD_ID, data -> {
            return new ModEnchantmentTagProvider(data, EnchantmentData.getDefaultEnchantmentData(true));
        }, data -> {
            return new ModItemTagProvider(data, EnchantmentData.getDefaultEnchantmentData(true));
        });
    }

    private static void registerCapabilities() {
        ForgeCapabilityHelper.setCapabilityToken(ModRegistry.ARROW_LOOTING_CAPABILITY, new CapabilityToken<ArrowLootingCapability>() {});
    }
}
