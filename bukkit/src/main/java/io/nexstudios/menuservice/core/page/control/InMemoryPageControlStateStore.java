package io.nexstudios.menuservice.core.page.control;

import io.nexstudios.menuservice.api.MenuKey;
import io.nexstudios.menuservice.api.page.control.PageControlStateStore;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Simple thread-safe in-memory state store.
 */
public final class InMemoryPageControlStateStore implements PageControlStateStore {

  private final ConcurrentMap<StateKey, String> state = new ConcurrentHashMap<>();

  @Override
  public Optional<String> getActiveModeId(UUID viewerId, MenuKey menuKey, String areaId, String controlId) {
    return Optional.ofNullable(state.get(new StateKey(viewerId, menuKey, areaId, controlId)));
  }

  @Override
  public void setActiveModeId(UUID viewerId, MenuKey menuKey, String areaId, String controlId, String modeId) {
    state.put(new StateKey(viewerId, menuKey, areaId, controlId), Objects.requireNonNull(modeId, "modeId"));
  }

  private record StateKey(UUID viewerId, MenuKey menuKey, String areaId, String controlId) {
    private StateKey {
      Objects.requireNonNull(viewerId, "viewerId");
      Objects.requireNonNull(menuKey, "menuKey");
      Objects.requireNonNull(areaId, "areaId");
      Objects.requireNonNull(controlId, "controlId");
    }
  }
}

