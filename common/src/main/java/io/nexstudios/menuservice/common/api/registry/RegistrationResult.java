package io.nexstudios.menuservice.common.api.registry;

import io.nexstudios.menuservice.common.api.MenuKey;
import io.nexstudios.menuservice.common.api.MenuDefinition;

import java.util.Objects;
import java.util.Optional;

/**
 * Result returned by registry operations.
 */
public record RegistrationResult(
    MenuKey key,
    boolean replaced,
    Optional<MenuDefinition> previous
) {
  public RegistrationResult {
    Objects.requireNonNull(key, "key must not be null");
    Objects.requireNonNull(previous, "previous must not be null");
    if (!replaced && previous.isPresent()) {
      throw new IllegalArgumentException("previous must be empty when replaced is false");
    }
    if (replaced && previous.isEmpty()) {
      throw new IllegalArgumentException("previous must be present when replaced is true");
    }
  }

  public static RegistrationResult added(MenuKey key) {
    return new RegistrationResult(key, false, Optional.empty());
  }

  public static RegistrationResult replaced(MenuKey key, MenuDefinition previous) {
    Objects.requireNonNull(previous, "previous must not be null");
    return new RegistrationResult(key, true, Optional.of(previous));
  }
}