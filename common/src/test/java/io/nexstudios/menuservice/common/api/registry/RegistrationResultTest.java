package io.nexstudios.menuservice.common.api.registry;

import io.nexstudios.menuservice.common.api.*;
import io.nexstudios.menuservice.common.api.deposit.DepositHandler;
import io.nexstudios.menuservice.common.api.item.MenuItem;
import io.nexstudios.menuservice.common.api.page.PagedAreaDefinition;
import io.nexstudios.menuservice.common.api.page.control.PageControlBinding;
import io.nexstudios.menuservice.common.api.page.control.PageControlButton;
import org.junit.jupiter.api.Test;

import java.util.List;
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
      @Override public Optional<java.time.Duration> refreshInterval() { return java.util.Optional.empty(); }
      @Override public InteractionPolicy interactionPolicy() { throw new UnsupportedOperationException(); }
      @Override public MenuPopulator populator() { throw new UnsupportedOperationException(); }
      @Override public Optional<MenuInteractionHooks> interactionHooks() { return Optional.empty(); }
      @Override public Optional<DepositHandler> depositHandler() { return Optional.empty(); }
      @Override public Optional<List<PagedAreaDefinition<?>>> pagedAreas() { return Optional.empty(); }

      @Override
      public Optional<List<PageControlBinding>> pageControls() {
        return Optional.empty();
      }

      @Override
      public Optional<List<PageControlButton>> pageControlButtons() {
        return Optional.empty();
      }

      @Override
      public Optional<MenuItem> emptySlotFiller() {
        return Optional.empty();
      }
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
          @Override public Optional<java.time.Duration> refreshInterval() { return Optional.empty(); }
          @Override public InteractionPolicy interactionPolicy() { throw new UnsupportedOperationException(); }
          @Override public MenuPopulator populator() { throw new UnsupportedOperationException(); }
          @Override public Optional<MenuInteractionHooks> interactionHooks() { return Optional.empty(); }
          @Override public Optional<DepositHandler> depositHandler() { return Optional.empty(); }
          @Override public Optional<List<PagedAreaDefinition<?>>> pagedAreas() { return Optional.empty(); }

          @Override
          public Optional<List<PageControlBinding>> pageControls() {
            return Optional.empty();
          }

          @Override
          public Optional<List<PageControlButton>> pageControlButtons() {
            return java.util.Optional.empty();
          }

          @Override
          public Optional<MenuItem> emptySlotFiller() {
            return Optional.empty();
          }
        }))
    );

    assertThrows(IllegalArgumentException.class, () ->
        new RegistrationResult(key, true, Optional.empty())
    );
  }
}