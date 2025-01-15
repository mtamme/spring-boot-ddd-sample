package com.github.seedwork.infrastructure.persistence;

import java.util.regex.Pattern;

public final class Params {

  private static final Pattern KEYWORDS_SPLIT_PATTERN = Pattern.compile("[^\\d\\p{L}]+");

  private Params() {
  }

  private static void escapeAndAppend(final String string, final StringBuilder pattern) {
    final var count = string.length();

    for (var index = 0; index < count; index++) {
      final var ch = string.charAt(index);

      switch (ch) {
        case '%':
          pattern.append("\\%");
          break;
        case '\\':
          pattern.append("\\\\");
          break;
        case '_':
          pattern.append("\\_");
          break;
        default:
          pattern.append(Character.toLowerCase(ch));
          break;
      }
    }
  }

  public static String containsPattern(final String string) {
    final var keywords = KEYWORDS_SPLIT_PATTERN.split(string);

    if (keywords.length == 0) {
      return "%";
    }
    final var pattern = new StringBuilder();

    pattern.append('%');
    for (final var keyword : keywords) {
      escapeAndAppend(keyword, pattern);
      pattern.append('%');
    }

    return pattern.toString();
  }

  public static String startsWithPattern(final String string) {
    final var keywords = KEYWORDS_SPLIT_PATTERN.split(string);

    if (keywords.length == 0) {
      return "%";
    }
    final var pattern = new StringBuilder();

    for (final var keyword : keywords) {
      escapeAndAppend(keyword, pattern);
      pattern.append('%');
    }

    return pattern.toString();
  }
}
