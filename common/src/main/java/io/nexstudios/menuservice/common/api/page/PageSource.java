package io.nexstudios.menuservice.common.api.page;

import io.nexstudios.menuservice.common.api.MenuKey;
import io.nexstudios.menuservice.common.api.ViewerRef;

import java.util.List;

/**
 * Provides elements for a paged view. Must be thread-safe.
 */
@FunctionalInterface
public interface PageSource<T> {

  /**
   * Loads the full element list (or a snapshot). Implementation may cache internally.
   */
  List<T> load(MenuKey menuKey, ViewerRef viewer);
}