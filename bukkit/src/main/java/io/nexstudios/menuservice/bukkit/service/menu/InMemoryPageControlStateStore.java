package io.nexstudios.menuservice.bukkit.service.menu;

import io.nexstudios.menuservice.common.api.MenuKey;
import io.nexstudios.menuservice.common.api.ViewerRef;
import io.nexstudios.menuservice.common.api.page.control.PageControlStateStore;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class InMemoryPageControlStateStore implements PageControlStateStore {

  private final ConcurrentMap<Key, String> activeModeByKey = new ConcurrentHashMap<>();

  @Override
  public Optional<String> getActiveModeId(ViewerRef viewer, MenuKey menuKey, String areaId, String controlId) {
    Objects.requireNonNull(viewer, "viewer must not be null");
    Objects.requireNonNull(menuKey, "menuKey must not be null");
    Objects.requireNonNull(areaId, "areaId must not be null");
    Objects.requireNonNull(controlId, "controlId must not be null");
    return Optional.ofNullable(activeModeByKey.get(new Key(viewer.uniqueId(), menuKey.asString(), areaId, controlId)));
  }

  @Override
  public void setActiveModeId(ViewerRef viewer, MenuKey menuKey, String areaId, String controlId, String modeId) {
    Objects.requireNonNull(viewer, "viewer must not be null");
    Objects.requireNonNull(menuKey, "menuKey must not be null");
    Objects.requireNonNull(areaId, "areaId must not be null");
    Objects.requireNonNull(controlId, "controlId must not be null");
    Objects.requireNonNull(modeId, "modeId must not be null");
    activeModeByKey.put(new Key(viewer.uniqueId(), menuKey.asString(), areaId, controlId), modeId);
  }

  public void clearForViewer(UUID viewerId) {
    if (viewerId == null) return;
    activeModeByKey.keySet().removeIf(k -> k.viewerId.equals(viewerId));
  }

  private record Key(UUID viewerId, String menuKey, String areaId, String controlId) {}
}