package fuzs.universalenchants.world.item.enchantment.data;

import com.google.common.base.CharMatcher;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fuzs.puzzleslib.api.config.v3.json.JsonConfigFileUtil;
import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.config.ServerConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.enchantment.Enchantment;

import java.io.File;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

public final class MaxLevelManager {

    private MaxLevelManager() {

    }

    public static Optional<Integer> getMaxLevel(Enchantment enchantment) {
        return UniversalEnchants.CONFIG.get(ServerConfig.class).maxLevelOverrides.<Integer>getOptional(enchantment, 0);
    }

    public static void onLoadComplete() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("input", "EnchantmentMixin.class.getName()");
        JsonArray jsonArray = new JsonArray();
        jsonObject.add("targets", jsonArray);
        Stream.concat(Stream.of(Enchantment.class), BuiltInRegistries.ENCHANTMENT.stream().map(Object::getClass))
                .distinct().map(Class::getName)
                .sorted(Comparator.<String>comparingInt(s -> CharMatcher.is('.').countIn(s)).thenComparing(Comparator.naturalOrder()))
                .forEach(jsonArray::add);
        File file = JsonConfigFileUtil.getConfigPath("." + UniversalEnchants.MOD_ID + "cache");
        JsonConfigFileUtil.saveToFile(file, jsonArray);
    }
}
