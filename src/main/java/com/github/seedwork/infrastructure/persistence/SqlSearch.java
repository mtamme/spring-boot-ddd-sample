package com.github.seedwork.infrastructure.persistence;

import java.util.Objects;
import java.util.regex.Pattern;

public final class SqlSearch {

  private static final Pattern KEYWORDS_SPLIT_PATTERN = Pattern.compile("[^\\d\\p{L}]+");

  private final String[] keywords;

  private SqlSearch(final String[] keywords) {
    this.keywords = Objects.requireNonNull(keywords);
  }

  public static SqlSearch of(final String query) {
    final var keywords = KEYWORDS_SPLIT_PATTERN.split(query);

    return new SqlSearch(keywords);
  }

  private void escapeAndAppend(final String string, final StringBuilder pattern) {
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

  public String containsPattern() {
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

  public String startsWithPattern() {
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
