package io.nexstudios.menuservice.common.api;

import io.nexstudios.menuservice.common.api.item.MenuItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Global defaults for menu rendering behavior.
 *
 * Host plugins may configure these once on startup.
 */
public final class MenuDefaults {

  private static final AtomicReference<MenuItem> DEFAULT_EMPTY_SLOT_FILLER = new AtomicReference<>(null);

  private MenuDefaults() {}

  // Lazily initialize to avoid early Bukkit/Registry touching during class-load
  private static MenuItem hardcodedEmptySlotFiller() {
    return MenuItem.of(new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1));
  }

  public static Optional<MenuItem> configuredEmptySlotFiller() {
    return Optional.ofNullable(DEFAULT_EMPTY_SLOT_FILLER.get());
  }

  public static MenuItem defaultEmptySlotFiller() {
    MenuItem configured = DEFAULT_EMPTY_SLOT_FILLER.get();
    return configured != null ? configured : hardcodedEmptySlotFiller();
  }

  public static void setDefaultEmptySlotFiller(MenuItem fillerOrNull) {
    DEFAULT_EMPTY_SLOT_FILLER.set(fillerOrNull);
  }

  public static void clearDefaultEmptySlotFiller() {
    DEFAULT_EMPTY_SLOT_FILLER.set(null);
  }
}