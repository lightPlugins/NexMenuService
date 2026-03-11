package io.nexstudios.menuservice.common.api.page.control;

import io.nexstudios.menuservice.common.api.MenuKey;
import io.nexstudios.menuservice.common.api.ViewerRef;

import java.util.function.Predicate;

public interface PageFilterControl<T> extends PageControl {

  Predicate<T> predicateFor(String modeId, MenuKey menuKey, ViewerRef viewer);
}