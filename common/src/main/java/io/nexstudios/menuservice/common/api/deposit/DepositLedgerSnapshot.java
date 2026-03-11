package io.nexstudios.menuservice.common.api.deposit;

import java.util.List;
import java.util.Objects;

/**
 * Immutable snapshot of tracked deposits.
 */
public record DepositLedgerSnapshot(List<DepositEntry> entries) {

  public DepositLedgerSnapshot {
    Objects.requireNonNull(entries, "entries must not be null");
  }

  public static DepositLedgerSnapshot empty() {
    return new DepositLedgerSnapshot(List.of());
  }

  public boolean isEmpty() {
    return entries.isEmpty();
  }
}