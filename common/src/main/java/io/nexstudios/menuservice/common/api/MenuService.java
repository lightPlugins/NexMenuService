package io.nexstudios.menuservice.common.api;

import io.nexstudios.menuservice.common.api.registry.MenuNotRegisteredException;
import io.nexstudios.serviceregistry.di.Service;
import io.nexstudios.menuservice.common.api.MenuLocalizationContext;

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

  /**
   * Optional open variant that can provide per-view localization.
   *
   * Implementations may ignore the context if the menu is not language-aware.
   */
  default void open(ViewerRef viewer, MenuKey key, MenuLocalizationContext localizationContext) {
    open(viewer, key);
  }

  Optional<MenuView> findOpenView(ViewerRef viewer);
}