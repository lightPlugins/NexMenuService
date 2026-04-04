package io.nexstudios.menuservice.common.api.page;

import io.nexstudios.menuservice.common.api.item.MenuItem;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Defines where pagination controls are placed and how they look.
 */
public record PageNavigation(
    OptionalInt previousSlot,
    OptionalInt nextSlot,
    OptionalInt refreshSlot,
    Optional<MenuItem> previousItem,
    Optional<MenuItem> nextItem,
    Optional<MenuItem> refreshItem,
    boolean showCurrentPageAmount,
    boolean hidePreviousOnFirstPage,
    boolean hideNextOnLastPage
) {

  public PageNavigation {
    Objects.requireNonNull(previousSlot, "previousSlot must not be null");
    Objects.requireNonNull(nextSlot, "nextSlot must not be null");
    Objects.requireNonNull(refreshSlot, "refreshSlot must not be null");
    Objects.requireNonNull(previousItem, "previousItem must not be null");
    Objects.requireNonNull(nextItem, "nextItem must not be null");
    Objects.requireNonNull(refreshItem, "refreshItem must not be null");
    validate(previousSlot, "previousSlot");
    validate(nextSlot, "nextSlot");
    validate(refreshSlot, "refreshSlot");
  }

  public PageNavigation(
      OptionalInt previousSlot,
      OptionalInt nextSlot,
      OptionalInt refreshSlot
  ) {
    this(previousSlot, nextSlot, refreshSlot, Optional.empty(), Optional.empty(), Optional.empty(), false, true, true);
  }

  private static void validate(OptionalInt slot, String name) {
    if (slot.isPresent() && slot.getAsInt() < 0) {
      throw new IllegalArgumentException(name + " must be >= 0 when present");
    }
  }

  public static PageNavigationBuilder builder() {
    return new PageNavigationBuilder();
  }

  public static PageNavigation none() {
    return new PageNavigation(OptionalInt.empty(), OptionalInt.empty(), OptionalInt.empty());
  }
}