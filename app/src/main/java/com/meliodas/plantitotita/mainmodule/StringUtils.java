package com.meliodas.plantitotita.mainmodule;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class StringUtils {
    private static final Set<String> LOWERCASE_WORDS = new HashSet<>(Arrays.asList(
            "a", "an", "the", "and", "but", "or", "for", "nor", "on", "at", "to", "from", "by", "in", "of"
    ));

    public static String capitalize(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String[] words = input.split("\\s+");
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            String word = words[i];

            if (i == 0 || !LOWERCASE_WORDS.contains(word.toLowerCase())) {
                result.append(capitalizeWord(word));
            } else {
                result.append(word.toLowerCase());
            }

            if (i < words.length - 1) {
                result.append(" ");
            }
        }

        return result.toString();
    }

    private static String capitalizeWord(String word) {
        if (word.length() > 1 && word.matches(".*[a-zA-Z].*")) {
            return word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
        }
        return word.toUpperCase();
    }
}
