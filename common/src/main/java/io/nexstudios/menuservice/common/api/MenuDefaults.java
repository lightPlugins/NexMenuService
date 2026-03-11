package io.nexstudios.menuservice.common.api;

import io.nexstudios.menuservice.common.api.item.MenuItem;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Global defaults for menu rendering behavior.
 *
 * Host plugins may configure these once on startup.
 */
public final class MenuDefaults {

  private static final MenuItem HARDCODED_EMPTY_SLOT_FILLER = MenuItem.builder("minecraft:black_stained_glass_pane")
      .displayName(null)
      .lore(List.of())
      .build();

  private static final AtomicReference<MenuItem> DEFAULT_EMPTY_SLOT_FILLER = new AtomicReference<>(null);

  private MenuDefaults() {}

  /**
   * Returns the globally configured empty-slot filler, if any (override).
   */
  public static Optional<MenuItem> configuredEmptySlotFiller() {
    return Optional.ofNullable(DEFAULT_EMPTY_SLOT_FILLER.get());
  }

  /**
   * Returns the default empty-slot filler.
   * If the host plugin has configured an override, that override is returned.
   * Otherwise, a hardcoded default is returned.
   */
  public static MenuItem defaultEmptySlotFiller() {
    MenuItem configured = DEFAULT_EMPTY_SLOT_FILLER.get();
    return configured != null ? configured : HARDCODED_EMPTY_SLOT_FILLER;
  }

  /**
   * Sets a global default empty-slot filler item (override).
   * Passing null clears the override (falls back to hardcoded default).
   */
  public static void setDefaultEmptySlotFiller(MenuItem fillerOrNull) {
    DEFAULT_EMPTY_SLOT_FILLER.set(fillerOrNull);
  }

  /**
   * Clears the override (falls back to hardcoded default).
   */
  public static void clearDefaultEmptySlotFiller() {
    DEFAULT_EMPTY_SLOT_FILLER.set(null);
  }
}