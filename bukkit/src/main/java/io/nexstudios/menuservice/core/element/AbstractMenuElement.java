package io.nexstudios.menuservice.core.element;

import io.nexstudios.menuservice.api.MenuContext;
import io.nexstudios.menuservice.api.MenuElement;
import java.util.Objects;
import java.util.function.BiConsumer;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Base class for reusable menu elements.
 */
public abstract class AbstractMenuElement implements MenuElement {

  private final BiConsumer<MenuContext, InventoryClickEvent> clickHandler;

  protected AbstractMenuElement() {
    this(null);
  }

  protected AbstractMenuElement(BiConsumer<MenuContext, InventoryClickEvent> clickHandler) {
    this.clickHandler = clickHandler;
  }

  @Override
  public void onClick(MenuContext context, InventoryClickEvent event) {
    if (clickHandler != null) {
      clickHandler.accept(Objects.requireNonNull(context, "context"), Objects.requireNonNull(event, "event"));
    }
  }

  @Override
  public abstract ItemStack render(MenuContext context);
}

