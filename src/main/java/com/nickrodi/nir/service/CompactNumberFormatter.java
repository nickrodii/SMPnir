package com.nickrodi.nir.service;

import java.util.Locale;

public final class CompactNumberFormatter {
    private static final long THOUSAND = 1_000L;
    private static final long MILLION = 1_000_000L;
    private static final long BILLION = 1_000_000_000L;
    private static final long TRILLION = 1_000_000_000_000L;
    private static final long QUADRILLION = 1_000_000_000_000_000L;

    private static final long[] DIVISORS = new long[]{
            THOUSAND,
            MILLION,
            BILLION,
            TRILLION,
            QUADRILLION
    };

    private static final String[] SUFFIXES = new String[]{
            "k",
            "mil",
            "bil",
            "tril",
            "quad"
    };

    private CompactNumberFormatter() {
    }

    public static String format(long value) {
        double abs = Math.abs((double) value);
        if (abs < THOUSAND) {
            return Long.toString(value);
        }

        int suffixIndex = 0;
        for (int i = DIVISORS.length - 1; i >= 0; i--) {
            if (abs >= DIVISORS[i]) {
                suffixIndex = i;
                break;
            }
        }

        double scaled = abs / DIVISORS[suffixIndex];
        double rounded = roundOneDecimal(scaled);

        if (rounded >= 1000.0 && suffixIndex < SUFFIXES.length - 1) {
            suffixIndex++;
            scaled = abs / DIVISORS[suffixIndex];
            rounded = roundOneDecimal(scaled);
        }

        String number = isWhole(rounded)
                ? Long.toString((long) rounded)
                : String.format(Locale.US, "%.1f", rounded);

        if (value < 0) {
            return "-" + number + SUFFIXES[suffixIndex];
        }
        return number + SUFFIXES[suffixIndex];
    }

    private static double roundOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private static boolean isWhole(double value) {
        return Math.abs(value - Math.rint(value)) < 0.0000001;
    }
}
