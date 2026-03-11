package io.nexstudios.menuservice.common.api.interaction;

import io.nexstudios.menuservice.common.api.ViewerRef;
import io.nexstudios.menuservice.common.api.item.MenuItem;

import java.util.Optional;

/**
 * Normalized interaction snapshot used by hooks/handlers.
 */
public interface InteractionContext {

  ViewerRef viewer();

  InventoryArea area();

  ClickAction clickAction();

  /**
   * Slot index in the clicked inventory area.
   * For OUTSIDE it should be -1.
   */
  int slot();

  /**
   * Item currently on the cursor, if any (snapshot).
   */
  Optional<MenuItem> cursorItem();

  /**
   * Item that was clicked, if any (snapshot).
   */
  Optional<MenuItem> clickedItem();

  /**
   * Hotbar button 1-9 if clickAction == NUMBER_KEY_SWAP.
   */
  Optional<Integer> hotbarButton();
}