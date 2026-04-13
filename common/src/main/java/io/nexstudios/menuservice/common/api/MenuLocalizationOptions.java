package io.nexstudios.menuservice.common.api;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

/**
 * Optional localization settings for a menu definition.
 */
public record MenuLocalizationOptions(boolean enabled, String markerPrefix) {

  public static final String DEFAULT_MARKER_PREFIX = "language:";
  public static final String LEGACY_MARKER_PREFIX = "lang:";

  public MenuLocalizationOptions {
    Objects.requireNonNull(markerPrefix, "markerPrefix must not be null");
    if (markerPrefix.isBlank()) {
      throw new IllegalArgumentException("markerPrefix must not be blank");
    }
  }

  public static MenuLocalizationOptions of() {
    return new MenuLocalizationOptions(true, DEFAULT_MARKER_PREFIX);
  }

  public static MenuLocalizationOptions of(String markerPrefix) {
    return new MenuLocalizationOptions(true, markerPrefix);
  }

  public boolean matches(String raw) {
    if (raw == null || raw.isBlank()) return false;

    if (raw.startsWith(markerPrefix)) {
      return true;
    }

    return DEFAULT_MARKER_PREFIX.equals(markerPrefix) && raw.startsWith(LEGACY_MARKER_PREFIX);
  }

  public @Nullable String extractKey(String raw) {
    if (!matches(raw)) return null;

    if (raw.startsWith(markerPrefix)) {
      return raw.substring(markerPrefix.length()).trim();
    }

    if (DEFAULT_MARKER_PREFIX.equals(markerPrefix) && raw.startsWith(LEGACY_MARKER_PREFIX)) {
      return raw.substring(LEGACY_MARKER_PREFIX.length()).trim();
    }

    return null;
  }
}


