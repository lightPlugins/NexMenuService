package io.nexstudios.menuservice.common.api.page;

import java.util.List;
import java.util.Objects;

/**
 * A view over a single page's element range.
 */
public record PageSlice<T>(int pageIndex, int startInclusive, int endExclusive, List<T> elements) {

  public PageSlice {
    Objects.requireNonNull(elements, "elements must not be null");
    if (pageIndex < 0) throw new IllegalArgumentException("pageIndex must be >= 0");
    if (startInclusive < 0) throw new IllegalArgumentException("startInclusive must be >= 0");
    if (endExclusive < startInclusive) throw new IllegalArgumentException("endExclusive must be >= startInclusive");
  }

  public int size() {
    return elements.size();
  }
}