package io.nexstudios.menuservice.common.api.deposit;

import io.nexstudios.menuservice.common.api.item.MenuItem;

import java.util.Optional;

/**
 * Tracks deposits for a single viewer/menu view.
 *
 * Implementations must be thread-safe if accessed from async render/update code.
 */
public interface DepositLedger {

  /**
   * Records that an item was deposited into a top slot.
   */
  void recordDeposit(int slot, MenuItem item);

  /**
   * Clears a deposit record for a slot (e.g., item removed or overwritten).
   */
  void clearDeposit(int slot);

  /**
   * Returns the currently recorded deposited item for a slot, if any.
   */
  Optional<MenuItem> findDepositedItem(int slot);

  /**
   * Snapshot used when returning items on close/disable/disconnect.
   */
  DepositLedgerSnapshot snapshot();

  /**
   * Clears all tracked deposits after a successful return.
   */
  void clearAll();
}