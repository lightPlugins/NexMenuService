package io.nexstudios.menuservice.core.page;

import io.nexstudios.menuservice.api.MenuContext;
import io.nexstudios.menuservice.api.MenuElement;
import io.nexstudios.menuservice.api.MenuKey;
import io.nexstudios.menuservice.api.page.PageBounds;
import io.nexstudios.menuservice.api.page.PageItemRenderer;
import io.nexstudios.menuservice.api.page.PageSource;
import io.nexstudios.menuservice.core.element.StaticMenuElement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Base menu view with built-in paging support.
 *
 * @param <T> the paged item type
 */
public abstract class PagedMenuView<T> extends io.nexstudios.menuservice.core.view.AbstractMenuView {

  private final PageBounds bounds;
  private final PageSource<T> source;
  private final PageItemRenderer<T> itemRenderer;
  private int page;
  private Integer previousSlot;
  private Integer nextSlot;
  private Integer refreshSlot;
  private Integer indicatorSlot;

  protected PagedMenuView(MenuKey key, int size, PageBounds bounds, PageSource<T> source, PageItemRenderer<T> itemRenderer) {
    super(key, size);
    this.bounds = Objects.requireNonNull(bounds, "bounds");
    this.source = Objects.requireNonNull(source, "source");
    this.itemRenderer = Objects.requireNonNull(itemRenderer, "itemRenderer");
  }

  /**
   * Sets the slot used for the previous page button.
   *
   * @param slot the inventory slot
   */
  protected final void setPreviousButton(int slot) {
    this.previousSlot = slot;
  }

  /**
   * Sets the slot used for the next page button.
   *
   * @param slot the inventory slot
   */
  protected final void setNextButton(int slot) {
    this.nextSlot = slot;
  }

  /**
   * Sets the slot used for the refresh button.
   *
   * @param slot the inventory slot
   */
  protected final void setRefreshButton(int slot) {
    this.refreshSlot = slot;
  }

  /**
   * Sets the slot used for the page indicator.
   *
   * @param slot the inventory slot
   */
  protected final void setPageIndicator(int slot) {
    this.indicatorSlot = slot;
  }

  /**
   * Returns the current page index.
   *
   * @return the page index
   */
  public final int page() {
    return page;
  }

  /**
   * Returns the total amount of pages.
   *
   * @return the page count
   */
  public final int pageCount() {
    return maxPageIndex(source.items()) + 1;
  }

  /**
   * Moves to the next page if possible.
   */
  public final void nextPage() {
    page = Math.min(page + 1, maxPageIndex(source.items()));
  }

  /**
   * Moves to the previous page if possible.
   */
  public final void previousPage() {
    page = Math.max(page - 1, 0);
  }

  /**
   * Sets the current page.
   *
   * @param page the page index
   */
  public final void setPage(int page) {
    this.page = Math.max(0, Math.min(page, maxPageIndex(source.items())));
  }

  @Override
  public Map<Integer, MenuElement> elements(MenuContext context) {
    Map<Integer, MenuElement> elements = new LinkedHashMap<>(super.elements(context));
    List<T> items = resolveItems(context);
    int maxPageIndex = maxPageIndex(items);
    page = Math.max(0, Math.min(page, maxPageIndex));

    for (int localIndex = 0; localIndex < bounds.capacity(); localIndex++) {
      int globalIndex = page * bounds.capacity() + localIndex;
      int slot = bounds.slotAt(localIndex);
      if (globalIndex < items.size()) {
        T item = items.get(globalIndex);
        elements.put(slot, itemRenderer.render(context, item, globalIndex));
      }
    }

    if (indicatorSlot != null) {
      elements.put(indicatorSlot, createPageIndicator(items));
    }
    if (previousSlot != null) {
      elements.put(previousSlot, createNavigationButton(Material.ARROW, Component.text("Previous Page"), this::previousPage));
    }
    if (refreshSlot != null) {
      elements.put(refreshSlot, createNavigationButton(Material.CLOCK, Component.text("Refresh Page"), () -> {}));
    }
    if (nextSlot != null) {
      elements.put(nextSlot, createNavigationButton(Material.ARROW, Component.text("Next Page"), this::nextPage));
    }

    return elements;
  }

  protected List<T> resolveItems(MenuContext context) {
    return source.items();
  }

  private int maxPageIndex(List<T> items) {
    if (items.isEmpty()) {
      return 0;
    }
    return Math.max(0, (int) Math.ceil((double) items.size() / bounds.capacity()) - 1);
  }

  private MenuElement createPageIndicator(List<T> items) {
    return new StaticMenuElement(createIndicatorItem(items), (menuContext, event) -> menuContext.menuService().refresh(menuContext.viewer()));
  }

  private ItemStack createIndicatorItem(List<T> items) {
    ItemStack itemStack = new ItemStack(Material.PAPER);
    var meta = itemStack.getItemMeta();
    if (meta != null) {
      meta.displayName(Component.text("Page %d/%d".formatted(page + 1, maxPageIndex(items) + 1)));
      itemStack.setItemMeta(meta);
    }
    return itemStack;
  }

  private MenuElement createNavigationButton(Material material, Component title, Runnable action) {
    ItemStack itemStack = new ItemStack(material);
    var meta = itemStack.getItemMeta();
    if (meta != null) {
      meta.displayName(title);
      itemStack.setItemMeta(meta);
    }
    return new StaticMenuElement(itemStack, (menuContext, event) -> {
      action.run();
      menuContext.menuService().refresh(menuContext.viewer());
    });
  }
}




