package io.nexstudios.menuservice.common.api.deposit;

/**
 * Configures what should happen when deposited items must be returned to the player.
 */
public enum DepositReturnStrategy {
  /**
   * Try to add items back to the player's inventory. If it is full, drop them at the player's location.
   */
  INVENTORY_THEN_DROP,

  /**
   * Always drop deposited items at the player's location.
   */
  DROP_ONLY
}