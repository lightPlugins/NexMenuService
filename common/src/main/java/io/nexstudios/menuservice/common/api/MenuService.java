package io.nexstudios.menuservice.common.api;

import io.nexstudios.menuservice.common.api.registry.MenuNotRegisteredException;
import io.nexstudios.serviceregistry.di.Service;

import java.util.Optional;

/**
 * Main entry point to the menu system.
 */
public interface MenuService extends Service {

  MenuRegistry registry();

  /**
   * Opens the menu for the viewer.
   *
   * @throws MenuNotRegisteredException if the menu key is not registered
   */
  void open(ViewerRef viewer, MenuKey key);

  Optional<MenuView> findOpenView(ViewerRef viewer);
}