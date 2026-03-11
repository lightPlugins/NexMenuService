package io.nexstudios.menuservice.common.api;

import io.nexstudios.menuservice.common.api.interaction.ClickAction;
import io.nexstudios.menuservice.common.api.item.MenuItem;

import java.util.Objects;

/**
 * Slot API used during population/rendering.
 */
public interface MenuSlot {

  int index();

  void setItem(MenuItem item);

  void clear();

  /**
   * Registers a click handler for this slot.
   */
  void onClick(MenuClickHandler handler);

  static void requireNonNullItem(MenuItem item) {
    Objects.requireNonNull(item, "item must not be null");
  }

  @FunctionalInterface
  interface MenuClickHandler {
    void handle(MenuClickContext context);
  }

  interface MenuClickContext {
    ViewerRef viewer();
    int slot();
    ClickAction action();
    boolean isTopInventory();
    boolean isBottomInventory();

    /**
     * Sets/replaces the clicked slot item (top inventory).
     */
    void setCurrentItem(MenuItem item);

    /**
     * Clears the clicked slot item (top inventory).
     */
    void clearCurrentItem();

    /**
     * Cancels the interaction (platform adapter will cancel the underlying event).
     */
    void cancel();
  }
}