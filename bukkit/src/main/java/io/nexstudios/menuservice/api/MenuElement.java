package io.nexstudios.menuservice.api;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a single interactive item inside a menu.
 */
public interface MenuElement {

  /**
   * Creates the item that should be displayed for the current viewer.
   *
   * @param context the active menu context
   * @return the item stack to display, or {@code null} to leave the slot empty
   */
  ItemStack render(MenuContext context);

  /**
   * Handles a click on this element.
   *
   * @param context the active menu context
   * @param event the original Bukkit click event
   */
  default void onClick(MenuContext context, InventoryClickEvent event) {
    // default no-op
  }

  /**
   * Returns whether this element should react to clicks.
   *
   * @return {@code true} if the element is clickable
   */
  default boolean isClickable() {
    return true;
  }
}

