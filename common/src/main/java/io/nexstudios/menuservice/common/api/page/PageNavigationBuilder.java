package io.nexstudios.menuservice.common.api.page;

import io.nexstudios.menuservice.common.api.item.MenuItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Builder for {@link PageNavigation}.
 */
public final class PageNavigationBuilder {

  private OptionalInt previousSlot = OptionalInt.empty();
  private OptionalInt nextSlot = OptionalInt.empty();
  private OptionalInt refreshSlot = OptionalInt.empty();
  private Optional<MenuItem> previousItem = Optional.empty();
  private Optional<MenuItem> nextItem = Optional.empty();
  private Optional<MenuItem> refreshItem = Optional.empty();
  private boolean showCurrentPageAmount;
  private boolean hidePreviousOnFirstPage = true;
  private boolean hideNextOnLastPage = true;

  public PageNavigationBuilder previousSlot(int slot) {
    validateSlot(slot, "previousSlot");
    this.previousSlot = OptionalInt.of(slot);
    return this;
  }

  public PageNavigationBuilder nextSlot(int slot) {
    validateSlot(slot, "nextSlot");
    this.nextSlot = OptionalInt.of(slot);
    return this;
  }

  public PageNavigationBuilder refreshSlot(int slot) {
    validateSlot(slot, "refreshSlot");
    this.refreshSlot = OptionalInt.of(slot);
    return this;
  }

  public PageNavigationBuilder disablePrevious() {
    this.previousSlot = OptionalInt.empty();
    this.previousItem = Optional.empty();
    return this;
  }

  public PageNavigationBuilder disableNext() {
    this.nextSlot = OptionalInt.empty();
    this.nextItem = Optional.empty();
    return this;
  }

  public PageNavigationBuilder disableRefresh() {
    this.refreshSlot = OptionalInt.empty();
    this.refreshItem = Optional.empty();
    return this;
  }

  public PageNavigationBuilder previousItem(MenuItem item) {
    this.previousItem = Optional.of(MenuNavigationItems.requireItem(item, "previousItem"));
    return this;
  }

  public PageNavigationBuilder previousItem(ItemStack stack) {
    return previousItem(MenuItem.of(stack));
  }

  public PageNavigationBuilder previousItem(Material material) {
    return previousItem(MenuItem.of(material));
  }

  public PageNavigationBuilder nextItem(MenuItem item) {
    this.nextItem = Optional.of(MenuNavigationItems.requireItem(item, "nextItem"));
    return this;
  }

  public PageNavigationBuilder nextItem(ItemStack stack) {
    return nextItem(MenuItem.of(stack));
  }

  public PageNavigationBuilder nextItem(Material material) {
    return nextItem(MenuItem.of(material));
  }

  public PageNavigationBuilder refreshItem(MenuItem item) {
    this.refreshItem = Optional.of(MenuNavigationItems.requireItem(item, "refreshItem"));
    return this;
  }

  public PageNavigationBuilder refreshItem(ItemStack stack) {
    return refreshItem(MenuItem.of(stack));
  }

  public PageNavigationBuilder refreshItem(Material material) {
    return refreshItem(MenuItem.of(material));
  }

  public PageNavigationBuilder showCurrentPageAmount(boolean showCurrentPageAmount) {
    this.showCurrentPageAmount = showCurrentPageAmount;
    return this;
  }

  public PageNavigationBuilder hidePreviousOnFirstPage(boolean hidePreviousOnFirstPage) {
    this.hidePreviousOnFirstPage = hidePreviousOnFirstPage;
    return this;
  }

  public PageNavigationBuilder hideNextOnLastPage(boolean hideNextOnLastPage) {
    this.hideNextOnLastPage = hideNextOnLastPage;
    return this;
  }

  public PageNavigation build() {
    return new PageNavigation(
        previousSlot,
        nextSlot,
        refreshSlot,
        previousItem,
        nextItem,
        refreshItem,
        showCurrentPageAmount,
        hidePreviousOnFirstPage,
        hideNextOnLastPage
    );
  }

  private static void validateSlot(int slot, String name) {
    if (slot < 0) {
      throw new IllegalArgumentException(name + " must be >= 0");
    }
  }

  private static final class MenuNavigationItems {
    private static MenuItem requireItem(MenuItem item, String name) {
      return Objects.requireNonNull(item, name + " must not be null");
    }
  }
}

