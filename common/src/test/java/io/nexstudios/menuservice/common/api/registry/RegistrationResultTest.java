package io.nexstudios.menuservice.common.api.registry;

import io.nexstudios.menuservice.common.api.MenuDefinition;
import io.nexstudios.menuservice.common.api.MenuKey;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RegistrationResultTest {

  @Test
  void addedShouldHaveNoPrevious() {
    MenuKey key = MenuKey.of("nex", "m1");
    RegistrationResult r = RegistrationResult.added(key);

    assertEquals(key, r.key());
    assertFalse(r.replaced());
    assertEquals(Optional.empty(), r.previous());
  }

  @Test
  void replacedShouldHavePrevious() {
    MenuKey key = MenuKey.of("nex", "m1");
    MenuDefinition prev = new MenuDefinition() {
      @Override public MenuKey key() { return key; }
      @Override public String title() { return "x"; }
      @Override public int rows() { return 1; }
      @Override public java.util.Optional<java.time.Duration> refreshInterval() { return java.util.Optional.empty(); }
      @Override public io.nexstudios.menuservice.common.api.InteractionPolicy interactionPolicy() { throw new UnsupportedOperationException(); }
      @Override public io.nexstudios.menuservice.common.api.MenuPopulator populator() { throw new UnsupportedOperationException(); }
      @Override public java.util.Optional<io.nexstudios.menuservice.common.api.MenuInteractionHooks> interactionHooks() { return java.util.Optional.empty(); }
      @Override public java.util.Optional<io.nexstudios.menuservice.common.api.deposit.DepositHandler> depositHandler() { return java.util.Optional.empty(); }
      @Override public java.util.Optional<java.util.List<io.nexstudios.menuservice.common.api.page.PagedAreaDefinition<?>>> pagedAreas() { return java.util.Optional.empty(); }
    };

    RegistrationResult r = RegistrationResult.replaced(key, prev);

    assertEquals(key, r.key());
    assertTrue(r.replaced());
    assertTrue(r.previous().isPresent());
    assertSame(prev, r.previous().get());
  }

  @Test
  void constructorShouldEnforceInvariants() {
    MenuKey key = MenuKey.of("nex", "m1");

    assertThrows(NullPointerException.class, () ->
        new RegistrationResult(key, false, null)
    );

    assertThrows(IllegalArgumentException.class, () ->
        new RegistrationResult(key, false, Optional.of(new MenuDefinition() {
          @Override public MenuKey key() { return key; }
          @Override public String title() { return "x"; }
          @Override public int rows() { return 1; }
          @Override public java.util.Optional<java.time.Duration> refreshInterval() { return java.util.Optional.empty(); }
          @Override public io.nexstudios.menuservice.common.api.InteractionPolicy interactionPolicy() { throw new UnsupportedOperationException(); }
          @Override public io.nexstudios.menuservice.common.api.MenuPopulator populator() { throw new UnsupportedOperationException(); }
          @Override public java.util.Optional<io.nexstudios.menuservice.common.api.MenuInteractionHooks> interactionHooks() { return java.util.Optional.empty(); }
          @Override public java.util.Optional<io.nexstudios.menuservice.common.api.deposit.DepositHandler> depositHandler() { return java.util.Optional.empty(); }
          @Override public java.util.Optional<java.util.List<io.nexstudios.menuservice.common.api.page.PagedAreaDefinition<?>>> pagedAreas() { return java.util.Optional.empty(); }
        }))
    );

    assertThrows(IllegalArgumentException.class, () ->
        new RegistrationResult(key, true, Optional.empty())
    );
  }
}