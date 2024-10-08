package com.meliodas.plantitotita.mainmodule;
import android.util.Log;
import android.widget.TextView;

import java.util.*;

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

    public static String arrayToString(ArrayList<String> array) {
        StringBuilder sb = new StringBuilder();
        for (String s : array) {
            sb.append(s).append("\n");
        }
        return sb.toString();
    }

    public static void largeLog(String tag, String content) {
        Log.i(tag, "Content length: " + content.length());
        if (content.length() > 4000) {
            Log.d(tag, content.substring(0, 4000));
            largeLog(tag, content.substring(4000));
        } else {
            Log.d(tag, content);
        }
    }

    public static void updateTextView(TextView textView, Object value) {
        if (value instanceof String) {
            textView.setText((String) value);
        } else if (value instanceof List<?> list) {
            StringBuilder sb = new StringBuilder();
            for (Object item : list) {
                if (item instanceof String) {
                    sb.append("• ").append((String) item).append("\n\n");
                }
            }
            textView.setText(sb.toString().trim());
        } else {
            textView.setText("N/A");
        }
    }
}
