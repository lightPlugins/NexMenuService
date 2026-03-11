package io.nexstudios.menuservice.common.api.render;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Mutable render state (fingerprints per slot) used for diffing.
 */
public final class RenderState {

  private final Map<Integer, Long> fingerprintsBySlot = new HashMap<>();

  public long fingerprintAt(int slot) {
    return fingerprintsBySlot.getOrDefault(slot, 0L);
  }

  public void setFingerprint(int slot, long fingerprint) {
    fingerprintsBySlot.put(slot, fingerprint);
  }

  public void clearSlot(int slot) {
    fingerprintsBySlot.remove(slot);
  }

  public void clearAll() {
    fingerprintsBySlot.clear();
  }

  Map<Integer, Long> snapshot() {
    return Map.copyOf(fingerprintsBySlot);
  }
}