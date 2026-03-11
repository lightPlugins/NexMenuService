package io.nexstudios.menuservice.common.api.registry;

/**
 * Defines how the registry should handle duplicate menu keys.
 */
public enum DuplicateStrategy {
  /**
   * Reject duplicates and throw an exception.
   */
  FAIL,

  /**
   * Replace the existing definition with the new one.
   */
  REPLACE
}