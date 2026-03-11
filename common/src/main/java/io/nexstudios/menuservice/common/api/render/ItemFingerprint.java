package io.nexstudios.menuservice.common.api.render;

/**
 * 64-bit fingerprint used for diffing render states.
 */
@FunctionalInterface
public interface ItemFingerprint {
  long value();

  static ItemFingerprint of(long value) {
    return () -> value;
  }
}