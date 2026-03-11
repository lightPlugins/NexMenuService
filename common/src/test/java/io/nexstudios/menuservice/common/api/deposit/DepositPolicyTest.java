package io.nexstudios.menuservice.common.api.deposit;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DepositPolicyTest {

  @Test
  void shouldCreateValidPolicy() {
    DepositPolicy p = new DepositPolicy(Set.of(0, 1, 8), DepositReturnStrategy.INVENTORY_THEN_DROP, false);
    assertTrue(p.isSlotAllowed(0));
    assertTrue(p.isSlotAllowed(8));
    assertFalse(p.isSlotAllowed(2));
  }

  @Test
  void shouldRejectEmptySlots() {
    assertThrows(IllegalArgumentException.class, () ->
        new DepositPolicy(Set.of(), DepositReturnStrategy.INVENTORY_THEN_DROP, false)
    );
  }

  @Test
  void shouldRejectNegativeSlots() {
    assertThrows(IllegalArgumentException.class, () ->
        new DepositPolicy(Set.of(-1, 0), DepositReturnStrategy.INVENTORY_THEN_DROP, false)
    );
  }

  @Test
  void shouldRejectNulls() {
    assertThrows(NullPointerException.class, () ->
        new DepositPolicy(null, DepositReturnStrategy.INVENTORY_THEN_DROP, false)
    );
    assertThrows(NullPointerException.class, () ->
        new DepositPolicy(Set.of(0), null, false)
    );
  }
}