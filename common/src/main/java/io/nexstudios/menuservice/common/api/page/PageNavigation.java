package io.nexstudios.menuservice.common.api.page;

import java.util.Objects;
import java.util.OptionalInt;

/**
 * Defines where navigation controls are placed (top inventory slots).
 */
public record PageNavigation(
    OptionalInt previousSlot,
    OptionalInt nextSlot,
    OptionalInt refreshSlot
) {
  public PageNavigation {
    Objects.requireNonNull(previousSlot, "previousSlot must not be null");
    Objects.requireNonNull(nextSlot, "nextSlot must not be null");
    Objects.requireNonNull(refreshSlot, "refreshSlot must not be null");
    validate(previousSlot, "previousSlot");
    validate(nextSlot, "nextSlot");
    validate(refreshSlot, "refreshSlot");
  }

  private static void validate(OptionalInt slot, String name) {
    if (slot.isPresent() && slot.getAsInt() < 0) {
      throw new IllegalArgumentException(name + " must be >= 0 when present");
    }
  }

  public static PageNavigation none() {
    return new PageNavigation(OptionalInt.empty(), OptionalInt.empty(), OptionalInt.empty());
  }
}