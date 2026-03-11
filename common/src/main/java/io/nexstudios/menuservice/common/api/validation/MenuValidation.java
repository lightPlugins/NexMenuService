package io.nexstudios.menuservice.common.api.validation;

import java.util.Objects;

/**
 * Small validation helpers with consistent English messages.
 */
public final class MenuValidation {

  private MenuValidation() {}

  public static <T> T requireNonNull(T value, String message) {
    return Objects.requireNonNull(value, message);
  }

  public static String requireNotBlank(String value, String fieldName) {
    Objects.requireNonNull(value, fieldName + " must not be null");
    if (value.isBlank()) throw new IllegalArgumentException(fieldName + " must not be blank");
    return value;
  }

  public static int requireRangeInclusive(int value, int min, int max, String fieldName) {
    if (value < min || value > max) {
      throw new IllegalArgumentException(fieldName + " must be between " + min + " and " + max + " (inclusive)");
    }
    return value;
  }
}