package io.nexstudios.menuservice.core.view;

import io.nexstudios.menuservice.api.MenuContext;
import io.nexstudios.menuservice.api.MenuElement;
import io.nexstudios.menuservice.api.MenuKey;
import io.nexstudios.menuservice.api.MenuView;
import io.nexstudios.menuservice.core.element.StaticMenuElement;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

/**
 * Base implementation for menu views with slot-based element placement.
 */
public abstract class AbstractMenuView implements MenuView {

  private final MenuKey key;
  private final int size;
  private final Map<Integer, MenuElement> elements = new LinkedHashMap<>();
  private Function<MenuContext, Component> titleProvider = context -> Component.empty();
  private Consumer<MenuContext> onViewListener = context -> {};
  private Consumer<MenuContext> closeListener = context -> {};

  protected AbstractMenuView(MenuKey key, int size) {
    this.key = Objects.requireNonNull(key, "key");
    if (size <= 0 || size % 9 != 0) {
      throw new IllegalArgumentException("Menu size must be a positive multiple of 9.");
    }
    this.size = size;
  }

  /**
   * Converts inventory rows into a menu size.
   *
   * @param rows the amount of rows
   * @return the corresponding inventory size
   */
  protected static int rowsToSize(int rows) {
    if (rows <= 0) {
      throw new IllegalArgumentException("Rows must be a positive integer.");
    }
    return rows * 9;
  }

  @Override
  public MenuKey key() {
    return key;
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public Component title(MenuContext context) {
    return titleProvider.apply(context);
  }

  @Override
  public Map<Integer, MenuElement> elements(MenuContext context) {
    return Collections.unmodifiableMap(new LinkedHashMap<>(elements));
  }

  @Override
  public void onOpen(MenuContext context) {
    onViewListener.accept(Objects.requireNonNull(context, "context"));
  }

  @Override
  public void onClose(MenuContext context) {
    closeListener.accept(Objects.requireNonNull(context, "context"));
  }

  /**
   * Sets a static title for this view.
   *
   * @param title the title to use
   */
  protected final void setTitle(Component title) {
    this.titleProvider = context -> title;
  }

  /**
   * Sets a dynamic title provider for this view.
   *
   * @param titleProvider the title provider
   */
  protected final void setTitle(Function<MenuContext, Component> titleProvider) {
    this.titleProvider = Objects.requireNonNull(titleProvider, "titleProvider");
  }

  /**
   * Sets a callback that is invoked whenever the view is opened.
   *
   * @param onView the open callback
   */
  protected final void setOnView(Consumer<MenuContext> onView) {
    this.onViewListener = Objects.requireNonNull(onView, "onView");
  }

  /**
   * Sets a callback that is invoked whenever the view is closed.
   *
   * @param closeListener the close callback
   */
  protected final void setCloseListener(Consumer<MenuContext> closeListener) {
    this.closeListener = Objects.requireNonNull(closeListener, "closeListener");
  }

  /**
   * Places an element at the given slot.
   *
   * @param slot the inventory slot
   * @param element the element to place
   */
  protected final void setElement(int slot, MenuElement element) {
    validateSlot(slot);
    elements.put(slot, Objects.requireNonNull(element, "element"));
  }

  /**
   * Places an element at the given slot.
   *
   * @param slot the inventory slot
   * @param element the element to place
   */
  protected final void addElement(int slot, MenuElement element) {
    setElement(slot, element);
  }

  /**
   * Places an element at the given column and row.
   *
   * @param column the zero-based column
   * @param row the zero-based row
   * @param element the element to place
   */
  protected final void setElement(int column, int row, MenuElement element) {
    setElement(toSlot(column, row), element);
  }

  /**
   * Places an element at the given column and row.
   *
   * @param column the zero-based column
   * @param row the zero-based row
   * @param element the element to place
   */
  protected final void addElement(int column, int row, MenuElement element) {
    setElement(column, row, element);
  }

  /**
   * Fills the entire inventory with the given element.
   *
   * @param element the element to fill with
   */
  protected final void fill(MenuElement element) {
    Objects.requireNonNull(element, "element");
    for (int slot = 0; slot < size; slot++) {
      elements.put(slot, element);
    }
  }

  /**
   * Fills the entire inventory with a static item.
   *
   * @param itemStack the item stack to use
   */
  protected final void fill(ItemStack itemStack) {
    fill(new StaticMenuElement(itemStack));
  }

  /**
   * Removes the element at the given slot.
   *
   * @param slot the inventory slot
   */
  protected final void removeElement(int slot) {
    validateSlot(slot);
    elements.remove(slot);
  }

  /**
   * Converts a column and row to an absolute inventory slot.
   *
   * @param column the zero-based column
   * @param row the zero-based row
   * @return the absolute slot index
   */
  protected final int toSlot(int column, int row) {
    if (column < 0 || column > 8) {
      throw new IllegalArgumentException("Column must be between 0 and 8.");
    }
    int maxRow = size / 9 - 1;
    if (row < 0 || row > maxRow) {
      throw new IllegalArgumentException("Row must be between 0 and %d.".formatted(maxRow));
    }
    return row * 9 + column;
  }

  private void validateSlot(int slot) {
    if (slot < 0 || slot >= size) {
      throw new IllegalArgumentException("Slot must be between 0 and %d.".formatted(size - 1));
    }
  }
}



