package com.comic.h.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class SlugUtils {

    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-zA-Z0-9-]");
    private static final Pattern DUPLICATE_HYPHENS = Pattern.compile("-{2,}");

    private SlugUtils() {
        // Utility class
    }

    public static String toSlug(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }

        // Replace Vietnamese 'đ' / 'Đ' before NFD normalization
        String str = input.trim()
                .replace("đ", "d")
                .replace("Đ", "d");

        // Separate base letters from combining diacritical marks
        String normalized = Normalizer.normalize(str, Normalizer.Form.NFD);

        // Remove combining diacritical marks (accents)
        String noDiacritics = DIACRITICS.matcher(normalized).replaceAll("");

        // Replace non-alphanumeric characters (spaces, punctuation) with hyphens
        String slug = NON_ALPHANUMERIC.matcher(noDiacritics).replaceAll("-");

        // Collapse multiple consecutive hyphens into a single hyphen
        slug = DUPLICATE_HYPHENS.matcher(slug).replaceAll("-");

        // Convert to lowercase and trim leading/trailing hyphens
        return slug.toLowerCase(Locale.ENGLISH).replaceAll("^-+|-+$", "");
    }
}
