package fuzs.universalenchants.client.resources.language;

import com.google.common.collect.Maps;
import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.config.ClientConfig;
import fuzs.universalenchants.mixin.client.accessor.I18nAccessor;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

import java.util.TreeMap;

/**
 * wrap {@link net.minecraft.client.resources.language.ClientLanguage} to add dynamic support for all enchantment/mob effect numerals
 *
 * <p>we generate these dynamically, so we don't have to pollute the language key map with a bunch of values that'll never be used
 *
 * <p>really cool solution for converting to roman numerals from <a href="https://stackoverflow.com/a/19759564">here</a>
 */
public class NumericClientLanguage extends Language {
    private final static TreeMap<Integer, String> ROMAN_NUMERALS = Util.make(Maps.newTreeMap(), map -> {
        map.put(1000, "M");
        map.put(900, "CM");
        map.put(500, "D");
        map.put(400, "CD");
        map.put(100, "C");
        map.put(90, "XC");
        map.put(50, "L");
        map.put(40, "XL");
        map.put(10, "X");
        map.put(9, "IX");
        map.put(5, "V");
        map.put(4, "IV");
        map.put(1, "I");
    });
    private final Language language;
    private final Int2ObjectMap<String> numeralsCache = new Int2ObjectOpenHashMap<>();

    public NumericClientLanguage(Language language) {
        this.language = language;
    }

    public static void injectLanguage() {
        if (!UniversalEnchants.CONFIG.get(ClientConfig.class).fixRomanNumerals) return;
        Language language = new NumericClientLanguage(Language.getInstance());
        I18nAccessor.callSetLanguage(language);
        Language.inject(language);
    }

    @Override
    public String getOrDefault(String string) {
        if (this.isNumeral(string)) {
            if (!this.language.has(string)) {
                String number = string.substring(string.lastIndexOf(".") + 1);
                int intNumber = Integer.parseInt(number);
                if (string.startsWith("potion.potency.")) intNumber++;
                return this.numeralsCache.computeIfAbsent(intNumber, this::toNumeral);
            }
        }
        return this.language.getOrDefault(string);
    }

    private boolean isNumeral(String string) {
        return string.startsWith("potion.potency.") || string.startsWith("enchantment.level.");
    }

    private String toNumeral(int number) {
        return number > 0 && number < 4000 ? toRomanNumeral(number) : String.valueOf(number);
    }

    private static String toRomanNumeral(int number) {
        int l = ROMAN_NUMERALS.floorKey(number);
        if (number == l) {
            return ROMAN_NUMERALS.get(number);
        }
        return ROMAN_NUMERALS.get(l) + toRomanNumeral(number - l);
    }

    @Override
    public boolean has(String string) {
        if (this.language.has(string)) return true;
        return this.isNumeral(string);
    }

    @Override
    public boolean isDefaultRightToLeft() {
        return this.language.isDefaultRightToLeft();
    }

    @Override
    public FormattedCharSequence getVisualOrder(FormattedText formattedText) {
        return this.language.getVisualOrder(formattedText);
    }
}
