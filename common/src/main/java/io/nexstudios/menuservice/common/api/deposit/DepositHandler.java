package io.nexstudios.menuservice.common.api.deposit;

import io.nexstudios.menuservice.common.api.MenuKey;
import io.nexstudios.menuservice.common.api.ViewerRef;
import io.nexstudios.menuservice.common.api.interaction.InteractionContext;
import io.nexstudios.menuservice.common.api.item.MenuItem;

import java.util.Objects;
import java.util.Optional;

/**
 * Handles deposits (moving items from the player's inventory into menu slots).
 */
public interface DepositHandler {

  /**
   * Called when a player attempts to place an item into a deposit-enabled top slot.
   *
   * @param slot the top inventory slot index
   * @param offered the item the player tries to place
   * @param interaction interaction snapshot (shift-click, number-key, etc.)
   * @return a decision that determines whether the item is accepted and optionally transformed
   */
  DepositDecision onPlace(MenuKey menuKey, ViewerRef viewer, int slot, MenuItem offered, InteractionContext interaction);

  /**
   * Called when a player attempts to remove an item from a deposit-enabled top slot.
   *
   * @param slot the top inventory slot index
   * @param current the current item in that slot (snapshot)
   * @param interaction interaction snapshot
   * @return a decision that determines whether removal is allowed
   */
  RemovalDecision onRemove(MenuKey menuKey, ViewerRef viewer, int slot, MenuItem current, InteractionContext interaction);

  record DepositDecision(boolean allowed, MenuItem resultingItemOrNull) {
    public DepositDecision {
      if (!allowed && resultingItemOrNull != null) {
        throw new IllegalArgumentException("resultingItemOrNull must be null when allowed is false");
      }
    }

    public static DepositDecision deny() {
      return new DepositDecision(false, null);
    }

    public static DepositDecision accept() {
      return new DepositDecision(true, null);
    }

    public static DepositDecision acceptTransformed(MenuItem resultingItem) {
      Objects.requireNonNull(resultingItem, "resultingItem must not be null");
      return new DepositDecision(true, resultingItem);
    }

    public Optional<MenuItem> resultingItem() {
      return Optional.ofNullable(resultingItemOrNull);
    }
  }

  record RemovalDecision(boolean allowed) {
    public static RemovalDecision deny() {
      return new RemovalDecision(false);
    }

    public static RemovalDecision allow() {
      return new RemovalDecision(true);
    }
  }
}