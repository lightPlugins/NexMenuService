package io.nexstudios.menuservice.paper.holder;

import io.nexstudios.menuservice.api.MenuKey;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * Inventory holder used to identify menu inventories on Paper.
 */
public final class PaperMenuHolder implements InventoryHolder {

  private final UUID viewerId;
  private MenuKey menuKey;
  private Inventory inventory;

  public PaperMenuHolder(UUID viewerId, MenuKey menuKey) {
    this.viewerId = Objects.requireNonNull(viewerId, "viewerId");
    this.menuKey = Objects.requireNonNull(menuKey, "menuKey");
  }

  public UUID viewerId() {
    return viewerId;
  }

  public MenuKey menuKey() {
    return menuKey;
  }

  public void menuKey(MenuKey menuKey) {
    this.menuKey = Objects.requireNonNull(menuKey, "menuKey");
  }

  public void attach(Inventory inventory) {
    this.inventory = Objects.requireNonNull(inventory, "inventory");
  }

  @Override
  public Inventory getInventory() {
    return inventory;
  }
}


