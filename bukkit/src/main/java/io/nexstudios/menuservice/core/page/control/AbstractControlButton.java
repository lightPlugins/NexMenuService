package io.nexstudios.menuservice.core.page.control;

import io.nexstudios.menuservice.api.MenuContext;
import io.nexstudios.menuservice.api.MenuKey;
import io.nexstudios.menuservice.api.MenuElement;
import io.nexstudios.menuservice.api.page.control.PageControl;
import io.nexstudios.menuservice.api.page.control.PageControlStateStore;
import io.nexstudios.menuservice.core.element.AbstractMenuElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Reusable click-to-cycle control button for filters and sorts.
 */
public class AbstractControlButton extends AbstractMenuElement implements MenuElement {

  private final Material material;
  private final String titlePrefix;
  private final MenuKey menuKey;
  private final String areaId;
  private final PageControl control;
  private final PageControlStateStore stateStore;
  private final Consumer<MenuContext> afterModeChange;

  public AbstractControlButton(
      Material material,
      String titlePrefix,
      MenuKey menuKey,
      String areaId,
      PageControl control,
      PageControlStateStore stateStore,
      Consumer<MenuContext> afterModeChange
  ) {
    this.material = Objects.requireNonNull(material, "material");
    this.titlePrefix = Objects.requireNonNull(titlePrefix, "titlePrefix");
    this.menuKey = Objects.requireNonNull(menuKey, "menuKey");
    this.areaId = Objects.requireNonNull(areaId, "areaId");
    this.control = Objects.requireNonNull(control, "control");
    this.stateStore = Objects.requireNonNull(stateStore, "stateStore");
    this.afterModeChange = Objects.requireNonNull(afterModeChange, "afterModeChange");
  }

  @Override
  public ItemStack render(MenuContext context) {
    UUID viewerId = context.viewer().getUniqueId();
    String activeModeId = stateStore.getActiveModeId(viewerId, menuKey, areaId, control.controlId())
        .orElse(control.defaultModeId());
    if (!control.modeIds().contains(activeModeId)) {
      activeModeId = control.defaultModeId();
    }

    ItemStack itemStack = new ItemStack(material);
    ItemMeta meta = itemStack.getItemMeta();
    if (meta != null) {
      meta.displayName(Component.text(titlePrefix + ": " + control.labelForMode(activeModeId))
          .color(NamedTextColor.GOLD)
          .decoration(TextDecoration.ITALIC, false)
          .decorate(TextDecoration.BOLD));
      meta.lore(buildLore(activeModeId));
      itemStack.setItemMeta(meta);
    }
    return itemStack;
  }

  @Override
  public void onClick(MenuContext context, InventoryClickEvent event) {
    if (!event.getClick().isLeftClick() && !event.getClick().isRightClick()) {
      return;
    }

    UUID viewerId = context.viewer().getUniqueId();
    if (event.getClick().isLeftClick()) {
      stateStore.cycleToNextMode(viewerId, menuKey, areaId, control);
    } else {
      stateStore.cycleToPreviousMode(viewerId, menuKey, areaId, control);
    }

    event.setCancelled(true);
    afterModeChange.accept(context);
  }

  private List<Component> buildLore(String activeModeId) {
    List<Component> lore = new ArrayList<>();
    lore.add(Component.empty());

    for (String modeId : control.modeIds()) {
      boolean active = modeId.equals(activeModeId);
      String marker = active ? "➤ " : "• ";
      NamedTextColor color = active ? NamedTextColor.GREEN : NamedTextColor.GRAY;
      lore.add(Component.text(marker + control.labelForMode(modeId))
          .color(color)
          .decoration(TextDecoration.ITALIC, false));
    }

    lore.add(Component.empty());
    lore.add(Component.text("Left click = next mode")
        .color(NamedTextColor.DARK_GRAY)
        .decoration(TextDecoration.ITALIC, false));
    lore.add(Component.text("Right click = previous mode")
        .color(NamedTextColor.DARK_GRAY)
        .decoration(TextDecoration.ITALIC, false));
    return lore;
  }
}

