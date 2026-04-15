package io.nexstudios.menuservice.api.page.control;

import io.nexstudios.menuservice.api.MenuKey;
import java.util.Comparator;
import java.util.UUID;

/**
 * A control that sorts items based on the active mode.
 *
 * @param <T> the item type
 */
public interface PageSortControl<T> extends PageControl {

  Comparator<T> comparatorFor(String modeId, MenuKey menuKey, UUID viewerId);
}

