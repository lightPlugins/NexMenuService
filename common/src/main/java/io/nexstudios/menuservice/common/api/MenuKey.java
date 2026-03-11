package io.nexstudios.menuservice.common.api;

import java.util.Objects;

/**
 * A stable identifier for a menu definition.
 */
public record MenuKey(String namespace, String value) {

  public MenuKey {
    Objects.requireNonNull(namespace, "namespace must not be null");
    Objects.requireNonNull(value, "value must not be null");
    if (namespace.isBlank()) throw new IllegalArgumentException("namespace must not be blank");
    if (value.isBlank()) throw new IllegalArgumentException("value must not be blank");
    if (namespace.contains(":")) throw new IllegalArgumentException("namespace must not contain ':'");
    if (value.contains(":")) throw new IllegalArgumentException("value must not contain ':'");
  }

  public static MenuKey of(String namespace, String value) {
    return new MenuKey(namespace, value);
  }

  public String asString() {
    return namespace + ":" + value;
  }
}