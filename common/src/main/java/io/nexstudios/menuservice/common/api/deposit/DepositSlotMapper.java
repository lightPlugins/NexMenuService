package io.nexstudios.menuservice.common.api.deposit;

import io.nexstudios.menuservice.common.api.layout.SlotIndex;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Utility for defining deposit zones using grid coordinates instead of individual slot indices.
 * Converts coordinate-based area definitions into slot sets.
 *
 * Assumes a chest-like inventory with 9 columns per row.
 *
 * Example: Define a 3x3 deposit area starting at (0, 0):
 * <pre>
 *   Set&lt;Integer&gt; depositSlots = DepositSlotMapper.areaToSlots(0, 0, 3, 3);
 *   // Results in slots: 0, 1, 2, 9, 10, 11, 18, 19, 20
 * </pre>
 */
public final class DepositSlotMapper {

  private DepositSlotMapper() {}

  /**
   * Converts a rectangular area defined by coordinates into a set of slot indices.
   *
   * @param x      the x coordinate (column) of the top-left corner
   * @param y      the y coordinate (row) of the top-left corner
   * @param width  the width of the area (number of columns)
   * @param height the height of the area (number of rows)
   * @return a set of slot indices covering the specified area
   */
  public static Set<Integer> areaToSlots(int x, int y, int width, int height) {
    if (x < 0) throw new IllegalArgumentException("x must be >= 0");
    if (y < 0) throw new IllegalArgumentException("y must be >= 0");
    if (width < 1) throw new IllegalArgumentException("width must be >= 1");
    if (height < 1) throw new IllegalArgumentException("height must be >= 1");
    if (x + width > SlotIndex.COLUMNS) {
      throw new IllegalArgumentException("x + width must be <= " + SlotIndex.COLUMNS);
    }

    Set<Integer> slots = new HashSet<>(width * height);

    for (int row = 0; row < height; row++) {
      for (int col = 0; col < width; col++) {
        int slot = SlotIndex.toSlot(x + col, y + row);
        slots.add(slot);
      }
    }

    return slots;
  }

  /**
   * Converts multiple rectangular areas into a combined set of slot indices.
   *
   * @param areas variable number of area definitions (x, y, width, height repeating)
   * @return a set of slot indices covering all specified areas
   */
  public static Set<Integer> multiAreaToSlots(int... areas) {
    Objects.requireNonNull(areas, "areas must not be null");
    if (areas.length % 4 != 0) {
      throw new IllegalArgumentException("areas must be in groups of 4 (x, y, width, height)");
    }

    Set<Integer> slots = new HashSet<>();

    for (int i = 0; i < areas.length; i += 4) {
      int x = areas[i];
      int y = areas[i + 1];
      int width = areas[i + 2];
      int height = areas[i + 3];

      slots.addAll(areaToSlots(x, y, width, height));
    }

    return slots;
  }

  /**
   * Converts a single row into a set of slot indices.
   *
   * @param x      the starting x coordinate (column)
   * @param y      the y coordinate (row)
   * @param length the number of columns in this row
   * @return a set of slot indices for the row
   */
  public static Set<Integer> rowToSlots(int x, int y, int length) {
    return areaToSlots(x, y, length, 1);
  }

  /**
   * Converts a single column into a set of slot indices.
   *
   * @param x      the x coordinate (column)
   * @param y      the starting y coordinate (row)
   * @param height the number of rows in this column
   * @return a set of slot indices for the column
   */
  public static Set<Integer> columnToSlots(int x, int y, int height) {
    return areaToSlots(x, y, 1, height);
  }

  /**
   * Converts a full row (all 9 columns) into a set of slot indices.
   *
   * @param y the y coordinate (row)
   * @return a set of slot indices for the entire row
   */
  public static Set<Integer> fullRowToSlots(int y) {
    return areaToSlots(0, y, SlotIndex.COLUMNS, 1);
  }

  /**
   * Converts individual slot coordinates into a set of slot indices.
   *
   * @param coordinates variable number of coordinate pairs (x, y repeating)
   * @return a set of slot indices for the specified coordinates
   */
  public static Set<Integer> coordinatesToSlots(int... coordinates) {
    Objects.requireNonNull(coordinates, "coordinates must not be null");
    if (coordinates.length % 2 != 0) {
      throw new IllegalArgumentException("coordinates must be in pairs (x, y)");
    }

    Set<Integer> slots = new HashSet<>(coordinates.length / 2);

    for (int i = 0; i < coordinates.length; i += 2) {
      int x = coordinates[i];
      int y = coordinates[i + 1];
      slots.add(SlotIndex.toSlot(x, y));
    }

    return slots;
  }
}

