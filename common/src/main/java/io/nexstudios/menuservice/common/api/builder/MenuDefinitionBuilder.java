package io.nexstudios.menuservice.common.api.builder;

import io.nexstudios.menuservice.common.api.InteractionPolicy;
import io.nexstudios.menuservice.common.api.MenuDefinition;
import io.nexstudios.menuservice.common.api.MenuInteractionHooks;
import io.nexstudios.menuservice.common.api.MenuKey;
import io.nexstudios.menuservice.common.api.MenuPopulator;
import io.nexstudios.menuservice.common.api.deposit.DepositHandler;
import io.nexstudios.menuservice.common.api.item.MenuItem;
import io.nexstudios.menuservice.common.api.page.PagedAreaDefinition;
import io.nexstudios.menuservice.common.api.page.control.PageControlBinding;
import io.nexstudios.menuservice.common.api.page.control.PageControlButton;
import io.nexstudios.menuservice.common.api.page.control.PageFilterControl;
import io.nexstudios.menuservice.common.api.page.control.PageSortControl;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Builder for {@link MenuDefinition}.
 */
public final class MenuDefinitionBuilder {

  private MenuKey key;
  private String title;
  private int rows = 6;

  @Nullable
  private Duration refreshInterval;

  private InteractionPolicy interactionPolicy;
  private MenuPopulator populator;

  @Nullable
  private MenuInteractionHooks interactionHooks;

  @Nullable
  private DepositHandler depositHandler;

  private final List<PagedAreaDefinition<?>> pagedAreas = new ArrayList<>();

  @Nullable
  private MenuItem emptySlotFiller;

  private final List<PageControlBinding> pageControls = new ArrayList<>();
  private final List<PageControlButton> pageControlButtons = new ArrayList<>();

  private boolean decorationsEnabled = true;

  public static MenuDefinitionBuilder create() {
    return new MenuDefinitionBuilder();
  }

  public <T> MenuDefinitionBuilder addFilterControl(String areaId, PageFilterControl<T> control) {
    this.pageControls.add(new PageControlBinding(areaId, Objects.requireNonNull(control, "control must not be null")));
    return this;
  }

  public <T> MenuDefinitionBuilder addSortControl(String areaId, PageSortControl<T> control) {
    this.pageControls.add(new PageControlBinding(areaId, Objects.requireNonNull(control, "control must not be null")));
    return this;
  }

  public MenuDefinitionBuilder addControlButton(PageControlButton button) {
    this.pageControlButtons.add(Objects.requireNonNull(button, "button must not be null"));
    return this;
  }

  public MenuDefinitionBuilder clearPageControls() {
    this.pageControls.clear();
    return this;
  }

  public MenuDefinitionBuilder clearControlButtons() {
    this.pageControlButtons.clear();
    return this;
  }

  public MenuDefinitionBuilder key(MenuKey key) {
    this.key = Objects.requireNonNull(key, "key must not be null");
    return this;
  }

  public MenuDefinitionBuilder title(String title) {
    Objects.requireNonNull(title, "title must not be null");
    if (title.isBlank()) throw new IllegalArgumentException("title must not be blank");
    if (title.length() > 64) throw new IllegalArgumentException("title must not be longer than 64 characters");
    this.title = title;
    return this;
  }

  public MenuDefinitionBuilder rows(int rows) {
    if (rows < 1) throw new IllegalArgumentException("rows must be >= 1");
    if (rows > 6) throw new IllegalArgumentException("rows must be <= 6");
    this.rows = rows;
    return this;
  }

  public MenuDefinitionBuilder refreshInterval(Duration refreshInterval) {
    Objects.requireNonNull(refreshInterval, "refreshInterval must not be null");
    if (refreshInterval.isNegative() || refreshInterval.isZero()) {
      throw new IllegalArgumentException("refreshInterval must be > 0");
    }
    this.refreshInterval = refreshInterval;
    return this;
  }

  public MenuDefinitionBuilder noRefreshIntervalOverride() {
    this.refreshInterval = null;
    return this;
  }

  public MenuDefinitionBuilder interactionPolicy(InteractionPolicy interactionPolicy) {
    this.interactionPolicy = Objects.requireNonNull(interactionPolicy, "interactionPolicy must not be null");
    return this;
  }

  public MenuDefinitionBuilder populator(MenuPopulator populator) {
    this.populator = Objects.requireNonNull(populator, "populator must not be null");
    return this;
  }

  public MenuDefinitionBuilder interactionHooks(MenuInteractionHooks hooks) {
    this.interactionHooks = Objects.requireNonNull(hooks, "hooks must not be null");
    return this;
  }

  public MenuDefinitionBuilder noInteractionHooks() {
    this.interactionHooks = null;
    return this;
  }

  public MenuDefinitionBuilder depositHandler(DepositHandler handler) {
    this.depositHandler = Objects.requireNonNull(handler, "handler must not be null");
    return this;
  }

  public MenuDefinitionBuilder noDepositHandler() {
    this.depositHandler = null;
    return this;
  }

  public MenuDefinitionBuilder addPagedArea(PagedAreaDefinition<?> pagedArea) {
    this.pagedAreas.add(Objects.requireNonNull(pagedArea, "pagedArea must not be null"));
    return this;
  }

  public MenuDefinitionBuilder clearPagedAreas() {
    this.pagedAreas.clear();
    return this;
  }

  public MenuDefinitionBuilder fillEmptySlotsWith(MenuItem filler) {
    this.emptySlotFiller = Objects.requireNonNull(filler, "filler must not be null");
    return this;
  }

  public MenuDefinitionBuilder noEmptySlotFiller() {
    this.emptySlotFiller = null;
    return this;
  }

  /**
   * Disables/enables decorations for this menu definition.
   *
   * @param disabled true to disable decorations, false to enable them
   */
  public MenuDefinitionBuilder disableDecorations(boolean disabled) {
    this.decorationsEnabled = !disabled;
    return this;
  }

  public MenuDefinition build() {
    Objects.requireNonNull(key, "key must not be null");
    Objects.requireNonNull(title, "title must not be null");
    Objects.requireNonNull(interactionPolicy, "interactionPolicy must not be null");
    Objects.requireNonNull(populator, "populator must not be null");

    final MenuKey builtKey = key;
    final String builtTitle = title;
    final int builtRows = rows;

    final Optional<Duration> builtRefreshInterval = Optional.ofNullable(refreshInterval);
    final InteractionPolicy builtInteractionPolicy = interactionPolicy;
    final MenuPopulator builtPopulator = populator;

    final Optional<MenuInteractionHooks> builtHooks = Optional.ofNullable(interactionHooks);
    final Optional<DepositHandler> builtDepositHandler = Optional.ofNullable(depositHandler);

    final Optional<List<PagedAreaDefinition<?>>> builtPagedAreas =
        pagedAreas.isEmpty() ? Optional.empty() : Optional.of(List.copyOf(pagedAreas));

    final Optional<List<PageControlBinding>> builtControls =
        pageControls.isEmpty() ? Optional.empty() : Optional.of(List.copyOf(pageControls));

    final Optional<List<PageControlButton>> builtButtons =
        pageControlButtons.isEmpty() ? Optional.empty() : Optional.of(List.copyOf(pageControlButtons));

    final Optional<MenuItem> builtEmptySlotFiller = Optional.ofNullable(emptySlotFiller);

    final boolean builtDecorationsEnabled = decorationsEnabled;

    return new MenuDefinition() {
      @Override public MenuKey key() { return builtKey; }
      @Override public String title() { return builtTitle; }
      @Override public int rows() { return builtRows; }
      @Override public Optional<Duration> refreshInterval() { return builtRefreshInterval; }
      @Override public InteractionPolicy interactionPolicy() { return builtInteractionPolicy; }
      @Override public MenuPopulator populator() { return builtPopulator; }
      @Override public Optional<MenuInteractionHooks> interactionHooks() { return builtHooks; }
      @Override public Optional<DepositHandler> depositHandler() { return builtDepositHandler; }
      @Override
      public Optional<List<PagedAreaDefinition<?>>> pagedAreas() { return builtPagedAreas; }

      @Override
      public Optional<List<PageControlBinding>> pageControls() { return builtControls; }

      @Override
      public Optional<List<PageControlButton>> pageControlButtons() { return builtButtons; }

      @Override public Optional<MenuItem> emptySlotFiller() { return builtEmptySlotFiller; }

      @Override
      public boolean decorationsEnabled() {
        return builtDecorationsEnabled;
      }

      @Override
      public String toString() {
        return "MenuDefinition[key=" + builtKey.asString() + ",rows=" + builtRows + "]";
      }
    };
  }
}