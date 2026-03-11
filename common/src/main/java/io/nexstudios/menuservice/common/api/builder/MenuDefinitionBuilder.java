package io.nexstudios.menuservice.common.api.builder;

import io.nexstudios.menuservice.common.api.*;
import io.nexstudios.menuservice.common.api.deposit.DepositHandler;
import io.nexstudios.menuservice.common.api.page.PagedAreaDefinition;

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

  private Optional<Duration> refreshInterval = Optional.empty();
  private InteractionPolicy interactionPolicy;
  private MenuPopulator populator;

  private Optional<MenuInteractionHooks> interactionHooks = Optional.empty();
  private Optional<DepositHandler> depositHandler = Optional.empty();
  private final List<PagedAreaDefinition<?>> pagedAreas = new ArrayList<>();

  public static MenuDefinitionBuilder create() {
    return new MenuDefinitionBuilder();
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
    this.refreshInterval = Optional.of(refreshInterval);
    return this;
  }

  public MenuDefinitionBuilder noRefreshIntervalOverride() {
    this.refreshInterval = Optional.empty();
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
    this.interactionHooks = Optional.of(Objects.requireNonNull(hooks, "hooks must not be null"));
    return this;
  }

  public MenuDefinitionBuilder noInteractionHooks() {
    this.interactionHooks = Optional.empty();
    return this;
  }

  public MenuDefinitionBuilder depositHandler(DepositHandler handler) {
    this.depositHandler = Optional.of(Objects.requireNonNull(handler, "handler must not be null"));
    return this;
  }

  public MenuDefinitionBuilder noDepositHandler() {
    this.depositHandler = Optional.empty();
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

  public MenuDefinition build() {
    Objects.requireNonNull(key, "key must not be null");
    Objects.requireNonNull(title, "title must not be null");
    Objects.requireNonNull(interactionPolicy, "interactionPolicy must not be null");
    Objects.requireNonNull(populator, "populator must not be null");

    final MenuKey builtKey = key;
    final String builtTitle = title;
    final int builtRows = rows;
    final Optional<Duration> builtRefreshInterval = refreshInterval;
    final InteractionPolicy builtInteractionPolicy = interactionPolicy;
    final MenuPopulator builtPopulator = populator;
    final Optional<MenuInteractionHooks> builtHooks = interactionHooks;
    final Optional<DepositHandler> builtDepositHandler = depositHandler;
    final Optional<List<PagedAreaDefinition<?>>> builtPagedAreas =
        pagedAreas.isEmpty() ? Optional.empty() : Optional.of(List.copyOf(pagedAreas));

    return new MenuDefinition() {
      @Override public MenuKey key() { return builtKey; }
      @Override public String title() { return builtTitle; }
      @Override public int rows() { return builtRows; }
      @Override public Optional<Duration> refreshInterval() { return builtRefreshInterval; }
      @Override public InteractionPolicy interactionPolicy() { return builtInteractionPolicy; }
      @Override public MenuPopulator populator() { return builtPopulator; }
      @Override public Optional<MenuInteractionHooks> interactionHooks() { return builtHooks; }
      @Override public Optional<DepositHandler> depositHandler() { return builtDepositHandler; }
      @Override public Optional<List<PagedAreaDefinition<?>>> pagedAreas() { return builtPagedAreas; }

      @Override
      public String toString() {
        return "MenuDefinition[key=" + builtKey.asString() + ",rows=" + builtRows + "]";
      }
    };
  }
}