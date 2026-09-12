package com.commerceops.admin.observability;

import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class SensitiveLogSanitizer {

    public static final String REDACTED_VALUE = "[REDACTED]";

    private static final List<Pattern> KEY_VALUE_PATTERNS = List.of(
            Pattern.compile(
                    "(?i)(password(?:_hash)?|secret|access[_-]?token|refresh[_-]?token|jwt|api[_-]?key|authorization)"
                            + "(\\s*[=:]\\s*|\\\"\\s*:\\s*\\\")([^\\s,;&\\\"]+)(\\\"?)"
            ),
            Pattern.compile("(?i)(bearer)\\s+[A-Za-z0-9._~+/=-]+")
    );

    public String sanitize(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        String sanitized = KEY_VALUE_PATTERNS.get(1).matcher(value)
                .replaceAll("$1 " + REDACTED_VALUE);
        return KEY_VALUE_PATTERNS.getFirst().matcher(sanitized)
                .replaceAll("$1$2" + REDACTED_VALUE + "$4");
    }
}
