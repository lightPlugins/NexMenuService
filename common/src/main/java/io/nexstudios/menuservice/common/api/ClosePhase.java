package io.nexstudios.menuservice.common.api;

/**
 * Phase information for close hooks.
 */
public enum ClosePhase {
  /**
   * Fired early during close: after the view was detached from the service,
   * but before deposit-return and before closing the Bukkit inventory.
   */
  BEFORE_CLOSE,

  /**
   * Fired late during close: after deposit-return (if any) and after triggering inventory close.
   */
  AFTER_CLOSE
}