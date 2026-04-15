package io.nexstudios.menuservice.core.element;

import io.nexstudios.menuservice.api.MenuContext;
import java.util.Objects;
import java.util.function.BiConsumer;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Menu element that always renders the same item stack.
 */
public class StaticMenuElement extends AbstractMenuElement {

  private final ItemStack itemStack;

  public StaticMenuElement(ItemStack itemStack) {
    this(itemStack, null);
  }

  public StaticMenuElement(ItemStack itemStack, BiConsumer<MenuContext, InventoryClickEvent> clickHandler) {
    super(clickHandler);
    this.itemStack = Objects.requireNonNull(itemStack, "itemStack");
  }

  @Override
  public ItemStack render(MenuContext context) {
    return itemStack.clone();
  }
}

