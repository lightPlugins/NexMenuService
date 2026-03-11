package io.nexstudios.menuservice.common.api.page;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores page index per paged area id.
 */
public final class PageState {

  private final Map<String, Integer> indexByAreaId = new ConcurrentHashMap<>();

  public int getIndex(String areaId) {
    return indexByAreaId.getOrDefault(areaId, 0);
  }

  public void setIndex(String areaId, int index) {
    if (areaId == null || areaId.isBlank()) throw new IllegalArgumentException("areaId must not be blank");
    if (index < 0) throw new IllegalArgumentException("index must be >= 0");
    indexByAreaId.put(areaId, index);
  }

  public void reset(String areaId) {
    if (areaId == null || areaId.isBlank()) throw new IllegalArgumentException("areaId must not be blank");
    indexByAreaId.remove(areaId);
  }
}