package io.nexstudios.menuservice.common.api.page;

import io.nexstudios.menuservice.common.api.MenuKey;
import io.nexstudios.menuservice.common.api.ViewerRef;

import java.util.List;
import java.util.Objects;

/**
 * Defines a paged content area inside a menu.
 */
public final class PagedAreaDefinition<T> {

  private final String id;
  private final PageBounds bounds;
  private final PageSource<T> source;
  private final PageItemRenderer<T> renderer;
  private final PageNavigation navigation;

  public PagedAreaDefinition(
      String id,
      PageBounds bounds,
      PageSource<T> source,
      PageItemRenderer<T> renderer,
      PageNavigation navigation
  ) {
    Objects.requireNonNull(id, "id must not be null");
    if (id.isBlank()) throw new IllegalArgumentException("id must not be blank");
    this.id = id;
    this.bounds = Objects.requireNonNull(bounds, "bounds must not be null");
    this.source = Objects.requireNonNull(source, "source must not be null");
    this.renderer = Objects.requireNonNull(renderer, "renderer must not be null");
    this.navigation = Objects.requireNonNull(navigation, "navigation must not be null");
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