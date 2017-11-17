package edu.wpi.first.shuffleboard.api.sources;

import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.util.PropertyUtils;

import java.util.function.Function;

/**
 * A type of source that provides a view of a subset of the data of another source, as well as propagating data changes
 * to the original source. This is especially helpful for widgets that delegate some behaviour to another, embedded
 * widget (typically used when a data type is a superset of another; e.g. in FRC, PIDCommand data is a superset of both
 * PIDController data and Command data).
 */
public final class SubSource<T> extends AbstractDataSource<T> {

  private final SourceType sourceType;

  /**
   * Creates a new sub source.
   *
   * @param type          the type of data provided by this source
   * @param source        the source that contains the backing data for this sub source
   * @param toBackingData a conversion function to use to convert the data of this source to data usable by the backing
   *                      source
   * @param extractData   a function to use to extract the relevant data from the backing source
   * @param <U>           the type of the backing data
   */
  public <U> SubSource(DataType<T> type,
                       DataSource<U> source,
                       Function<? super T, ? extends U> toBackingData,
                       Function<? super U, ? extends T> extractData) {
    super(type);
    this.sourceType = source.getType();

    PropertyUtils.bindBidirectionalWithConverter(
        this.dataProperty(),
        source.dataProperty(),
        toBackingData,
        extractData
    );

    this.activeProperty().bindBidirectional(source.activeProperty());

    if (source instanceof AbstractDataSource) {
      this.connectedProperty().bindBidirectional(((AbstractDataSource) source).connectedProperty());
    }
  }

  @Override
  public SourceType getType() {
    return sourceType;
  }

}
