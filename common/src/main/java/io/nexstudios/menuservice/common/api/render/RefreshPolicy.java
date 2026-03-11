package io.nexstudios.menuservice.common.api.render;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

/**
 * Controls refresh behavior for a menu view.
 */
public record RefreshPolicy(
    Optional<Duration> interval,
    Duration debounce
) {

  public RefreshPolicy {
    Objects.requireNonNull(interval, "interval must not be null");
    Objects.requireNonNull(debounce, "debounce must not be null");

    if (debounce.isNegative()) throw new IllegalArgumentException("debounce must not be negative");
    if (interval.isPresent()) {
      Duration i = interval.get();
      if (i.isNegative() || i.isZero()) throw new IllegalArgumentException("interval must be > 0 when present");
    }
  }

  public static RefreshPolicy defaultPolicy() {
    return new RefreshPolicy(Optional.empty(), Duration.ofMillis(50));
  }

  public static RefreshPolicy interval(Duration interval) {
    Objects.requireNonNull(interval, "interval must not be null");
    return new RefreshPolicy(Optional.of(interval), Duration.ofMillis(50));
  }
}