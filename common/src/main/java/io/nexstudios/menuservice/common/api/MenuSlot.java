package io.nexstudios.menuservice.common.api;

import io.nexstudios.menuservice.common.api.interaction.ClickAction;
import io.nexstudios.menuservice.common.api.item.MenuItem;
import io.nexstudios.menuservice.common.api.item.MenuItemSupplier;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

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
   * allows deferring Bukkit ItemStack/MenuItem creation to the main thread.
   * The supplier MUST be safe to run on the main thread.
   */
  void setPlannedItem(MenuItemSupplier supplier);

  /**
   * Sets a planned player head with a default placeholder.
   *
   * Implementations may show a default player head immediately and replace it once
   * the provided future completes successfully.
   */
  default void setPlannedHead(CompletableFuture<ItemStack> headFuture) {
    setPlannedHead(MenuItem.of(new ItemStack(Material.PLAYER_HEAD, 1)), headFuture);
  }

  /**
   * Sets a planned player head using the supplied placeholder immediately.
   *
   * The placeholder is shown first (for example with lore, name, or other text),
   * then replaced with the loaded head while keeping placeholder text metadata.
   */
  void setPlannedHead(MenuItem placeholder, CompletableFuture<ItemStack> headFuture);

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