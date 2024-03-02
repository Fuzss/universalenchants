package fuzs.universalenchants.forge;

import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.universalenchants.UniversalEnchants;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

@Mod(UniversalEnchants.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class UniversalEnchantsForge {

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        ModConstructor.construct(UniversalEnchants.MOD_ID, UniversalEnchants::new);
    }
}
