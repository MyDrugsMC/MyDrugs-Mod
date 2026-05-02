package org.mydrugs.mydrugs.advancement;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;

final class StringCriteria {
    private StringCriteria() {
    }

    static boolean matches(Optional<String> expected, @Nullable String actual) {
        return expected.isEmpty() || normalize(expected.get()).equals(normalize(actual));
    }

    static String normalize(@Nullable String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
