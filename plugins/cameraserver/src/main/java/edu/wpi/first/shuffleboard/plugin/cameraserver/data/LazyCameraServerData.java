package edu.wpi.first.shuffleboard.plugin.cameraserver.data;

import edu.wpi.first.shuffleboard.api.util.LazyInit;

import org.opencv.core.Mat;

import java.util.function.Supplier;

/**
 * A subtype of {@code CameraServerData} for playback that will only load the frame from disk when the image is accessed
 * by an external class with {@link #getImage()}. This results in much faster loading of recording files, since the app
 * will not have to wait for expensive disk I/O operations for potentially tens of thousands of frames, as well as
 * drastically reducing memory use by not loading entire video files frame-by-frame into memory.
 *
 * <p>When an instance of this class goes out-of-scope, the image should be released with {@link #clear()} to free up
 * memory. Later image access will re-read the frame from the video file.
 */
public final class LazyCameraServerData extends CameraServerData {

  private final int fileNum;
  private final int frameNum;
  private final LazyInit<Mat> image;

  /**
   * Creates a new data object. This constructor is identical to {@link CameraServerData#CameraServerData}, but takes
   * a {@code Supplier<Mat>} instead of a {@code Mat} for lazily loading the image.
   *
   * @param name          no change
   * @param fileNum       the number of the video file corresponding to this frame
   * @param frameNum      the frame index in the video file
   * @param imageSupplier a callback for reading the frame from disk
   * @param fps           no change
   * @param bandwidth     no change
   */
  public LazyCameraServerData(String name,
                              int fileNum,
                              int frameNum,
                              Supplier<Mat> imageSupplier,
                              double fps,
                              double bandwidth) {
    super(name, null, fps, bandwidth);
    this.fileNum = fileNum;
    this.frameNum = frameNum;
    image = LazyInit.of(imageSupplier::get);
  }

  @Override
  public Mat getImage() {
    return image.get();
  }

  /**
   * Releases the image and clears the lazy-loading holder. The next call to {@link #getImage()} will read the frame
   * from disk.
   */
  public void clear() {
    if (image.hasValue()) {
      image.get().release();
    }
    image.clear();
  }

  @Override
  public String toHumanReadableString() {
    return String.format(
        "fileIndex=%d, frameIndex=%d, fps=%s, bandwidth=%s",
        fileNum,
        frameNum,
        getFps(),
        getBandwidth()
    );
  }
}
