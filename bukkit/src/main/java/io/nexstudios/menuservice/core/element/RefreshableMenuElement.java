package io.nexstudios.menuservice.core.element;

import io.nexstudios.menuservice.api.MenuContext;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Menu element that recomputes its item stack whenever it is rendered.
 */
public class RefreshableMenuElement extends AbstractMenuElement {

  private final Supplier<ItemStack> itemSupplier;

  public RefreshableMenuElement(Supplier<ItemStack> itemSupplier) {
    this(itemSupplier, null);
  }

  public RefreshableMenuElement(Supplier<ItemStack> itemSupplier, BiConsumer<MenuContext, InventoryClickEvent> clickHandler) {
    super(clickHandler);
    this.itemSupplier = Objects.requireNonNull(itemSupplier, "itemSupplier");
  }

  @Override
  public ItemStack render(MenuContext context) {
    ItemStack itemStack = itemSupplier.get();
    return itemStack == null ? null : itemStack.clone();
  }
}

