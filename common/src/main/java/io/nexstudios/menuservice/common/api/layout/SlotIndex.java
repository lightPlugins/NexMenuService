package io.nexstudios.menuservice.common.api.layout;

/**
 * Utility for converting between slot index and grid coordinates.
 *
 * Assumes a chest-like inventory with 9 columns per row.
 */
public final class SlotIndex {

  public static final int COLUMNS = 9;

  private SlotIndex() {}

  public static int toSlot(int x, int y) {
    if (x < 0) throw new IllegalArgumentException("x must be >= 0");
    if (x >= COLUMNS) throw new IllegalArgumentException("x must be < " + COLUMNS);
    if (y < 0) throw new IllegalArgumentException("y must be >= 0");
    return y * COLUMNS + x;
  }

  public static int xOf(int slot) {
    if (slot < 0) throw new IllegalArgumentException("slot must be >= 0");
    return slot % COLUMNS;
  }

  public static int yOf(int slot) {
    if (slot < 0) throw new IllegalArgumentException("slot must be >= 0");
    return slot / COLUMNS;
  }

  public static int sizeForRows(int rows) {
    if (rows < 1) throw new IllegalArgumentException("rows must be >= 1");
    if (rows > 6) throw new IllegalArgumentException("rows must be <= 6");
    return rows * COLUMNS;
  }
}