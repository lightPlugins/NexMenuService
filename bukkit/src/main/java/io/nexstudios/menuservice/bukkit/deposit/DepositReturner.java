package io.nexstudios.menuservice.bukkit.deposit;

import io.nexstudios.menuservice.common.api.deposit.DepositEntry;
import io.nexstudios.menuservice.common.api.deposit.DepositLedgerSnapshot;
import io.nexstudios.menuservice.common.api.deposit.DepositReturnStrategy;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Objects;

public final class DepositReturner {

  public DepositReturner() {}

  public void returnDeposits(Player player, DepositLedgerSnapshot snapshot, DepositReturnStrategy strategy) {
    Objects.requireNonNull(player, "player must not be null");
    Objects.requireNonNull(snapshot, "snapshot must not be null");
    Objects.requireNonNull(strategy, "strategy must not be null");

    if (snapshot.isEmpty()) return;

    Location loc = player.getLocation();

    for (DepositEntry entry : snapshot.entries()) {
      ItemStack stack = entry.item().stack().clone();

      switch (strategy) {
        case INVENTORY_THEN_DROP -> {
          Map<Integer, ItemStack> leftover = player.getInventory().addItem(stack);
          if (!leftover.isEmpty()) {
            for (ItemStack left : leftover.values()) {
              if (left == null || left.getType().isAir()) continue;
              player.getWorld().dropItemNaturally(loc, left);
            }
          }
        }
        case DROP_ONLY -> player.getWorld().dropItemNaturally(loc, stack);
      }
    }
  }
}