package fuzs.universalenchants.world.item.enchantment.data;

import com.google.common.base.CharMatcher;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fuzs.puzzleslib.api.config.v3.json.JsonConfigFileUtil;
import fuzs.puzzleslib.api.core.v1.ModLoaderEnvironment;
import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.config.ServerConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public final class MaxLevelManager {

    private MaxLevelManager() {

    }

    @Nullable
    public static Integer getMaxLevel(Enchantment enchantment) {
        return UniversalEnchants.CONFIG.get(ServerConfig.class).maxLevelOverrides.<Integer>getOptional(enchantment, 0).orElse(null);
    }

    public static Optional<Integer> getMaxLevel2(Enchantment enchantment) {
        return UniversalEnchants.CONFIG.get(ServerConfig.class).maxLevelOverrides.<Integer>getOptional(enchantment, 0);
    }

    public static void onLoadComplete() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("loader", ModLoaderEnvironment.INSTANCE.getModLoader().name());
        jsonObject.addProperty("main", Enchantment.class.getName().replace(".", "/"));
        int skip = 1;
        for (Method method : Enchantment.class.getMethods()) {
            if (method.getParameterTypes().length == 0 && method.getReturnType() == int.class) {
                if (skip-- <= 0) {
                    jsonObject.addProperty("method", method.getName());
                    break;
                }
            }
        }
        JsonArray jsonArray = new JsonArray();
        Stream.concat(Stream.of(Enchantment.class), BuiltInRegistries.ENCHANTMENT.stream().map(Object::getClass)).distinct()
                .map(Class::getName).map(s -> s.replace(".", "/"))
                .sorted(Comparator.<String>comparingInt(s -> CharMatcher.is('/').countIn(s)).thenComparing(Comparator.naturalOrder()))
                .forEach(jsonArray::add);
        jsonObject.add("targets", jsonArray);
        File file = JsonConfigFileUtil.getConfigPath("." + UniversalEnchants.MOD_ID + "cache");
        JsonConfigFileUtil.saveToFile(file, jsonObject);
    }
}
