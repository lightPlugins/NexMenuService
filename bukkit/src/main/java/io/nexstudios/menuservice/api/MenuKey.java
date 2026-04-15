package io.nexstudios.menuservice.api;

import java.util.Objects;

/**
 * Identifies a menu definition in a stable and registry-friendly way.
 *
 * @param value the unique menu key value
 */
public record MenuKey(String value) {

  public MenuKey {
    value = Objects.requireNonNull(value, "value").trim();
    if (value.isEmpty()) {
      throw new IllegalArgumentException("Menu key value must not be blank.");
    }
  }

  /**
   * Creates a new menu key from the given value.
   *
   * @param value the key value
   * @return the created menu key
   */
  public static MenuKey of(String value) {
    return new MenuKey(value);
  }

  @Override
  public String toString() {
    return value;
  }
}

