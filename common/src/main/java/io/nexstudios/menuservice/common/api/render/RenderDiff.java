package io.nexstudios.menuservice.common.api.render;

import io.nexstudios.menuservice.common.api.item.MenuItem;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Computes a {@link RenderPatch} by comparing a {@link RenderResult} against a {@link RenderState}.
 */
public final class RenderDiff {

  private RenderDiff() {}

  public static RenderPatch diff(RenderState state, RenderResult next) {
    Objects.requireNonNull(state, "state must not be null");
    Objects.requireNonNull(next, "next must not be null");

    Map<Integer, MenuItem> changed = new HashMap<>();
    Set<Integer> cleared = new HashSet<>();

    for (int slot : next.clearedSlots()) {
      long prev = state.fingerprintAt(slot);
      if (prev != 0L) {
        cleared.add(slot);
        state.clearSlot(slot);
      }
    }

    for (var e : next.slotsToItems().entrySet()) {
      int slot = e.getKey();
      MenuItem item = e.getValue();

      long fp = MenuItemFingerprinter.fingerprint(item);
      long prev = state.fingerprintAt(slot);

      if (fp != prev) {
        changed.put(slot, item);
        state.setFingerprint(slot, fp);
      }
    }

    return new RenderPatch(Map.copyOf(changed), Set.copyOf(cleared));
  }
}