package fuzs.universalenchants;

import fuzs.puzzleslib.api.core.v1.ModConstructor;
import net.fabricmc.api.ModInitializer;

public class UniversalEnchantsFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ModConstructor.construct(UniversalEnchants.MOD_ID, UniversalEnchants::new);
    }
}
