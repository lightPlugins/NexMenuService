package io.nexstudios.menuservice.common.api.page;

import io.nexstudios.menuservice.common.api.layout.SlotIndex;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PageSlotMapperTest {

  @Test
  void leftAlignmentSingleRow() {
    PageBounds bounds = new PageBounds(0, 0, 5, 1, PageAlignment.LEFT);
    List<Integer> slots = PageSlotMapper.slotsFor(bounds, 3);

    assertEquals(List.of(
        SlotIndex.toSlot(0, 0),
        SlotIndex.toSlot(1, 0),
        SlotIndex.toSlot(2, 0)
    ), slots);
  }

  @Test
  void centerAlignmentSingleRow() {
    PageBounds bounds = new PageBounds(0, 0, 5, 1, PageAlignment.CENTER);
    List<Integer> slots = PageSlotMapper.slotsFor(bounds, 3);

    // width=5, rowCount=3 => startX = 0 + (5-3)/2 = 1
    assertEquals(List.of(
        SlotIndex.toSlot(1, 0),
        SlotIndex.toSlot(2, 0),
        SlotIndex.toSlot(3, 0)
    ), slots);
  }

  @Test
  void rightAlignmentSingleRow() {
    PageBounds bounds = new PageBounds(0, 0, 5, 1, PageAlignment.RIGHT);
    List<Integer> slots = PageSlotMapper.slotsFor(bounds, 3);

    // width=5, rowCount=3 => startX = 0 + (5-3) = 2
    assertEquals(List.of(
        SlotIndex.toSlot(2, 0),
        SlotIndex.toSlot(3, 0),
        SlotIndex.toSlot(4, 0)
    ), slots);
  }
}