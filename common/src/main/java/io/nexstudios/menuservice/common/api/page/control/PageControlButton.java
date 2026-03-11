package io.nexstudios.menuservice.common.api.page.control;

import io.nexstudios.menuservice.common.api.MenuKey;
import io.nexstudios.menuservice.common.api.MenuView;
import io.nexstudios.menuservice.common.api.ViewerRef;
import io.nexstudios.menuservice.common.api.item.MenuItem;

import java.util.Objects;
import java.util.Optional;

public interface PageControlButton {

  String areaId();

  String controlId();

  int slot();

  MenuItem render(RenderContext ctx);

  void onClick(ClickContext ctx);

  record RenderContext(
      MenuKey menuKey,
      ViewerRef viewer,
      String areaId,
      PageControl control,
      Optional<String> activeModeId
  ) {
    public RenderContext {
      Objects.requireNonNull(menuKey, "menuKey must not be null");
      Objects.requireNonNull(viewer, "viewer must not be null");
      Objects.requireNonNull(areaId, "areaId must not be null");
      Objects.requireNonNull(control, "control must not be null");
      Objects.requireNonNull(activeModeId, "activeModeId must not be null");
    }
  }

  record ClickContext(
      MenuView view,
      MenuKey menuKey,
      ViewerRef viewer,
      String areaId,
      PageControl control,
      PageControlStateStore stateStore
  ) {
    public ClickContext {
      Objects.requireNonNull(view, "view must not be null");
      Objects.requireNonNull(menuKey, "menuKey must not be null");
      Objects.requireNonNull(viewer, "viewer must not be null");
      Objects.requireNonNull(areaId, "areaId must not be null");
      Objects.requireNonNull(control, "control must not be null");
      Objects.requireNonNull(stateStore, "stateStore must not be null");
    }

    public void requestAreaRefresh() {
      view.requestPagedAreaRefresh(areaId);
    }

    public void requestFullRefresh() {
      view.requestRefresh();
    }
  }
}