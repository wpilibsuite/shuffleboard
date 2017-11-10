package edu.wpi.first.shuffleboard.api.widget;

/**
 * A typed sub-class of ComponentType for Layouts. This interface serves as a runtime
 * "marker type" for types that produce layouts.
 *
 * <p>Inheriting from this interface is required for a Component to show up in the
 * "Add to new layout..." menu, among other things.
 */
public interface LayoutType<T extends Layout> extends ComponentType<T> {

}
