package io.nexstudios.menuservice.core.page.control;

import io.nexstudios.menuservice.api.MenuKey;
import io.nexstudios.menuservice.api.page.control.PageSortControl;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Declarative sort control backed by a small mode registry.
 *
 * @param <T> the item type
 */
public final class BasicPageSortControl<T> implements PageSortControl<T> {

  private final String controlId;
  private final List<String> modeIds;
  private final String defaultModeId;
  private final Map<String, String> labels;
  private final Map<String, Comparator<T>> comparators;

  private BasicPageSortControl(Builder<T> builder) {
    this.controlId = builder.controlId;
    this.modeIds = List.copyOf(builder.modeIds);
    this.defaultModeId = builder.defaultModeId != null ? builder.defaultModeId : builder.modeIds.getFirst();
    this.labels = Map.copyOf(builder.labels);
    this.comparators = Map.copyOf(builder.comparators);
    validate();
  }

  public static <T> Builder<T> builder(String controlId) {
    return new Builder<>(controlId);
  }

  @Override
  public String controlId() {
    return controlId;
  }

  @Override
  public List<String> modeIds() {
    return modeIds;
  }

  @Override
  public String defaultModeId() {
    return defaultModeId;
  }

  @Override
  public String labelForMode(String modeId) {
    return labels.getOrDefault(modeId, modeId);
  }

  @Override
  public Comparator<T> comparatorFor(String modeId, MenuKey menuKey, UUID viewerId) {
    Objects.requireNonNull(menuKey, "menuKey");
    Objects.requireNonNull(viewerId, "viewerId");
    return comparators.getOrDefault(modeId, comparators.get(defaultModeId));
  }

  public static final class Builder<T> {

    private final String controlId;
    private final List<String> modeIds = new ArrayList<>();
    private final Map<String, String> labels = new LinkedHashMap<>();
    private final Map<String, Comparator<T>> comparators = new LinkedHashMap<>();
    private String defaultModeId;

    private Builder(String controlId) {
      this.controlId = Objects.requireNonNull(controlId, "controlId");
    }

    public Builder<T> mode(String modeId, String label, Comparator<T> comparator) {
      Objects.requireNonNull(modeId, "modeId");
      Objects.requireNonNull(label, "label");
      Objects.requireNonNull(comparator, "comparator");
      if (modeIds.contains(modeId)) {
        throw new IllegalArgumentException("Duplicate modeId: " + modeId);
      }
      modeIds.add(modeId);
      labels.put(modeId, label);
      comparators.put(modeId, comparator);
      return this;
    }

    public Builder<T> defaultMode(String modeId) {
      this.defaultModeId = Objects.requireNonNull(modeId, "modeId");
      return this;
    }

    public BasicPageSortControl<T> build() {
      if (modeIds.isEmpty()) {
        throw new IllegalStateException("At least one mode must be defined.");
      }
      if (defaultModeId != null && !modeIds.contains(defaultModeId)) {
        throw new IllegalStateException("defaultModeId must be one of the registered modeIds.");
      }
      return new BasicPageSortControl<>(this);
    }
  }
}

