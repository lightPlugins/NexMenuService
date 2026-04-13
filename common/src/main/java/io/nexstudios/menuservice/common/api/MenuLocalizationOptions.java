package io.nexstudios.menuservice.common.api;

import java.util.Objects;

/**
 * Optional localization settings for a menu definition.
 */
public record MenuLocalizationOptions(boolean enabled, String markerPrefix) {

  public static final String DEFAULT_MARKER_PREFIX = "lang:";

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
}


