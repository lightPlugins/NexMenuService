package io.nexstudios.menuservice.common.api.page.control;

import io.nexstudios.menuservice.common.api.MenuKey;
import io.nexstudios.menuservice.common.api.ViewerRef;

import java.util.List;
import java.util.Optional;

public interface PageControlStateStore {

  Optional<String> getActiveModeId(ViewerRef viewer, MenuKey menuKey, String areaId, String controlId);

  void setActiveModeId(ViewerRef viewer, MenuKey menuKey, String areaId, String controlId, String modeId);

  default String cycleToNextMode(
      ViewerRef viewer,
      MenuKey menuKey,
      String areaId,
      PageControl control
  ) {
    String current = getActiveModeId(viewer, menuKey, areaId, control.controlId()).orElse(control.defaultModeId());

    List<String> modes = control.modeIds();
    int idx = modes.indexOf(current);
    String next = (idx < 0) ? control.defaultModeId() : modes.get((idx + 1) % modes.size());

    setActiveModeId(viewer, menuKey, areaId, control.controlId(), next);
    return next;
  }
}