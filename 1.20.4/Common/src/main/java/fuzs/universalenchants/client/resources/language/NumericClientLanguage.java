package fuzs.universalenchants.client.resources.language;

import com.google.common.collect.Maps;
import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.config.ClientConfig;
import fuzs.universalenchants.mixin.client.accessor.I18nAccessor;
import net.minecraft.Util;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * wrap {@link net.minecraft.client.resources.language.ClientLanguage} to add dynamic support for all enchantment/mob effect numerals
 * <p>we generate these dynamically, so we don't have to pollute the language key map with a bunch of values that'll never be used
 */
public class NumericClientLanguage extends Language {
    /**
     * really cool solution for converting to roman numerals from <a href="https://stackoverflow.com/a/19759564">here</a>
     */
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
    private final Map<String, String> numeralsCache = Maps.newHashMap();

    public NumericClientLanguage(Language language) {
        this.language = language;
    }

    public static void injectLanguage(NumericClientLanguage numericLanguage) {
        if (UniversalEnchants.CONFIG.get(ClientConfig.class).fixRomanNumerals == ClientConfig.NumeralLanguage.NONE) return;
        // prevents us wrapping ourselves accidentally, also prevents wrapping custom language implementations from other mods
        // which might result in an infinite loop (an issue with Server Translation API on Fabric)
        if (numericLanguage.language instanceof ClientLanguage) {
            I18nAccessor.universalenchants$callSetLanguage(numericLanguage);
            Language.inject(numericLanguage);
        }
    }

    @Override
    public String getOrDefault(String string, String fallback) {
        if (fallback == null || Objects.equals(string, fallback)) {
            if (this.isNumeral(string)) {
                return this.numeralsCache.computeIfAbsent(string, this::computeLanguageNumeral);
            }
        }
        return this.language.getOrDefault(string, fallback);
    }

    private boolean isNumeral(String string) {
        return string.startsWith("potion.potency.") || string.startsWith("enchantment.level.");
    }

    private String computeLanguageNumeral(String translationKey) {
        if (!this.language.has(translationKey) || UniversalEnchants.CONFIG.get(ClientConfig.class).fixRomanNumerals == ClientConfig.NumeralLanguage.ARABIC) {
            String number = translationKey.substring(translationKey.lastIndexOf(".") + 1);
            if (number.chars().allMatch(Character::isDigit)) {
                int intNumber = Integer.parseInt(number);
                if (translationKey.startsWith("potion.potency.")) intNumber++;
                return this.toNumeral(intNumber);
            }
            return translationKey;
        } else {
            return this.language.getOrDefault(translationKey);
        }
    }

    private String toNumeral(int number) {
        if (number > 0 && number < 4000) {
            if (UniversalEnchants.CONFIG.get(ClientConfig.class).fixRomanNumerals == ClientConfig.NumeralLanguage.ROMAN) {
                return toRomanNumeral(number);
            }
        }
        return String.valueOf(number);
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
