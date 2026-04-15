package io.nexstudios.menuservice.core.page;

import io.nexstudios.menuservice.api.page.PageSource;
import java.util.List;
import java.util.Objects;

/**
 * Immutable page source backed by a collection snapshot.
 *
 * @param <T> the item type
 */
public final class CollectionPageSource<T> implements PageSource<T> {

  private final List<T> items;

  public CollectionPageSource(List<T> items) {
    this.items = List.copyOf(Objects.requireNonNull(items, "items"));
  }

  @Override
  public List<T> items() {
    return items;
  }
}

