package fuzs.universalenchants.world.item.enchantment.serialize.entry;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;
import fuzs.universalenchants.UniversalEnchants;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Set;

public class IncompatibleEntry extends DataEntry<Enchantment> {
    public final Set<Enchantment> incompatibles = Sets.newHashSet();

    @Override
    public void dissolve(Set<Enchantment> items) throws JsonSyntaxException {
        items.addAll(this.incompatibles);
    }

    @Override
    public void serialize(JsonArray jsonArray) {
        for (Enchantment enchantment : this.incompatibles) {
            jsonArray.add(Registry.ENCHANTMENT.getKey(enchantment).toString());
        }
    }

    public static IncompatibleEntry deserialize(ResourceLocation enchantment, String... items) throws JsonSyntaxException {
        IncompatibleEntry entry = new IncompatibleEntry();
        for (String item : items) {
            ResourceLocation id = new ResourceLocation(item);
            if (!Registry.ENCHANTMENT.containsKey(id)) {
                JsonSyntaxException e = new JsonSyntaxException("No enchantment with name %s found".formatted(id));
                UniversalEnchants.LOGGER.warn("Failed to deserialize {} enchantment config entry {}: {}", enchantment, item, e);
                continue;
            }
            entry.incompatibles.add(Registry.ENCHANTMENT.get(id));
        }
        return entry;
    }
}
