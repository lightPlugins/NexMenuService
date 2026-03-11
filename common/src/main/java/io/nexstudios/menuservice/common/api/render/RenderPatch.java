package io.nexstudios.menuservice.common.api.render;

import io.nexstudios.menuservice.common.api.item.MenuItem;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * A diff patch to apply to a view.
 */
public record RenderPatch(
    Map<Integer, MenuItem> changedSlots,
    Set<Integer> clearedSlots
) {
  public RenderPatch {
    Objects.requireNonNull(changedSlots, "changedSlots must not be null");
    Objects.requireNonNull(clearedSlots, "clearedSlots must not be null");
  }

  public boolean isEmpty() {
    return changedSlots.isEmpty() && clearedSlots.isEmpty();
  }
}