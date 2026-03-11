package io.nexstudios.menuservice.common.api.deposit;

import java.util.Objects;
import java.util.Set;

/**
 * Enables deposit mechanics for a set of top inventory slots.
 */
public record DepositPolicy(
    Set<Integer> allowedTopSlots,
    DepositReturnStrategy returnStrategy,
    boolean notifyBottomClicks
) {

  public DepositPolicy {
    Objects.requireNonNull(allowedTopSlots, "allowedTopSlots must not be null");
    Objects.requireNonNull(returnStrategy, "returnStrategy must not be null");
    if (allowedTopSlots.isEmpty()) {
      throw new IllegalArgumentException("allowedTopSlots must not be empty");
    }
    for (int slot : allowedTopSlots) {
      if (slot < 0) throw new IllegalArgumentException("allowedTopSlots must not contain negative slot indices");
    }
  }

  public boolean isSlotAllowed(int slot) {
    return allowedTopSlots.contains(slot);
  }
}