package io.nexstudios.menuservice.bukkit.interaction;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Simple smart throttling for GUI interactions:
 * - Allow up to maxActionsPerWindow within windowMillis
 * - If exceeded, deny until blockedUntilMillis (cooldown)
 */
public final class ClickThrottler {

  private final int maxActionsPerWindow;
  private final long windowMillis;
  private final long cooldownMillis;

  private final ConcurrentMap<UUID, State> states = new ConcurrentHashMap<>();

  public ClickThrottler(int maxActionsPerWindow, long windowMillis, long cooldownMillis) {
    if (maxActionsPerWindow < 1) throw new IllegalArgumentException("maxActionsPerWindow must be >= 1");
    if (windowMillis < 1) throw new IllegalArgumentException("windowMillis must be >= 1");
    if (cooldownMillis < 1) throw new IllegalArgumentException("cooldownMillis must be >= 1");
    this.maxActionsPerWindow = maxActionsPerWindow;
    this.windowMillis = windowMillis;
    this.cooldownMillis = cooldownMillis;
  }

  /**
   * @return true if the interaction should be processed, false if it should be ignored (throttled).
   */
  public boolean allow(UUID viewerId, long nowMillis) {
    if (viewerId == null) return true;

    State s = states.computeIfAbsent(viewerId, id -> new State(nowMillis));

    synchronized (s) {
      // Active cooldown?
      if (nowMillis < s.blockedUntilMillis) {
        return false;
      }

      // Window expired -> reset
      if (nowMillis - s.windowStartMillis >= windowMillis) {
        s.windowStartMillis = nowMillis;
        s.actionsInWindow = 0;
      }

      s.actionsInWindow++;

      // Exceeded -> start cooldown
      if (s.actionsInWindow > maxActionsPerWindow) {
        s.blockedUntilMillis = nowMillis + cooldownMillis;

        // Reset counter to avoid immediate re-trigger loops after cooldown ends
        s.windowStartMillis = nowMillis;
        s.actionsInWindow = 0;

        return false;
      }

      return true;
    }
  }

  public void clear(UUID viewerId) {
    if (viewerId == null) return;
    states.remove(viewerId);
  }

  public void cleanupStaleEntries(long nowMillis, long staleThresholdMillis) {
    states.entrySet().removeIf(e -> {
      State s = e.getValue();
      synchronized (s) {
        return (nowMillis - s.windowStartMillis) > staleThresholdMillis && s.blockedUntilMillis < nowMillis;
      }
    });
  }

  private static final class State {
    private long windowStartMillis;
    private int actionsInWindow;
    private long blockedUntilMillis;

    private State(long nowMillis) {
      this.windowStartMillis = nowMillis;
      this.actionsInWindow = 0;
      this.blockedUntilMillis = 0L;
    }
  }
}