package io.nexstudios.menuservice.common.api.interaction;

import io.nexstudios.menuservice.common.api.InteractionPolicy;
import io.nexstudios.menuservice.common.api.deposit.DepositPolicy;

import java.util.Objects;
import java.util.Optional;

/**
 * Factory methods for common interaction policies.
 */
public final class InteractionPolicies {

  private InteractionPolicies() {}

  public static InteractionPolicy locked() {
    return new SimpleInteractionPolicy(true, false, Optional.empty());
  }

  public static InteractionPolicy lockedWithBottomClickNotifications() {
    return new SimpleInteractionPolicy(true, true, Optional.empty());
  }

  public static InteractionPolicy deposits(DepositPolicy depositPolicy) {
    Objects.requireNonNull(depositPolicy, "depositPolicy must not be null");
    return new SimpleInteractionPolicy(true, depositPolicy.notifyBottomClicks(), Optional.of(depositPolicy));
  }

  private record SimpleInteractionPolicy(
      boolean lockBottomInventoryByDefault,
      boolean notifyBottomInventoryClicks,
      Optional<DepositPolicy> depositPolicy
  ) implements InteractionPolicy {
    private SimpleInteractionPolicy {
      Objects.requireNonNull(depositPolicy, "depositPolicy must not be null");
    }
  }
}