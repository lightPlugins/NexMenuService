package io.nexstudios.menuservice.core.page.control;

import io.nexstudios.menuservice.api.MenuKey;
import io.nexstudios.menuservice.api.page.control.PageFilterControl;
import io.nexstudios.menuservice.api.page.control.PageSortControl;
import io.nexstudios.menuservice.api.page.control.PageControlStateStore;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Applies filter and sort controls to a list of items.
 */
public final class PageControlPipeline {

  private PageControlPipeline() {
  }

  public static <T> List<T> apply(
      List<T> items,
      UUID viewerId,
      MenuKey menuKey,
      String areaId,
      PageControlStateStore stateStore,
      List<? extends PageFilterControl<T>> filters,
      List<? extends PageSortControl<T>> sorts
  ) {
    Objects.requireNonNull(items, "items");
    Objects.requireNonNull(viewerId, "viewerId");
    Objects.requireNonNull(menuKey, "menuKey");
    Objects.requireNonNull(areaId, "areaId");
    Objects.requireNonNull(stateStore, "stateStore");
    Objects.requireNonNull(filters, "filters");
    Objects.requireNonNull(sorts, "sorts");

    List<T> result = new ArrayList<>(items);

    for (PageFilterControl<T> filter : filters) {
      String modeId = stateStore.getActiveModeId(viewerId, menuKey, areaId, filter.controlId())
          .orElse(filter.defaultModeId());
      Predicate<T> predicate = filter.predicateFor(modeId, menuKey, viewerId);
      result = result.stream().filter(predicate).toList();
    }

    Comparator<T> comparator = null;
    for (PageSortControl<T> sort : sorts) {
      String modeId = stateStore.getActiveModeId(viewerId, menuKey, areaId, sort.controlId())
          .orElse(sort.defaultModeId());
      Comparator<T> current = sort.comparatorFor(modeId, menuKey, viewerId);
      comparator = comparator == null ? current : comparator.thenComparing(current);
    }

    if (comparator != null) {
      result = result.stream().sorted(comparator).toList();
    }

    return result;
  }
}

