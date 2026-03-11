package io.nexstudios.menuservice.common.api.page.control;

import io.nexstudios.menuservice.common.api.MenuKey;
import io.nexstudios.menuservice.common.api.ViewerRef;

import java.util.Comparator;

public interface PageSortControl<T> extends PageControl {

  Comparator<T> comparatorFor(String modeId, MenuKey menuKey, ViewerRef viewer);
}