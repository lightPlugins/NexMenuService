package io.nexstudios.menuservice.api.page.control;

import io.nexstudios.menuservice.api.MenuKey;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Stores the active mode per viewer, menu, area and control.
 */
public interface PageControlStateStore {

  Optional<String> getActiveModeId(UUID viewerId, MenuKey menuKey, String areaId, String controlId);

  void setActiveModeId(UUID viewerId, MenuKey menuKey, String areaId, String controlId, String modeId);

  default String cycleToNextMode(UUID viewerId, MenuKey menuKey, String areaId, PageControl control) {
    return cycle(viewerId, menuKey, areaId, control, 1);
  }

  default String cycleToPreviousMode(UUID viewerId, MenuKey menuKey, String areaId, PageControl control) {
    return cycle(viewerId, menuKey, areaId, control, -1);
  }

  private String cycle(UUID viewerId, MenuKey menuKey, String areaId, PageControl control, int step) {
    Objects.requireNonNull(viewerId, "viewerId");
    Objects.requireNonNull(menuKey, "menuKey");
    Objects.requireNonNull(areaId, "areaId");
    Objects.requireNonNull(control, "control");

    List<String> modes = control.modeIds();
    if (modes.isEmpty()) {
      throw new IllegalStateException("control.modeIds() must not be empty.");
    }

    String current = getActiveModeId(viewerId, menuKey, areaId, control.controlId()).orElse(control.defaultModeId());
    int index = modes.indexOf(current);
    if (index < 0) {
      index = modes.indexOf(control.defaultModeId());
    }
    if (index < 0) {
      index = 0;
    }

    int nextIndex = Math.floorMod(index + step, modes.size());
    String next = modes.get(nextIndex);
    setActiveModeId(viewerId, menuKey, areaId, control.controlId(), next);
    return next;
  }
}

