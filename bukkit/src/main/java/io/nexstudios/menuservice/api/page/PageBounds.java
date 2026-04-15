package io.nexstudios.menuservice.api.page;

import java.util.ArrayList;
import java.util.List;

/**
 * Describes a rectangular page area inside an inventory.
 *
 * @param startColumn the zero-based start column
 * @param startRow the zero-based start row
 * @param width the area width in slots
 * @param height the area height in slots
 */
public record PageBounds(int startColumn, int startRow, int width, int height) {

  public PageBounds {
    if (startColumn < 0 || startColumn > 8) {
      throw new IllegalArgumentException("Start column must be between 0 and 8.");
    }
    if (startRow < 0) {
      throw new IllegalArgumentException("Start row must not be negative.");
    }
    if (width <= 0 || width > 9) {
      throw new IllegalArgumentException("Width must be between 1 and 9.");
    }
    if (height <= 0) {
      throw new IllegalArgumentException("Height must be positive.");
    }
    if (startColumn + width > 9) {
      throw new IllegalArgumentException("Page bounds must stay within a single inventory row width.");
    }
  }

  /**
   * Creates a new page bounds descriptor.
   *
   * @param startColumn the start column
   * @param startRow the start row
   * @param width the width
   * @param height the height
   * @return the bounds
   */
  public static PageBounds of(int startColumn, int startRow, int width, int height) {
    return new PageBounds(startColumn, startRow, width, height);
  }

  /**
   * Returns the amount of slots available in this page area.
   *
   * @return the capacity
   */
  public int capacity() {
    return width * height;
  }

  /**
   * Returns the absolute inventory slots in display order.
   *
   * @return the inventory slots
   */
  public List<Integer> slots() {
    List<Integer> slots = new ArrayList<>(capacity());
    for (int row = 0; row < height; row++) {
      for (int column = 0; column < width; column++) {
        slots.add((startRow + row) * 9 + (startColumn + column));
      }
    }
    return List.copyOf(slots);
  }

  /**
   * Returns the absolute inventory slot at the given local index.
   *
   * @param index the zero-based local index
   * @return the absolute slot
   */
  public int slotAt(int index) {
    if (index < 0 || index >= capacity()) {
      throw new IndexOutOfBoundsException("Index must be between 0 and %d".formatted(capacity() - 1));
    }
    int row = index / width;
    int column = index % width;
    return (startRow + row) * 9 + (startColumn + column);
  }
}


