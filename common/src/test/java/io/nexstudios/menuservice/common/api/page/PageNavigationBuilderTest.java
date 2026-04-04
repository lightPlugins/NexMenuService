package io.nexstudios.menuservice.common.api.page;

import org.junit.jupiter.api.Test;

import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.*;

class PageNavigationBuilderTest {

  @Test
  void builderSetsSlotsAndDefaultFlags() {
    PageNavigation navigation = PageNavigation.builder()
        .previousSlot(45)
        .nextSlot(53)
        .build();

    assertEquals(OptionalInt.of(45), navigation.previousSlot());
    assertEquals(OptionalInt.of(53), navigation.nextSlot());
    assertEquals(OptionalInt.empty(), navigation.refreshSlot());
    assertTrue(navigation.hidePreviousOnFirstPage());
    assertTrue(navigation.hideNextOnLastPage());
    assertFalse(navigation.showCurrentPageAmount());
    assertTrue(navigation.previousItem().isEmpty());
    assertTrue(navigation.nextItem().isEmpty());
    assertTrue(navigation.refreshItem().isEmpty());
  }

  @Test
  void builderCanDisableRefreshAndShowPageAmount() {
    PageNavigation navigation = PageNavigation.builder()
        .previousSlot(45)
        .nextSlot(53)
        .refreshSlot(49)
        .showCurrentPageAmount(true)
        .disableRefresh()
        .build();

    assertEquals(OptionalInt.of(45), navigation.previousSlot());
    assertEquals(OptionalInt.of(53), navigation.nextSlot());
    assertEquals(OptionalInt.empty(), navigation.refreshSlot());
    assertTrue(navigation.showCurrentPageAmount());
  }
}

