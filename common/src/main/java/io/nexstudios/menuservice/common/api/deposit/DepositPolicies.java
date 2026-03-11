package io.nexstudios.menuservice.common.api.deposit;

import java.util.Objects;
import java.util.Set;

/**
 * Factory methods for common deposit policy setups.
 */
public final class DepositPolicies {

  private DepositPolicies() {}

  public static DepositPolicy slots(Set<Integer> allowedTopSlots) {
    Objects.requireNonNull(allowedTopSlots, "allowedTopSlots must not be null");
    return new DepositPolicy(allowedTopSlots, DepositReturnStrategy.INVENTORY_THEN_DROP, false);
  }

  public static DepositPolicy slots(Set<Integer> allowedTopSlots, DepositReturnStrategy returnStrategy) {
    Objects.requireNonNull(allowedTopSlots, "allowedTopSlots must not be null");
    Objects.requireNonNull(returnStrategy, "returnStrategy must not be null");
    return new DepositPolicy(allowedTopSlots, returnStrategy, false);
  }

  public static DepositPolicy slotsWithBottomClickNotifications(Set<Integer> allowedTopSlots, DepositReturnStrategy returnStrategy) {
    Objects.requireNonNull(allowedTopSlots, "allowedTopSlots must not be null");
    Objects.requireNonNull(returnStrategy, "returnStrategy must not be null");
    return new DepositPolicy(allowedTopSlots, returnStrategy, true);
  }
}