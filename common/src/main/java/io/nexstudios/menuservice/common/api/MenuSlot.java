package io.nexstudios.menuservice.common.api;

import io.nexstudios.menuservice.common.api.interaction.ClickAction;
import io.nexstudios.menuservice.common.api.item.MenuItem;
import io.nexstudios.menuservice.common.api.item.MenuItemSupplier;

import java.util.Objects;

/**
 * Slot API used during population/rendering.
 */
public interface MenuSlot {

  int index();

  /**
   * Sets an already materialized MenuItem.
   *
   * IMPORTANT: This is NOT async-safe if the MenuItem was created from Bukkit types (ItemStack/Material).
   * Prefer {@link #setPlannedItem(MenuItemSupplier)} so Bukkit objects are created on the main thread.
   */
  @Deprecated(forRemoval = false)
  void setItem(MenuItem item);

  void clear();

  /**
   * NEW: allows deferring Bukkit ItemStack/MenuItem creation to the main thread.
   * The supplier MUST be safe to run on the main thread.
   */
  void setPlannedItem(MenuItemSupplier supplier);

  /**
   * Registers a click handler for this slot.
   */
  void onClick(MenuClickHandler handler);

  static void requireNonNullItem(MenuItem item) {
    Objects.requireNonNull(item, "item must not be null");
  }

  static void requireNonNullPlannedItem(MenuItemSupplier supplier) {
    Objects.requireNonNull(supplier, "supplier must not be null");
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
     * The current open view (top inventory menu view).
     */
    MenuView view();

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