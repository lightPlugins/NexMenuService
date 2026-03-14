package io.nexstudios.menuservice.common.api.page;

import io.nexstudios.menuservice.common.api.item.MenuItemSupplier;
import io.nexstudios.menuservice.common.api.render.RenderPlan;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Stateless helper to render a page slice into slots.
 */
public final class PageRenderer {

  private PageRenderer() {}

  public static <T> RenderPlan renderPage(
      PagedAreaDefinition<T> definition,
      int pageIndex,
      List<T> allElements
  ) {
    if (definition == null) throw new IllegalArgumentException("definition must not be null");
    if (pageIndex < 0) throw new IllegalArgumentException("pageIndex must be >= 0");
    if (allElements == null) throw new IllegalArgumentException("allElements must not be null");

    PageModel model = definition.model();
    int clamped = model.clampPageIndex(pageIndex, allElements.size());

    int start = model.startIndex(clamped);
    int end = model.endExclusiveIndex(clamped, allElements.size());
    List<T> slice = allElements.subList(start, end);

    List<Integer> targetSlots = PageSlotMapper.slotsFor(definition.bounds(), slice.size());

    Map<Integer, MenuItemSupplier> items = new HashMap<>();
    for (int i = 0; i < slice.size(); i++) {
      T element = slice.get(i);
      items.put(targetSlots.get(i), definition.renderer().render(element, start + i));
    }

    int capacity = definition.bounds().capacity();
    List<Integer> capacitySlots = PageSlotMapper.slotsFor(definition.bounds(), capacity);

    Set<Integer> cleared = new HashSet<>();
    for (int i = slice.size(); i < capacitySlots.size(); i++) {
      cleared.add(capacitySlots.get(i));
    }

    return new RenderPlan(Map.copyOf(items), Set.copyOf(cleared));
  }
}