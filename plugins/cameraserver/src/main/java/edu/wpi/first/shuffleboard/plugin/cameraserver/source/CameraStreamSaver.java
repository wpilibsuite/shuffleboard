package edu.wpi.first.shuffleboard.plugin.cameraserver.source;

import edu.wpi.first.shuffleboard.plugin.cameraserver.data.CameraServerData;
import edu.wpi.first.shuffleboard.plugin.cameraserver.data.Resolution;

import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.avutil;
import org.bytedeco.javacpp.indexer.UByteBufferIndexer;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.opencv.core.Mat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Saves frames from a single camera stream to a video file on disk. {@link #finish()} must be called to finish writing
 * the video file; otherwise, it will be unreadable. Additionally, if the stream changes resolution then a new video
 * file will be created (the previous one will be completed automatically). These video files have an index number
 * embedded in their name: the first video file will be 0, the second video file will be 1, the third 2, and so on.
 */
public final class CameraStreamSaver {

  private static final Logger log = Logger.getLogger(CameraStreamSaver.class.getName());

  private final String cameraName;
  private final File rootRecordingFile;
  private FFmpegFrameRecorder recorder;
  private Frame frame;
  private final AtomicBoolean running = new AtomicBoolean(false);
  private final AtomicInteger frameNum = new AtomicInteger(0);
  private final AtomicInteger fileNum = new AtomicInteger(0);
  private Resolution resolution = null;
  private byte[] buffer = null;
  private int[] wideBuffer = null;

  private final Lock lock = new ReentrantLock();

  /**
   * Creates a new stream saver.
   *
   * @param cameraName        the name of the camera stream
   * @param rootRecordingFile the the root recording file being recorded to
   */
  public CameraStreamSaver(String cameraName, File rootRecordingFile) {
    this.cameraName = cameraName;
    this.rootRecordingFile = rootRecordingFile;
    recorder = createRecorder(0);
  }

  /**
   * Saves a single frame to a video file. If the image resolution changes, the current video file will be closed and
   * cleaned up before creating a new file that the given frame will be written to. This avoids issues with changing
   * resolutions or aspect ratios causing issues with codecs or video players. The video file name is formatted as:
   * {@code recording-<timestamp>-<camera name>.<file number>.mp4}, eg {@code recording-15.03.11-Camera.0.mp4},
   * {@code recording-15.03.11-Camera.1.mp4}, {@code recording-15.03.11-Camera.2.mp4}, etc.
   *
   * @param data the camera data to save
   */
  public void serializeFrame(CameraServerData data) {
    try {
      lock.lock();
      if (recorder == null) {
        log.warning("Attempting to write frame after saver has finished");
        return;
      }
      Mat image = data.getImage();
      if (image == null || image.getNativeObjAddr() == 0) {
        // No image to save, bail
        return;
      }
      if (frame == null) {
        frame = newFrameFromMat(image);
        resolution = new Resolution(image.width(), image.height());
        buffer = new byte[(int) (image.total() * image.channels())];
        wideBuffer = new int[buffer.length];
      } else if (resolution.isNotEqual(image.width(), image.height())) {
        // Stream resolution changed. Video files don't like frames with different resolutions, so finish writing the
        // current file and move on to writing to a new file instead
        try {
          finish();
        } catch (FrameRecorder.Exception e) {
          log.log(Level.WARNING, "Could not finish writing video file " + fileNum, e);
        }
        frame = newFrameFromMat(image);
        resolution = new Resolution(image.width(), image.height());
        recorder = createRecorder(fileNum.incrementAndGet());
        setupAndStartRecorder(data);
        buffer = new byte[(int) (image.total() * image.channels())];
        wideBuffer = new int[buffer.length];
      }
      image.get(0, 0, buffer);
      for (int i = 0; i < buffer.length; i++) {
        wideBuffer[i] = buffer[i] & 0xFF;
      }
      frame.<UByteBufferIndexer>createIndexer()
          .put(0, wideBuffer)
          .release();
      try {
        if (!running.get()) {
          setupAndStartRecorder(data);
        }
        recorder.setFrameNumber(frameNum.getAndIncrement());
        recorder.record(frame);
      } catch (FrameRecorder.Exception e) {
        throw new AssertionError("Could not save frame", e);
      } finally {
        image.release();
      }
    } finally {
      lock.unlock();
    }
  }

  private static Frame newFrameFromMat(Mat image) {
    return new Frame(
        image.width(),
        image.height(),
        OpenCVFrameConverter.getFrameDepth(image.depth()),
        image.channels()
    );
  }

  private void setupAndStartRecorder(CameraServerData data) {
    if (recorder == null) {
      return;
    }
    try {
      recorder.setImageWidth(resolution.getWidth());
      recorder.setImageHeight(resolution.getHeight());
      //recorder.setFrameRate(data.getFps()); // Doesn't work? "[mpeg4 @ 0x7f2...] The encoder timebase is not set."
      recorder.setVideoBitrate((int) (data.getBandwidth() * 8)); // x8 to covert bytes per second to bits per second
      recorder.start();
      running.set(true);
    } catch (FrameRecorder.Exception e) {
      throw new AssertionError("Could not start recorder", e);
    }
  }

  /**
   * Gets the index of the most recent frame that was saved to the current video file.
   */
  public int getFrameNum() {
    return frameNum.get();
  }

  public int getLastFrameNum() {
    return Math.max(0, getFrameNum() - 1);
  }

  /**
   * Gets the video file number. The first video file written is number 0, the second is 1, and so on.
   */
  public int getFileNum() {
    return fileNum.get();
  }

  /**
   * Finishes writing the current video file.
   *
   * @throws FrameRecorder.Exception if the file could not be written
   */
  public void finish() throws FrameRecorder.Exception {
    try {
      lock.lock();
      if (running.get()) {
        recorder.stop();
        running.set(false);
        frameNum.set(0);
        recorder = null;
      }
    } finally {
      lock.unlock();
    }
  }

  private FFmpegFrameRecorder createRecorder(int fileIndex) {
    String file = CameraStreamAdapter.videoFilePath(rootRecordingFile, cameraName, fileIndex);
    try {
      var path = Paths.get(file);
      if (Files.notExists(path)) {
        Files.createFile(path);
      }
    } catch (IOException e) {
      throw new AssertionError("Could not create video file " + file, e);
    }
    FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(file, 0);
    recorder.setVideoQuality(0); // lossless
    recorder.setVideoCodec(avcodec.AV_CODEC_ID_MPEG4);
    recorder.setFormat("mp4");
    recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
    return recorder;
  }

}
