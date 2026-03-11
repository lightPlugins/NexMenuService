package io.nexstudios.menuservice.common.api.page;

import io.nexstudios.menuservice.common.api.MenuSlot;

/**
 * Optional click handler for elements rendered inside a paged area.
 *
 * Executed on the platform main-thread (because the underlying inventory click event is main-thread).
 */
@FunctionalInterface
public interface PageClickHandler<T> {

  /**
   * @param element the element that was rendered into the clicked slot
   * @param index the global index in the full element list (not page-local)
   * @param ctx click context
   */
  void onClick(T element, int index, MenuSlot.MenuClickContext ctx);
}