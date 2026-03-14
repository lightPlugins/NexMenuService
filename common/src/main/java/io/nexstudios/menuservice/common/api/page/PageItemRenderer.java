package io.nexstudios.menuservice.common.api.page;

import io.nexstudios.menuservice.common.api.item.MenuItemSupplier;

/**
 * Converts an element into a display item (deferred).
 */
@FunctionalInterface
public interface PageItemRenderer<T> {
  MenuItemSupplier render(T element, int index);
}