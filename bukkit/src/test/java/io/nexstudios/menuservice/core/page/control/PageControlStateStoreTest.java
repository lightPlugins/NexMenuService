package io.nexstudios.menuservice.core.page.control;

import io.nexstudios.menuservice.api.MenuKey;
import io.nexstudios.menuservice.api.page.control.PageControl;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PageControlStateStoreTest {

  private static final UUID VIEWER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final MenuKey MENU_KEY = MenuKey.of("paging-test");
  private static final PageControl CONTROL = new PageControl() {
    @Override
    public String controlId() {
      return "sorting";
    }

    @Override
    public List<String> modeIds() {
      return List.of("name", "power", "rarity");
    }

    @Override
    public String defaultModeId() {
      return "name";
    }

    @Override
    public String labelForMode(String modeId) {
      return modeId;
    }
  };

  @Test
  void cycleToNextModeWrapsAround() {
    InMemoryPageControlStateStore store = new InMemoryPageControlStateStore();

    assertEquals("power", store.cycleToNextMode(VIEWER_ID, MENU_KEY, "items", CONTROL));
    assertEquals(Optional.of("power"), store.getActiveModeId(VIEWER_ID, MENU_KEY, "items", CONTROL.controlId()));
    assertEquals("rarity", store.cycleToNextMode(VIEWER_ID, MENU_KEY, "items", CONTROL));
    assertEquals("name", store.cycleToNextMode(VIEWER_ID, MENU_KEY, "items", CONTROL));
  }

  @Test
  void cycleToPreviousModeWrapsAround() {
    InMemoryPageControlStateStore store = new InMemoryPageControlStateStore();
    store.setActiveModeId(VIEWER_ID, MENU_KEY, "items", CONTROL.controlId(), "name");

    assertEquals("rarity", store.cycleToPreviousMode(VIEWER_ID, MENU_KEY, "items", CONTROL));
    assertEquals(Optional.of("rarity"), store.getActiveModeId(VIEWER_ID, MENU_KEY, "items", CONTROL.controlId()));
  }
}

