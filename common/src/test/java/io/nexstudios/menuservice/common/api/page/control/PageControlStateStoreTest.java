package io.nexstudios.menuservice.common.api.page.control;

import io.nexstudios.menuservice.common.api.MenuKey;
import io.nexstudios.menuservice.common.api.ViewerRef;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PageControlStateStoreTest {

  private static final MenuKey MENU_KEY = MenuKey.of("nexmenu", "paging-test");
  private static final ViewerRef VIEWER = ViewerRef.of(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Tester");
  private static final PageControl CONTROL = new PageControl() {
    @Override
    public String controlId() {
      return "sorting";
    }

    @Override
    public List<String> modeIds() {
      return List.of("name", "price", "rarity");
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
    InMemoryStore store = new InMemoryStore();
    store.setActiveModeId(VIEWER, MENU_KEY, "items", CONTROL.controlId(), "rarity");

    assertEquals("name", store.cycleToNextMode(VIEWER, MENU_KEY, "items", CONTROL));
    assertEquals(Optional.of("name"), store.getActiveModeId(VIEWER, MENU_KEY, "items", CONTROL.controlId()));
  }

  @Test
  void cycleToPreviousModeWrapsAround() {
    InMemoryStore store = new InMemoryStore();
    store.setActiveModeId(VIEWER, MENU_KEY, "items", CONTROL.controlId(), "name");

    assertEquals("rarity", store.cycleToPreviousMode(VIEWER, MENU_KEY, "items", CONTROL));
    assertEquals(Optional.of("rarity"), store.getActiveModeId(VIEWER, MENU_KEY, "items", CONTROL.controlId()));
  }

  @Test
  void cycleToPreviousModeFallsBackToDefaultWhenCurrentModeIsUnknown() {
    InMemoryStore store = new InMemoryStore();
    store.setActiveModeId(VIEWER, MENU_KEY, "items", CONTROL.controlId(), "unknown");

    assertEquals("name", store.cycleToPreviousMode(VIEWER, MENU_KEY, "items", CONTROL));
    assertEquals(Optional.of("name"), store.getActiveModeId(VIEWER, MENU_KEY, "items", CONTROL.controlId()));
  }

  private static final class InMemoryStore implements PageControlStateStore {
    private final Map<Key, String> activeModes = new HashMap<>();

    @Override
    public Optional<String> getActiveModeId(ViewerRef viewer, MenuKey menuKey, String areaId, String controlId) {
      return Optional.ofNullable(activeModes.get(new Key(viewer.uniqueId(), menuKey.asString(), areaId, controlId)));
    }

    @Override
    public void setActiveModeId(ViewerRef viewer, MenuKey menuKey, String areaId, String controlId, String modeId) {
      activeModes.put(new Key(viewer.uniqueId(), menuKey.asString(), areaId, controlId), Objects.requireNonNull(modeId, "modeId must not be null"));
    }

    private record Key(UUID viewerId, String menuKey, String areaId, String controlId) {}
  }
}

