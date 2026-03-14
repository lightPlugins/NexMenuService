package io.nexstudios.menuservice.common.api.render;

import io.nexstudios.menuservice.common.api.item.MenuItemSupplier;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public record RenderPlan(
    Map<Integer, MenuItemSupplier> slotsToItems,
    Set<Integer> clearedSlots
) {
  public RenderPlan {
    Objects.requireNonNull(slotsToItems, "slotsToItems must not be null");
    Objects.requireNonNull(clearedSlots, "clearedSlots must not be null");
  }

  public static RenderPlan empty() {
    return new RenderPlan(Map.of(), Set.of());
  }
}