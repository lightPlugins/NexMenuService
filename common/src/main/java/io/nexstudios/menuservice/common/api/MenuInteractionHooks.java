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
}