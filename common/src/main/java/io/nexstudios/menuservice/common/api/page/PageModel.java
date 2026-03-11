package io.nexstudios.menuservice.common.api.page;

import java.util.Objects;

/**
 * Stateless paging calculations for a rectangular page area.
 */
public record PageModel(PageBounds bounds) {

  public PageModel {
    Objects.requireNonNull(bounds, "bounds must not be null");
  }

  public int capacity() {
    return bounds.capacity();
  }

  public int pageCountFor(int totalElements) {
    if (totalElements < 0) throw new IllegalArgumentException("totalElements must be >= 0");
    if (totalElements == 0) return 1;
    int cap = capacity();
    return (totalElements + cap - 1) / cap;
  }

  public int clampPageIndex(int pageIndex, int totalElements) {
    if (pageIndex < 0) pageIndex = 0;
    int max = Math.max(0, pageCountFor(totalElements) - 1);
    return Math.min(pageIndex, max);
  }

  public int startIndex(int pageIndex) {
    if (pageIndex < 0) throw new IllegalArgumentException("pageIndex must be >= 0");
    return pageIndex * capacity();
  }

  public int endExclusiveIndex(int pageIndex, int totalElements) {
    if (totalElements < 0) throw new IllegalArgumentException("totalElements must be >= 0");
    int start = startIndex(pageIndex);
    return Math.min(totalElements, start + capacity());
  }
}