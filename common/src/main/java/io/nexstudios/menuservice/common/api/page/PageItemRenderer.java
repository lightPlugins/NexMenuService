package io.nexstudios.menuservice.common.api.page;

import io.nexstudios.menuservice.common.api.item.MenuItemSupplier;

/** Converts an element into a display item (deferred).
 * Implementations may return a {@link io.nexstudios.menuservice.common.api.item.PlannedMenuItemSupplier}
 * when the rendered item should show a placeholder first and resolve a player head texture later.
 */
@FunctionalInterface
public interface PageItemRenderer<T> {
  MenuItemSupplier render(T element, int index);
}