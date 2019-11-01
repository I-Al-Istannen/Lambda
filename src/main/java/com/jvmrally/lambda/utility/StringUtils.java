package com.jvmrally.lambda.utility;

/**
 * Contains some utility functions for dealing with strings.
 */
public final class StringUtils {

    private static final String ELLIPSIS = "...";

    // utility class
    private StringUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Trims the string to the given length and appends ellipsis if needed.
     *
     * @param input   the input string
     * @param maxSize the maximum size
     * @return the trimmed string with ellipsis appended, if it was cut
     */
    public static String trimToSize(String input, int maxSize) {
        if (input.length() <= maxSize) {
            return input;
        } else {
            return input.substring(0, maxSize - ELLIPSIS.length()) + ELLIPSIS;
        }
    }
}
