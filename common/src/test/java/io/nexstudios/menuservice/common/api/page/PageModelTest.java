package io.nexstudios.menuservice.common.api.page;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PageModelTest {

  @Test
  void pageCountForZeroElementsIsOne() {
    PageModel model = new PageModel(new PageBounds(0, 0, 3, 2, PageAlignment.LEFT)); // cap=6
    assertEquals(1, model.pageCountFor(0));
  }

  @Test
  void pageCountForElements() {
    PageModel model = new PageModel(new PageBounds(0, 0, 3, 2, PageAlignment.LEFT)); // cap=6
    assertEquals(1, model.pageCountFor(1));
    assertEquals(1, model.pageCountFor(6));
    assertEquals(2, model.pageCountFor(7));
    assertEquals(2, model.pageCountFor(12));
    assertEquals(3, model.pageCountFor(13));
  }

  @Test
  void clampPageIndex() {
    PageModel model = new PageModel(new PageBounds(0, 0, 3, 2, PageAlignment.LEFT)); // cap=6
    assertEquals(0, model.clampPageIndex(-5, 10));
    assertEquals(0, model.clampPageIndex(0, 10));
    assertEquals(1, model.clampPageIndex(1, 10));
    assertEquals(1, model.clampPageIndex(99, 10)); // 10 elements => 2 pages => max index 1
  }

  @Test
  void startAndEndExclusive() {
    PageModel model = new PageModel(new PageBounds(0, 0, 3, 2, PageAlignment.LEFT)); // cap=6

    assertEquals(0, model.startIndex(0));
    assertEquals(6, model.startIndex(1));

    assertEquals(6, model.endExclusiveIndex(0, 6));
    assertEquals(10, model.endExclusiveIndex(1, 10)); // start=6, end=min(10,12)=10
  }
}