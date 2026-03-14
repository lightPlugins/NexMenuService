package io.nexstudios.menuservice.common.api.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

/**
 * Bukkit-bound menu item.
 *
 * This type intentionally wraps a Bukkit ItemStack.
 */
public record MenuItem(ItemStack stack) {

  public MenuItem {
    Objects.requireNonNull(stack, "stack must not be null");
    if (stack.getType().isAir()) {
      throw new IllegalArgumentException("stack must not be AIR");
    }
    if (stack.getAmount() < 1) {
      throw new IllegalArgumentException("stack amount must be >= 1");
    }

    // Snapshot: do not retain caller-owned mutable instance
    stack = stack.clone();
  }

  @Override
  public ItemStack stack() {
    // Defensive copy: do not leak internal mutable state
    return stack.clone();
  }

  public static MenuItem of(ItemStack stack) {
    return new MenuItem(stack);
  }

  public static MenuItem of(Material material) {
    Objects.requireNonNull(material, "material must not be null");
    return new MenuItem(new ItemStack(material, 1));
  }
}