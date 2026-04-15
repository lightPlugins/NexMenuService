package io.nexstudios.menuservice.core.page.control;

import io.nexstudios.menuservice.api.MenuKey;
import io.nexstudios.menuservice.api.page.control.PageFilterControl;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Declarative filter control backed by a small mode registry.
 *
 * @param <T> the item type
 */
public final class BasicPageFilterControl<T> implements PageFilterControl<T> {

  private final String controlId;
  private final List<String> modeIds;
  private final String defaultModeId;
  private final Map<String, String> labels;
  private final Map<String, Predicate<T>> predicates;

  private BasicPageFilterControl(Builder<T> builder) {
    this.controlId = builder.controlId;
    this.modeIds = List.copyOf(builder.modeIds);
    this.defaultModeId = builder.defaultModeId != null ? builder.defaultModeId : builder.modeIds.getFirst();
    this.labels = Map.copyOf(builder.labels);
    this.predicates = Map.copyOf(builder.predicates);
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
  public Predicate<T> predicateFor(String modeId, MenuKey menuKey, UUID viewerId) {
    Objects.requireNonNull(menuKey, "menuKey");
    Objects.requireNonNull(viewerId, "viewerId");
    return predicates.getOrDefault(modeId, predicates.get(defaultModeId));
  }

  public static final class Builder<T> {

    private final String controlId;
    private final List<String> modeIds = new ArrayList<>();
    private final Map<String, String> labels = new LinkedHashMap<>();
    private final Map<String, Predicate<T>> predicates = new LinkedHashMap<>();
    private String defaultModeId;

    private Builder(String controlId) {
      this.controlId = Objects.requireNonNull(controlId, "controlId");
    }

    public Builder<T> mode(String modeId, String label, Predicate<T> predicate) {
      Objects.requireNonNull(modeId, "modeId");
      Objects.requireNonNull(label, "label");
      Objects.requireNonNull(predicate, "predicate");
      if (modeIds.contains(modeId)) {
        throw new IllegalArgumentException("Duplicate modeId: " + modeId);
      }
      modeIds.add(modeId);
      labels.put(modeId, label);
      predicates.put(modeId, predicate);
      return this;
    }

    public Builder<T> defaultMode(String modeId) {
      this.defaultModeId = Objects.requireNonNull(modeId, "modeId");
      return this;
    }

    public BasicPageFilterControl<T> build() {
      if (modeIds.isEmpty()) {
        throw new IllegalStateException("At least one mode must be defined.");
      }
      if (defaultModeId != null && !modeIds.contains(defaultModeId)) {
        throw new IllegalStateException("defaultModeId must be one of the registered modeIds.");
      }
      return new BasicPageFilterControl<>(this);
    }
  }
}

