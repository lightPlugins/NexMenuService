package io.nexstudios.menuservice.common.api.page;

import io.nexstudios.menuservice.common.api.layout.SlotIndex;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Produces the slot indices within a page bounds in display order.
 */
public final class PageSlotMapper {

  private PageSlotMapper() {}

  /**
   * Returns slots in row-major order, applying horizontal alignment per row
   * when itemCountInRow is less than bounds.width().
   */
  public static List<Integer> slotsFor(PageBounds bounds, int itemCount) {
    Objects.requireNonNull(bounds, "bounds must not be null");
    if (itemCount < 0) throw new IllegalArgumentException("itemCount must be >= 0");

    int capacity = bounds.capacity();
    int count = Math.min(itemCount, capacity);

    List<Integer> slots = new ArrayList<>(count);

    int width = bounds.width();
    int height = bounds.height();

    int remaining = count;
    for (int row = 0; row < height && remaining > 0; row++) {
      int rowCount = Math.min(width, remaining);
      int startX = alignedStartX(bounds.alignment(), bounds.x(), width, rowCount);

      for (int col = 0; col < rowCount; col++) {
        int x = startX + col;
        int y = bounds.y() + row;
        slots.add(SlotIndex.toSlot(x, y));
      }

      remaining -= rowCount;
    }

    return List.copyOf(slots);
  }

  private static int alignedStartX(PageAlignment alignment, int baseX, int width, int rowCount) {
    return switch (alignment) {
      case LEFT -> baseX;
      case CENTER -> baseX + Math.max(0, (width - rowCount) / 2);
      case RIGHT -> baseX + Math.max(0, width - rowCount);
    };
  }
}