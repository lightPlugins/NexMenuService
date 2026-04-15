package io.nexstudios.menuservice.api.page.control;

import io.nexstudios.menuservice.api.MenuKey;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * A control that filters items based on the active mode.
 *
 * @param <T> the item type
 */
public interface PageFilterControl<T> extends PageControl {

  Predicate<T> predicateFor(String modeId, MenuKey menuKey, UUID viewerId);
}

