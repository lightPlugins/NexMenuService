package io.nexstudios.menuservice.api.page;

import java.util.List;

/**
 * Provides the items that should be paged inside a menu view.
 *
 * @param <T> the item type
 */
public interface PageSource<T> {

  /**
   * Returns the items that should be displayed.
   *
   * @return the page items
   */
  List<T> items();

  /**
   * Returns the number of items in this source.
   *
   * @return the item count
   */
  default int size() {
    return items().size();
  }
}

