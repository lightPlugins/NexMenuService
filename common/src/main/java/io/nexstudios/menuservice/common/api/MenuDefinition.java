package io.nexstudios.menuservice.common.api;

import io.nexstudios.menuservice.common.api.deposit.DepositHandler;
import io.nexstudios.menuservice.common.api.item.MenuItem;
import io.nexstudios.menuservice.common.api.page.PagedAreaDefinition;
import io.nexstudios.menuservice.common.api.page.control.PageControlBinding;
import io.nexstudios.menuservice.common.api.page.control.PageControlButton;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * A menu blueprint (definition) registered in the registry.
 */
public interface MenuDefinition {

  MenuKey key();

  String title();

  int rows();

  /**
   * If empty, the implementation uses a global/default refresh interval.
   */
  Optional<Duration> refreshInterval();

  InteractionPolicy interactionPolicy();

  MenuPopulator populator();

  Optional<MenuInteractionHooks> interactionHooks();

  Optional<DepositHandler> depositHandler();

  /**
   * Optional list of paged areas declared for this menu.
   */
  Optional<List<PagedAreaDefinition<?>>> pagedAreas();

  /**
   * Optional page controls (filters/sorts) bound to paged areas.
   */
  Optional<List<PageControlBinding>> pageControls();

  /**
   * Optional UI buttons for controls.
   */
  Optional<List<PageControlButton>> pageControlButtons();

  /**
   * Optional filler item for empty slots.
   * Applied at the very end of rendering (after populator + paging + navigation),
   * so it decorates all remaining empty fields.
   */
  Optional<MenuItem> emptySlotFiller();

  /**
   * Controls whether menu decorations (e.g. empty-slot filler) are applied.
   * Default: enabled.
   */
  default boolean decorationsEnabled() {
    return true;
  }
}