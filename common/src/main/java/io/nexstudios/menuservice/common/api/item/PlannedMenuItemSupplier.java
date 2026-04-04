package io.nexstudios.menuservice.common.api.item;

import org.bukkit.inventory.ItemStack;

import java.util.concurrent.CompletableFuture;

/** A {@link MenuItemSupplier} that exposes a placeholder item and a future resolved head texture.
 * Use this from page content when you want the placeholder (name, lore, flags, etc.) to render
 * immediately while the actual player head texture is loaded asynchronously.
 */
public interface PlannedMenuItemSupplier extends MenuItemSupplier {

  MenuItem placeholder();

  CompletableFuture<ItemStack> headFuture();

  @Override
  default MenuItem get() {
    return placeholder();
  }

  static PlannedMenuItemSupplier withHead(MenuItem placeholder, CompletableFuture<ItemStack> headStackFuture) {

    return new PlannedMenuItemSupplier() {
      @Override
      public MenuItem placeholder() {
        return placeholder;
      }

      @Override
      public CompletableFuture<ItemStack> headFuture() {
        return headStackFuture;
      }
    };
  }
}


