package io.nexstudios.menuservice.core.page;

import io.nexstudios.menuservice.api.MenuContext;
import io.nexstudios.menuservice.api.MenuKey;
import io.nexstudios.menuservice.api.page.PageBounds;
import io.nexstudios.menuservice.api.page.PageItemRenderer;
import io.nexstudios.menuservice.api.page.control.PageFilterControl;
import io.nexstudios.menuservice.api.page.control.PageSortControl;
import io.nexstudios.menuservice.api.page.control.PageControlStateStore;
import io.nexstudios.menuservice.core.page.control.AbstractControlButton;
import io.nexstudios.menuservice.core.page.control.InMemoryPageControlStateStore;
import io.nexstudios.menuservice.core.page.control.PageControlPipeline;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

/**
 * Paged menu view with built-in reusable filter and sort controls.
 *
 * @param <T> the item type
 */
public abstract class ControlledPagedMenuView<T> extends PagedMenuView<T> {

  private final List<T> baseItems;
  private final String areaId;
  private final PageControlStateStore controlStateStore = new InMemoryPageControlStateStore();
  private final List<PageFilterControl<T>> filterControls = new ArrayList<>();
  private final List<PageSortControl<T>> sortControls = new ArrayList<>();

  protected ControlledPagedMenuView(
      MenuKey key,
      int size,
      PageBounds bounds,
      String areaId,
      List<T> items,
      PageItemRenderer<T> itemRenderer
  ) {
    super(key, size, bounds, new CollectionPageSource<>(items), itemRenderer);
    this.baseItems = List.copyOf(Objects.requireNonNull(items, "items"));
    this.areaId = Objects.requireNonNull(areaId, "areaId");
  }

  protected final PageControlStateStore controlStateStore() {
    return controlStateStore;
  }

  protected final String controlAreaId() {
    return areaId;
  }

  protected final <TControl extends PageFilterControl<T>> TControl addFilterControl(
      int slot,
      Component titlePrefix,
      Material material,
      TControl control
  ) {
    filterControls.add(Objects.requireNonNull(control, "control"));
    addElement(slot, new AbstractControlButton(
        material,
        titlePrefix,
        key(),
        areaId,
        control,
        controlStateStore,
        this::refreshAndResetPage
    ));
    return control;
  }

  protected final <TControl extends PageFilterControl<T>> TControl addFilterControl(
      int slot,
      String titlePrefix,
      Material material,
      TControl control
  ) {
    return addFilterControl(slot,
        net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(titlePrefix),
        material, control);
  }

  protected final <TControl extends PageSortControl<T>> TControl addSortControl(
      int slot,
      Component titlePrefix,
      Material material,
      TControl control
  ) {
    sortControls.add(Objects.requireNonNull(control, "control"));
    addElement(slot, new AbstractControlButton(
        material,
        titlePrefix,
        key(),
        areaId,
        control,
        controlStateStore,
        this::refreshAndResetPage
    ));
    return control;
  }

  protected final <TControl extends PageSortControl<T>> TControl addSortControl(
      int slot,
      String titlePrefix,
      Material material,
      TControl control
  ) {
    return addSortControl(slot,
        net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(titlePrefix),
        material, control);
  }

  protected final String activeModeId(PageFilterControl<T> control, MenuContext context) {
    return controlStateStore.getActiveModeId(context.viewer().getUniqueId(), key(), areaId, control.controlId())
        .orElse(control.defaultModeId());
  }

  protected final String activeModeId(PageSortControl<T> control, MenuContext context) {
    return controlStateStore.getActiveModeId(context.viewer().getUniqueId(), key(), areaId, control.controlId())
        .orElse(control.defaultModeId());
  }

  protected final void refreshAndResetPage(MenuContext context) {
    setPage(0);
    context.menuService().refresh(context.viewer());
  }

  @Override
  protected List<T> resolveItems(MenuContext context) {
    return PageControlPipeline.apply(
        baseItems,
        context.viewer().getUniqueId(),
        key(),
        areaId,
        controlStateStore,
        filterControls,
        sortControls
    );
  }

  protected final Component activeTitle(String baseTitle, String... parts) {
    StringBuilder builder = new StringBuilder(baseTitle);
    for (String part : parts) {
      if (part != null && !part.isBlank()) {
        builder.append(" | ").append(part);
      }
    }
    return Component.text(builder.toString());
  }
}



