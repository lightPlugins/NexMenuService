package io.nexstudios.menuservice.api.page;

import io.nexstudios.menuservice.api.MenuContext;
import io.nexstudios.menuservice.api.MenuElement;

/**
 * Renders a single paged item into a menu element.
 *
 * @param <T> the item type
 */
@FunctionalInterface
public interface PageItemRenderer<T> {

  /**
   * Creates the menu element for the given paged item.
   *
   * @param context the active menu context
   * @param item the item to render
   * @param globalIndex the absolute item index in the source
   * @return the menu element for the item
   */
  MenuElement render(MenuContext context, T item, int globalIndex);
}

