package io.nexstudios.menuservice.common.api;

import io.nexstudios.menuservice.common.api.registry.DuplicateStrategy;
import io.nexstudios.menuservice.common.api.registry.RegistrationResult;

import java.util.Collection;
import java.util.Optional;

/**
 * Stores menu definitions.
 */
public interface MenuRegistry {

  /**
   * Registers a menu definition.
   *
   * Default behavior is {@link DuplicateStrategy#FAIL}.
   */
  default RegistrationResult register(MenuDefinition definition) {
    return register(definition, DuplicateStrategy.FAIL);
  }

  /**
   * Registers a menu definition using the provided duplicate strategy.
   */
  RegistrationResult register(MenuDefinition definition, DuplicateStrategy duplicateStrategy);

  Optional<MenuDefinition> find(MenuKey key);

  Collection<MenuDefinition> all();
}