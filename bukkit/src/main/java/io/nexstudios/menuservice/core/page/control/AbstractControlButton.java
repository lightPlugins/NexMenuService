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
import java.util.function.Function;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Reusable click-to-cycle control button for filters and sorts.
 *
 * <p>The display name supports a {@code <sort-mode>} placeholder that is replaced
 * with the label of the currently active mode at render time.
 *
 * <p>Example (MiniMessage string):
 * <pre>{@code "<yellow><bold>Sorting: <sort-mode>"}</pre>
 */
public class AbstractControlButton extends AbstractMenuElement implements MenuElement {

  private final Material material;
  /** Resolves the display name for a given active mode label. */
  private final Function<String, Component> titleResolver;
  private final MenuKey menuKey;
  private final String areaId;
  private final PageControl control;
  private final PageControlStateStore stateStore;
  private final Consumer<MenuContext> afterModeChange;

  /**
   * Primary constructor – accepts a pre-built resolver function.
   */
  public AbstractControlButton(
      Material material,
      Function<String, Component> titleResolver,
      MenuKey menuKey,
      String areaId,
      PageControl control,
      PageControlStateStore stateStore,
      Consumer<MenuContext> afterModeChange
  ) {
    this.material = Objects.requireNonNull(material, "material");
    this.titleResolver = Objects.requireNonNull(titleResolver, "titleResolver");
    this.menuKey = Objects.requireNonNull(menuKey, "menuKey");
    this.areaId = Objects.requireNonNull(areaId, "areaId");
    this.control = Objects.requireNonNull(control, "control");
    this.stateStore = Objects.requireNonNull(stateStore, "stateStore");
    this.afterModeChange = Objects.requireNonNull(afterModeChange, "afterModeChange");
  }

  /**
   * String overload – {@code titleTemplate} is a MiniMessage string that may contain
   * the {@code <sort-mode>} placeholder, which is replaced with the active mode label.
   */
  public AbstractControlButton(
      Material material,
      String titleTemplate,
      MenuKey menuKey,
      String areaId,
      PageControl control,
      PageControlStateStore stateStore,
      Consumer<MenuContext> afterModeChange
  ) {
    this(material,
        modeLabel -> MiniMessage.miniMessage().deserialize(
            Objects.requireNonNull(titleTemplate, "titleTemplate"),
            Placeholder.parsed("sort-mode", modeLabel)
        ),
        menuKey, areaId, control, stateStore, afterModeChange);
  }

  /**
   * Component overload – the {@code <sort-mode>} placeholder cannot be used here.
   * The component is shown as-is regardless of the active mode.
   * Use the String overload if you need the active mode injected into the title.
   */
  public AbstractControlButton(
      Material material,
      Component title,
      MenuKey menuKey,
      String areaId,
      PageControl control,
      PageControlStateStore stateStore,
      Consumer<MenuContext> afterModeChange
  ) {
    this(material,
        modeLabel -> Objects.requireNonNull(title, "title"),
        menuKey, areaId, control, stateStore, afterModeChange);
  }

  @Override
  public ItemStack render(MenuContext context) {
    UUID viewerId = context.viewer().getUniqueId();
    String activeModeId = stateStore.getActiveModeId(viewerId, menuKey, areaId, control.controlId())
        .orElse(control.defaultModeId());
    if (!control.modeIds().contains(activeModeId)) {
      activeModeId = control.defaultModeId();
    }

    String activeModeLabel = control.labelForMode(activeModeId);

    ItemStack itemStack = new ItemStack(material);
    ItemMeta meta = itemStack.getItemMeta();
    if (meta != null) {
      meta.displayName(titleResolver.apply(activeModeLabel)
          .decoration(TextDecoration.ITALIC, false));
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
