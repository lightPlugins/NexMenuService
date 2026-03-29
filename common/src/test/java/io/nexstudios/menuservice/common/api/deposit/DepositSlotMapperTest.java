package io.nexstudios.menuservice.common.api.deposit;

import io.nexstudios.menuservice.common.api.layout.SlotIndex;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DepositSlotMapperTest {

  @Test
  void areaToSlots_singleCell() {
    Set<Integer> slots = DepositSlotMapper.areaToSlots(0, 0, 1, 1);
    assertEquals(Set.of(0), slots);
  }

  @Test
  void areaToSlots_3x3() {
    Set<Integer> slots = DepositSlotMapper.areaToSlots(0, 0, 3, 3);
    
    // Row 0: slots 0, 1, 2
    // Row 1: slots 9, 10, 11
    // Row 2: slots 18, 19, 20
    Set<Integer> expected = Set.of(
        0, 1, 2,
        9, 10, 11,
        18, 19, 20
    );
    assertEquals(expected, slots);
  }

  @Test
  void areaToSlots_withOffset() {
    Set<Integer> slots = DepositSlotMapper.areaToSlots(3, 1, 2, 2);
    
    // Row 1, Col 3-4: slots 12, 13
    // Row 2, Col 3-4: slots 21, 22
    Set<Integer> expected = Set.of(12, 13, 21, 22);
    assertEquals(expected, slots);
  }

  @Test
  void areaToSlots_fullRow() {
    Set<Integer> slots = DepositSlotMapper.areaToSlots(0, 0, 9, 1);
    
    Set<Integer> expected = Set.of(0, 1, 2, 3, 4, 5, 6, 7, 8);
    assertEquals(expected, slots);
  }

  @Test
  void areaToSlots_invalidX() {
    assertThrows(IllegalArgumentException.class, () ->
        DepositSlotMapper.areaToSlots(-1, 0, 3, 3)
    );
  }

  @Test
  void areaToSlots_invalidY() {
    assertThrows(IllegalArgumentException.class, () ->
        DepositSlotMapper.areaToSlots(0, -1, 3, 3)
    );
  }

  @Test
  void areaToSlots_invalidWidth() {
    assertThrows(IllegalArgumentException.class, () ->
        DepositSlotMapper.areaToSlots(0, 0, 0, 3)
    );
  }

  @Test
  void areaToSlots_invalidHeight() {
    assertThrows(IllegalArgumentException.class, () ->
        DepositSlotMapper.areaToSlots(0, 0, 3, 0)
    );
  }

  @Test
  void areaToSlots_outOfBounds() {
    assertThrows(IllegalArgumentException.class, () ->
        DepositSlotMapper.areaToSlots(7, 0, 3, 1)  // 7 + 3 = 10 > 9
    );
  }

  @Test
  void multiAreaToSlots_twoAreas() {
    Set<Integer> slots = DepositSlotMapper.multiAreaToSlots(
        0, 0, 3, 1,  // First area: 3 slots in row 0
        6, 0, 3, 1   // Second area: 3 slots in row 0
    );
    
    Set<Integer> expected = Set.of(0, 1, 2, 6, 7, 8);
    assertEquals(expected, slots);
  }

  @Test
  void multiAreaToSlots_invalidLength() {
    assertThrows(IllegalArgumentException.class, () ->
        DepositSlotMapper.multiAreaToSlots(0, 0, 3)  // Missing height
    );
  }

  @Test
  void rowToSlots() {
    Set<Integer> slots = DepositSlotMapper.rowToSlots(2, 1, 4);
    
    // Row 1, Col 2-5: slots 11, 12, 13, 14
    Set<Integer> expected = Set.of(11, 12, 13, 14);
    assertEquals(expected, slots);
  }

  @Test
  void columnToSlots() {
    Set<Integer> slots = DepositSlotMapper.columnToSlots(3, 0, 3);
    
    // Col 3, Row 0-2: slots 3, 12, 21
    Set<Integer> expected = Set.of(3, 12, 21);
    assertEquals(expected, slots);
  }

  @Test
  void fullRowToSlots() {
    Set<Integer> slots = DepositSlotMapper.fullRowToSlots(0);
    
    Set<Integer> expected = Set.of(0, 1, 2, 3, 4, 5, 6, 7, 8);
    assertEquals(expected, slots);
  }

  @Test
  void fullRowToSlots_row2() {
    Set<Integer> slots = DepositSlotMapper.fullRowToSlots(2);
    
    Set<Integer> expected = Set.of(18, 19, 20, 21, 22, 23, 24, 25, 26);
    assertEquals(expected, slots);
  }

  @Test
  void coordinatesToSlots_single() {
    Set<Integer> slots = DepositSlotMapper.coordinatesToSlots(0, 0);
    
    assertEquals(Set.of(0), slots);
  }

  @Test
  void coordinatesToSlots_multiple() {
    Set<Integer> slots = DepositSlotMapper.coordinatesToSlots(
        0, 0,  // slot 0
        8, 0,  // slot 8
        0, 2,  // slot 18
        8, 2   // slot 26
    );
    
    Set<Integer> expected = Set.of(0, 8, 18, 26);
    assertEquals(expected, slots);
  }

  @Test
  void coordinatesToSlots_invalidLength() {
    assertThrows(IllegalArgumentException.class, () ->
        DepositSlotMapper.coordinatesToSlots(0, 0, 1)  // Missing y for second coordinate
    );
  }

  @Test
  void equivalence_area3x3_vs_manual() {
    Set<Integer> byCoordinates = DepositSlotMapper.areaToSlots(0, 0, 3, 3);
    Set<Integer> manual = Set.of(0, 1, 2, 9, 10, 11, 18, 19, 20);
    
    assertEquals(manual, byCoordinates);
  }

  @Test
  void equivalence_fullRow_vs_rowToSlots() {
    Set<Integer> full = DepositSlotMapper.fullRowToSlots(1);
    Set<Integer> partial = DepositSlotMapper.rowToSlots(0, 1, 9);
    
    assertEquals(full, partial);
  }
}

