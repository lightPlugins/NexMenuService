package io.nexstudios.menuservice.common.api.registry;

import io.nexstudios.menuservice.common.api.MenuKey;
import io.nexstudios.menuservice.common.api.validation.MenuException;

import java.util.Objects;

/**
 * Thrown when a requested menu key is not present in the registry.
 */
public final class MenuNotRegisteredException extends MenuException {

  private final MenuKey key;

  public MenuNotRegisteredException(MenuKey key) {
    super("No menu is registered for key '" + Objects.requireNonNull(key, "key must not be null").asString() + "'");
    this.key = key;
  }

  public MenuKey key() {
    return key;
  }
}