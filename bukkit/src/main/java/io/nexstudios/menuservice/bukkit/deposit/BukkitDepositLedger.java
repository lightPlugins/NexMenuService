package io.nexstudios.menuservice.bukkit.deposit;

import io.nexstudios.menuservice.common.api.deposit.DepositEntry;
import io.nexstudios.menuservice.common.api.deposit.DepositLedger;
import io.nexstudios.menuservice.common.api.deposit.DepositLedgerSnapshot;
import io.nexstudios.menuservice.common.api.item.MenuItem;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class BukkitDepositLedger implements DepositLedger {

  private final ConcurrentMap<Integer, DepositEntry> bySlot = new ConcurrentHashMap<>();

  @Override
  public void recordDeposit(int slot, MenuItem item) {
    if (slot < 0) throw new IllegalArgumentException("slot must be >= 0");
    Objects.requireNonNull(item, "item must not be null");
    bySlot.put(slot, new DepositEntry(slot, item, Instant.now()));
  }

  @Override
  public void clearDeposit(int slot) {
    bySlot.remove(slot);
  }

  @Override
  public Optional<MenuItem> findDepositedItem(int slot) {
    DepositEntry e = bySlot.get(slot);
    if (e == null) return Optional.empty();
    return Optional.of(e.item());
  }

  @Override
  public DepositLedgerSnapshot snapshot() {
    return new DepositLedgerSnapshot(new ArrayList<>(bySlot.values()));
  }

  @Override
  public void clearAll() {
    bySlot.clear();
  }
}

