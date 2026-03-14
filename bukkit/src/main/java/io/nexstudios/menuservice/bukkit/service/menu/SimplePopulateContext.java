package io.nexstudios.menuservice.bukkit.service.menu;

import io.nexstudios.menuservice.common.api.MenuDefinition;
import io.nexstudios.menuservice.common.api.MenuKey;
import io.nexstudios.menuservice.common.api.MenuPopulateContext;
import io.nexstudios.menuservice.common.api.MenuSlot;
import io.nexstudios.menuservice.common.api.ViewerRef;
import io.nexstudios.menuservice.common.api.item.MenuItem;
import io.nexstudios.menuservice.common.api.item.MenuItemSupplier;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Simple synchronous populate context (phase 1).
 *
 * This will be replaced by the async render/diff pipeline.
 */
final class SimplePopulateContext implements MenuPopulateContext {

  private final MenuKey key;
  private final ViewerRef viewer;
  private final Inventory inventory;
  private final MenuDefinition definition;

  private final Map<Integer, MenuItem> items = new HashMap<>();
  private final Map<Integer, MenuSlot.MenuClickHandler> clickHandlers = new HashMap<>();

  SimplePopulateContext(MenuKey key, ViewerRef viewer, Inventory inventory, MenuDefinition definition) {
    this.key = Objects.requireNonNull(key, "key must not be null");
    this.viewer = Objects.requireNonNull(viewer, "viewer must not be null");
    this.inventory = Objects.requireNonNull(inventory, "inventory must not be null");
    this.definition = Objects.requireNonNull(definition, "definition must not be null");
  }

  @Override
  public MenuKey key() {
    return key;
  }

  @Override
  public ViewerRef viewer() {
    return viewer;
  }

  @Override
  public MenuSlot slot(int slot) {
    if (slot < 0 || slot >= inventory.getSize()) {
      throw new IllegalArgumentException("slot must be within inventory bounds: 0.." + (inventory.getSize() - 1));
    }

    return new MenuSlot() {
      @Override public int index() { return slot; }

      @Override
      public void setItem(MenuItem item) {
        MenuSlot.requireNonNullItem(item);
        items.put(slot, item);
      }

      @Override
      public void setPlannedItem(MenuItemSupplier supplier) {
        MenuSlot.requireNonNullPlannedItem(supplier);
        MenuItem item = supplier.get();
        MenuSlot.requireNonNullItem(item);
        items.put(slot, item);
      }

      @Override
      public void clear() {
        items.remove(slot);
      }

      @Override
      public void onClick(MenuClickHandler handler) {
        clickHandlers.put(slot, Objects.requireNonNull(handler, "handler must not be null"));
      }
    };
  }

  @Override
  public int size() {
    return inventory.getSize();
  }

  void applyAll() {
    for (var e : items.entrySet()) {
      inventory.setItem(e.getKey(), e.getValue().stack());
    }
    ClickHandlerStore.attach(inventory, clickHandlers);
  }
}