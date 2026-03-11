package io.nexstudios.menuservice.bukkit.service.menu;

import io.nexstudios.menuservice.common.api.MenuSlot;
import org.bukkit.inventory.Inventory;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

/**
 * Stores slot click handlers per inventory instance.
 */
public final class ClickHandlerStore {

  private static final Map<Inventory, Map<Integer, MenuSlot.MenuClickHandler>> HANDLERS =
      Collections.synchronizedMap(new WeakHashMap<>());

  public static void attach(Inventory inventory, Map<Integer, MenuSlot.MenuClickHandler> handlers) {
    Objects.requireNonNull(inventory, "inventory must not be null");
    Objects.requireNonNull(handlers, "handlers must not be null");
    HANDLERS.put(inventory, Map.copyOf(handlers));
  }

  public static MenuSlot.MenuClickHandler find(Inventory inventory, int slot) {
    Map<Integer, MenuSlot.MenuClickHandler> map = HANDLERS.get(inventory);
    if (map == null) return null;
    return map.get(slot);
  }

  private ClickHandlerStore() {}
}