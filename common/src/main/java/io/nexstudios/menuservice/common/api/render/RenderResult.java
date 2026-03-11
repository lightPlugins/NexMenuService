package io.nexstudios.menuservice.common.api.render;

import io.nexstudios.menuservice.common.api.item.MenuItem;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Output of a render pass.
 *
 * slotsToItems contains only top-inventory slots.
 * clearedSlots indicates slots that must be cleared.
 */
public record RenderResult(
    Map<Integer, MenuItem> slotsToItems,
    Set<Integer> clearedSlots
) {

  public RenderResult {
    Objects.requireNonNull(slotsToItems, "slotsToItems must not be null");
    Objects.requireNonNull(clearedSlots, "clearedSlots must not be null");
  }

  public static RenderResult empty() {
    return new RenderResult(Collections.emptyMap(), Collections.emptySet());
  }
}