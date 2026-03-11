package io.nexstudios.menuservice.common.api;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ViewerRefTest {

  @Test
  void shouldCreateViewerRef() {
    UUID id = UUID.randomUUID();
    ViewerRef v = ViewerRef.of(id, "Phil");

    assertEquals(id, v.uniqueId());
    assertEquals("Phil", v.name());
    assertTrue(v.toString().contains("Phil"));
  }

  @Test
  void shouldRejectBlankName() {
    assertThrows(IllegalArgumentException.class, () -> ViewerRef.of(UUID.randomUUID(), " "));
  }

  @Test
  void shouldRejectNulls() {
    assertThrows(NullPointerException.class, () -> ViewerRef.of(null, "Phil"));
    assertThrows(NullPointerException.class, () -> ViewerRef.of(UUID.randomUUID(), null));
  }
}