package io.nexstudios.menuservice.common.api.registry;

import io.nexstudios.menuservice.common.api.MenuKey;
import io.nexstudios.menuservice.common.api.validation.MenuException;

import java.util.Objects;

/**
 * Thrown when a menu key is registered twice and duplicates are not allowed.
 */
public final class DuplicateMenuKeyException extends MenuException {

  private final MenuKey key;

  public DuplicateMenuKeyException(MenuKey key) {
    super("A menu with key '" + Objects.requireNonNull(key, "key must not be null").asString() + "' is already registered");
    this.key = key;
  }

  public MenuKey key() {
    return key;
  }
}