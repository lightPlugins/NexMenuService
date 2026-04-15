package io.nexstudios.menuservice.api;

import java.util.Map;
import net.kyori.adventure.text.Component;

/**
 * Describes a menu blueprint that can be rendered for a viewer.
 */
public interface MenuView {

  /**
   * Returns the stable key of this menu.
   *
   * @return the menu key
   */
  MenuKey key();

  /**
   * Returns the total inventory size in slots.
   *
   * @return the inventory size
   */
  int size();

  /**
   * Returns the title that should be used for the current viewer.
   *
   * @param context the active menu context
   * @return the menu title
   */
  Component title(MenuContext context);

  /**
   * Returns the elements that should be rendered for the current viewer.
   * The map key is the absolute inventory slot index.
   *
   * @param context the active menu context
   * @return the slot-to-element mapping
   */
  Map<Integer, MenuElement> elements(MenuContext context);

  /**
   * Called after the menu has been opened.
   *
   * @param context the active menu context
   */
  default void onOpen(MenuContext context) {
    // default no-op
  }

  /**
   * Called before the menu is closed.
   *
   * @param context the active menu context
   */
  default void onClose(MenuContext context) {
    // default no-op
  }
}

