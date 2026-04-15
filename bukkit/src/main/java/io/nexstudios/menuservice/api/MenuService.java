package io.nexstudios.menuservice.api;

import java.util.Optional;
import java.util.UUID;

import io.nexstudios.serviceregistry.di.Service;
import org.bukkit.entity.Player;

/**
 * Main entry point for opening, refreshing, and navigating menus.
 */
public interface MenuService extends Service {

  /**
   * Opens the given menu view for the player.
   *
   * @param player the viewer
   * @param view the menu view to open
   */
  void open(Player player, MenuView view);

  /**
   * Opens the registered menu definition for the given key.
   *
   * @param player the viewer
   * @param key the menu key
   */
  void open(Player player, MenuKey key);

  /**
   * Refreshes the currently open menu for the player.
   *
   * @param player the viewer
   */
  void refresh(Player player);

  /**
   * Navigates back to the previous menu if one exists.
   *
   * @param player the viewer
   */
  void back(Player player);

  /**
   * Closes the current menu and clears the session state.
   *
   * @param player the viewer
   */
  void close(Player player);

  /**
   * Resolves the currently open menu key for a viewer.
   *
   * @param viewerId the viewer unique id
   * @return the active menu key, if present
   */
  Optional<MenuKey> currentMenu(UUID viewerId);

  /**
   * Resolves the currently open menu view for a viewer.
   *
   * @param viewerId the viewer unique id
   * @return the active menu view, if present
   */
  Optional<MenuView> currentView(UUID viewerId);
}


