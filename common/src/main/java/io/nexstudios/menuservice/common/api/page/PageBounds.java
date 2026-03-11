package io.nexstudios.menuservice.common.api.page;

import java.util.Objects;

/**
 * Defines a rectangular area in a chest-like grid (x/y in slot coordinates).
 */
public record PageBounds(int x, int y, int width, int height, PageAlignment alignment) {

  public PageBounds {
    Objects.requireNonNull(alignment, "alignment must not be null");
    if (x < 0) throw new IllegalArgumentException("x must be >= 0");
    if (y < 0) throw new IllegalArgumentException("y must be >= 0");
    if (width < 1) throw new IllegalArgumentException("width must be >= 1");
    if (height < 1) throw new IllegalArgumentException("height must be >= 1");
  }

  public int capacity() {
    return width * height;
  }
}