package fuzs.universalenchants.handler;

import fuzs.universalenchants.UniversalEnchants;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.apache.commons.compress.utils.Lists;

import java.util.List;

public class EnchantCompatManager {
    public static final EnchantCompatManager INSTANCE = new EnchantCompatManager();

    private final List<CompatibilityEntry> entries = Lists.newArrayList();

    public void init() {
        this.entries.clear();
        if (UniversalEnchants.CONFIG.server().infinityMendingFix) {
            this.entries.add(new CompatibilityEntry(Enchantments.INFINITY_ARROWS, Enchantments.MENDING));
        }
        if (UniversalEnchants.CONFIG.server().multishotPiercingFix) {
            this.entries.add(new CompatibilityEntry(Enchantments.MULTISHOT, Enchantments.PIERCING));
        }
    }

    public boolean isCompatibleWith(Enchantment enchantment1, Enchantment enchantment2) {
        for (CompatibilityEntry entry : this.entries) {
            if (entry.validate(enchantment1, enchantment2)) {
                return true;
            }
        }
        return false;
    }

    private static record CompatibilityEntry(Enchantment enchantment1, Enchantment enchantment2, boolean addRestrictions) {
        public CompatibilityEntry(Enchantment enchantment1, Enchantment enchantment2) {
            this(enchantment1, enchantment2, false);
        }

        public boolean validate(Enchantment enchantment1, Enchantment enchantment2) {
            if (enchantment1 == this.enchantment1) {
                return enchantment2 == this.enchantment2;
            }
            return enchantment2 == this.enchantment1 && enchantment1 == this.enchantment2;
        }
    }
}
