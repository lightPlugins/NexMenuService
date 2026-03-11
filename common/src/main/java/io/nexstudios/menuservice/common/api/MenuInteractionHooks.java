package io.nexstudios.menuservice.common.api;

import io.nexstudios.menuservice.common.api.interaction.ClickAction;
import io.nexstudios.menuservice.common.api.item.MenuItem;

/**
 * Optional hooks for interactions that are not tied to a specific top-slot component.
 */
public interface MenuInteractionHooks {

  /**
   * Called when a viewer clicks an item in the bottom inventory.
   * Only invoked when enabled by the interaction policy.
   */
  default void onBottomInventoryClick(ViewerRef viewer, MenuItem clickedItem, ClickAction action) {
    // default no-op
  }

  /**
   * Called when a menu view is closed for any reason.
   *
   * Implementations should be fast and must not throw.
   */
  default void onClose(MenuKey menuKey, ViewerRef viewer, CloseReason reason) {
    // default no-op
  }

  /**
   * Called when a menu view is closed for any reason, with phase information.
   *
   * Default implementation keeps backwards-compatibility by calling {@link #onClose(MenuKey, ViewerRef, CloseReason)}
   * only once in {@link ClosePhase#AFTER_CLOSE}.
   *
   * Implementations should be fast and must not throw.
   */
  default void onClose(MenuKey menuKey, ViewerRef viewer, CloseReason reason, ClosePhase phase) {
    if (phase == ClosePhase.AFTER_CLOSE) {
      onClose(menuKey, viewer, reason);
    }
  }
}