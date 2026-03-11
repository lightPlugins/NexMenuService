package io.nexstudios.menuservice.bukkit.inventory;

import io.nexstudios.menuservice.common.api.MenuKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.Objects;
import java.util.UUID;

/**
 * Custom inventory holder used to identify menu inventories.
 */
public final class PaperMenuHolder implements InventoryHolder {

  private final UUID viewerId;
  private final MenuKey menuKey;

  public PaperMenuHolder(UUID viewerId, MenuKey menuKey) {
    this.viewerId = Objects.requireNonNull(viewerId, "viewerId must not be null");
    this.menuKey = Objects.requireNonNull(menuKey, "menuKey must not be null");
  }

  public UUID viewerId() {
    return viewerId;
  }

  public MenuKey menuKey() {
    return menuKey;
  }

  @Override
  public Inventory getInventory() {
    return null; // Bukkit provides the Inventory instance separately.
  }
}