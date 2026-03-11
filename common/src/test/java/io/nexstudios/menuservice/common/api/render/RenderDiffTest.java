package io.nexstudios.menuservice.common.api.render;

import io.nexstudios.menuservice.common.api.item.MenuItem;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RenderDiffTest {

  @Test
  void diffShouldReturnChangedSlotsAndUpdateState() {
    RenderState state = new RenderState();

    MenuItem item = MenuItem.builder("minecraft:stone").amount(1).build();
    RenderResult next = new RenderResult(Map.of(0, item), Set.of());

    RenderPatch patch = RenderDiff.diff(state, next);

    assertEquals(Set.of(), patch.clearedSlots());
    assertEquals(Set.of(0), patch.changedSlots().keySet());
    assertNotEquals(0L, state.fingerprintAt(0));
  }

  @Test
  void diffShouldBeEmptyWhenNothingChanges() {
    RenderState state = new RenderState();

    MenuItem item = MenuItem.builder("minecraft:stone").amount(1).build();
    RenderResult first = new RenderResult(Map.of(0, item), Set.of());
    RenderDiff.diff(state, first);

    RenderResult second = new RenderResult(Map.of(0, item), Set.of());
    RenderPatch patch = RenderDiff.diff(state, second);

    assertTrue(patch.isEmpty());
  }

  @Test
  void diffShouldClearSlotWhenRequested() {
    RenderState state = new RenderState();

    MenuItem item = MenuItem.builder("minecraft:stone").amount(1).build();
    RenderDiff.diff(state, new RenderResult(Map.of(0, item), Set.of()));
    assertNotEquals(0L, state.fingerprintAt(0));

    RenderPatch patch = RenderDiff.diff(state, new RenderResult(Map.of(), Set.of(0)));

    assertEquals(Set.of(0), patch.clearedSlots());
    assertEquals(0L, state.fingerprintAt(0));
  }
}