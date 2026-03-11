package io.nexstudios.menuservice.common.api.page;

import io.nexstudios.menuservice.common.api.item.MenuItem;

/**
 * Converts an element into a display item.
 */
@FunctionalInterface
public interface PageItemRenderer<T> {
  MenuItem render(T element, int index);
}