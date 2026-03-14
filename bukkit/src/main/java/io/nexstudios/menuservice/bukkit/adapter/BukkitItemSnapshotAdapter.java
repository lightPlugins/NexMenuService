package io.nexstudios.menuservice.bukkit.adapter;

import io.nexstudios.menuservice.common.api.item.MenuItem;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

/**
 * Converts Bukkit {@link ItemStack} into a {@link MenuItem} snapshot.
 *
 * This is used for interaction hooks (e.g. bottom inventory click reporting).
 */
public final class BukkitItemSnapshotAdapter {

  public Optional<MenuItem> toMenuItemSnapshot(ItemStack stack) {
    if (stack == null) return Optional.empty();
    if (stack.getType().isAir()) return Optional.empty();
    if (stack.getAmount() <= 0) return Optional.empty();

    return Optional.of(MenuItem.of(stack.clone()));
  }
}