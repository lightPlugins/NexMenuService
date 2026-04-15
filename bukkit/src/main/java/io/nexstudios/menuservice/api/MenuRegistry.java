package io.nexstudios.menuservice.api;

import io.nexstudios.serviceregistry.di.Service;

import java.util.Collection;
import java.util.Optional;

/**
 * Stores and resolves menu definitions by their stable key.
 */
public interface MenuRegistry extends Service {

  /**
   * Registers the given menu definition.
   *
   * @param definition the definition to register
   */
  void register(MenuDefinition definition);

  /**
   * Resolves a menu definition by key.
   *
   * @param key the menu key
   * @return the resolved definition, if present
   */
  Optional<MenuDefinition> find(MenuKey key);

  /**
   * Returns all registered keys.
   *
   * @return the registered keys
   */
  Collection<MenuKey> keys();
}

