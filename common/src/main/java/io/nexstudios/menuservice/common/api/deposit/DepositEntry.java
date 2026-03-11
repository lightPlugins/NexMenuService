package io.nexstudios.menuservice.common.api.deposit;

import io.nexstudios.menuservice.common.api.item.MenuItem;

import java.time.Instant;
import java.util.Objects;

/**
 * One deposited item tracked for safe returns.
 */
public record DepositEntry(
    int slot,
    MenuItem item,
    Instant depositedAt
) {
  public DepositEntry {
    Objects.requireNonNull(item, "item must not be null");
    Objects.requireNonNull(depositedAt, "depositedAt must not be null");
    if (slot < 0) throw new IllegalArgumentException("slot must be >= 0");
  }
}