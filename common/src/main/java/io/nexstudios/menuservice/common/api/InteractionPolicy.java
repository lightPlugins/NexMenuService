package io.nexstudios.menuservice.common.api;

import io.nexstudios.menuservice.common.api.deposit.DepositPolicy;

import java.util.Optional;

/**
 * Controls which interactions are allowed.
 */
public interface InteractionPolicy {

  /**
   * If true, player inventory interactions are blocked by default.
   */
  boolean lockBottomInventoryByDefault();

  /**
   * If true, clicks in the bottom inventory should be forwarded to hooks (if registered).
   */
  boolean notifyBottomInventoryClicks();

  /**
   * Optional deposit policy. If present, moving items from bottom -> top into allowed slots is supported.
   */
  Optional<DepositPolicy> depositPolicy();
}