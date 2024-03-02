package fuzs.universalenchants.mixin;

import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.config.ServerConfig;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.ShieldItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ShearsItem.class)
abstract class ShearsItemMixin extends Item {

    public ShearsItemMixin(Properties properties) {
        super(properties);
    }

    @Override
    public int getEnchantmentValue() {
        return UniversalEnchants.CONFIG.get(ServerConfig.class).enchantableShears ? 1 : super.getEnchantmentValue();
    }
}
