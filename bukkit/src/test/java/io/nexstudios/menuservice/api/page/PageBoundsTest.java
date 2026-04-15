package io.nexstudios.menuservice.api.page;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PageBoundsTest {

  @Test
  void calculatesSlotsInRowMajorOrder() {
    PageBounds bounds = PageBounds.of(1, 2, 3, 2);

    assertEquals(6, bounds.capacity());
    assertEquals(19, bounds.slotAt(0));
    assertEquals(20, bounds.slotAt(1));
    assertEquals(21, bounds.slotAt(2));
    assertEquals(28, bounds.slotAt(3));
    assertEquals(29, bounds.slotAt(4));
    assertEquals(30, bounds.slotAt(5));
    assertEquals(6, bounds.slots().size());
  }
}

