package io.nexstudios.menuservice.common.api.page;

import io.nexstudios.menuservice.common.api.MenuKey;
import io.nexstudios.menuservice.common.api.ViewerRef;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Defines a paged content area inside a menu.
 */
public final class PagedAreaDefinition<T> {

  private final String id;
  private final PageBounds bounds;
  private final PageSource<T> source;
  private final PageItemRenderer<T> renderer;
  private final PageNavigation navigation;
  private final Optional<PageClickHandler<T>> clickHandler;

  public PagedAreaDefinition(
      String id,
      PageBounds bounds,
      PageSource<T> source,
      PageItemRenderer<T> renderer,
      PageNavigation navigation
  ) {
    this(id, bounds, source, renderer, navigation, Optional.empty());
  }

  public PagedAreaDefinition(
      String id,
      PageBounds bounds,
      PageSource<T> source,
      PageItemRenderer<T> renderer,
      PageNavigation navigation,
      Optional<PageClickHandler<T>> clickHandler
  ) {
    Objects.requireNonNull(id, "id must not be null");
    if (id.isBlank()) throw new IllegalArgumentException("id must not be blank");
    this.id = id;
    this.bounds = Objects.requireNonNull(bounds, "bounds must not be null");
    this.source = Objects.requireNonNull(source, "source must not be null");
    this.renderer = Objects.requireNonNull(renderer, "renderer must not be null");
    this.navigation = Objects.requireNonNull(navigation, "navigation must not be null");
    this.clickHandler = Objects.requireNonNull(clickHandler, "clickHandler must not be null");
  }

  public String id() {
    return id;
  }

  public PageBounds bounds() {
    return bounds;
  }

  public PageNavigation navigation() {
    return navigation;
  }

  public Optional<PageClickHandler<T>> clickHandler() {
    return clickHandler;
  }

  public List<T> load(MenuKey menuKey, ViewerRef viewer) {
    return source.load(menuKey, viewer);
  }

  public PageItemRenderer<T> renderer() {
    return renderer;
  }

  public PageModel model() {
    return new PageModel(bounds);
  }
}