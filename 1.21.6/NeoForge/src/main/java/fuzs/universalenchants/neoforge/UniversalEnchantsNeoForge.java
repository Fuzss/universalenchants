package fuzs.universalenchants.neoforge;

import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.api.event.v1.data.MutableValue;
import fuzs.puzzleslib.neoforge.api.data.v2.core.DataProviderHelper;
import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.data.ModBlockTagProvider;
import fuzs.universalenchants.data.ModEnchantmentTagProvider;
import fuzs.universalenchants.data.ModItemTagProvider;
import fuzs.universalenchants.data.ModDatapackRegistriesProvider;
import fuzs.universalenchants.handler.BetterEnchantsHandler;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingGetProjectileEvent;

@Mod(UniversalEnchants.MOD_ID)
public class UniversalEnchantsNeoForge {

    public UniversalEnchantsNeoForge() {
        ModConstructor.construct(UniversalEnchants.MOD_ID, UniversalEnchants::new);
        registerEventHandlers(NeoForge.EVENT_BUS);
        DataProviderHelper.registerDataProviders(UniversalEnchants.MOD_ID,
                ModItemTagProvider::new,
                ModBlockTagProvider::new,
                ModEnchantmentTagProvider::new,
                ModDatapackRegistriesProvider::new);
    }

    @Deprecated(forRemoval = true)
    private static void registerEventHandlers(IEventBus eventBus) {
        eventBus.addListener((final LivingGetProjectileEvent evt) -> {
            MutableValue<ItemStack> ammoItemStack = MutableValue.fromEvent(evt::setProjectileItemStack,
                    evt::getProjectileItemStack);
            BetterEnchantsHandler.onGetProjectile(evt.getEntity(), evt.getProjectileWeaponItemStack(), ammoItemStack);
        });
    }
}
