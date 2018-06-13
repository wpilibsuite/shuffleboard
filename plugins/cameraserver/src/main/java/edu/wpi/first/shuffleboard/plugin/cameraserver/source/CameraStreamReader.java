package edu.wpi.first.shuffleboard.plugin.cameraserver.source;

import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.indexer.UByteIndexer;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.opencv.core.Mat;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.opencv.core.CvType.CV_8UC3;

/**
 * Reads images from saved video files for a single camera stream in playback.
 */
public final class CameraStreamReader {

  private static final Logger log = Logger.getLogger(CameraStreamReader.class.getName());

  private final String cameraName;
  private final File rootRecordingFile;
  private FFmpegFrameGrabber grabber;
  private final AtomicBoolean started = new AtomicBoolean(false);
  private int[] buffer;
  private byte[] narrowBuffer;

  private final Lock lock = new ReentrantLock();

  private Mat mat;

  private final AtomicInteger fileNumber = new AtomicInteger(0);

  /**
   * Creates a new video recording reader.
   *
   * @param cameraName        the name of camera stream
   * @param rootRecordingFile the root recording file being read
   */
  public CameraStreamReader(String cameraName, File rootRecordingFile) {
    this.cameraName = cameraName;
    this.rootRecordingFile = rootRecordingFile;
    grabber = createGrabber(0);
  }

  /**
   * Sets the video file number to read from. File numbers are saved in the main recording file as part of the frame
   * data.
   *
   * @param fileNumber the file number to read
   */
  public void setFileNumber(int fileNumber) {
    try {
      lock.lock();
      if (fileNumber != this.fileNumber.get()) {
        this.fileNumber.set(fileNumber);
        try {
          grabber.stop();
          started.set(false);
        } catch (FrameGrabber.Exception e) {
          log.log(Level.WARNING, "Could not clean up grabber", e);
        }
        mat = null;
        grabber = createGrabber(fileNumber);
      }
    } finally {
      lock.unlock();
    }
  }

  private FFmpegFrameGrabber createGrabber(int fileNumber) {
    String file = CameraStreamRecorder.videoFilePath(rootRecordingFile, cameraName, fileNumber);
    FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(file);
    grabber.setVideoCodec(avcodec.AV_CODEC_ID_MPEG4);
    grabber.setFormat("mp4");
    return grabber;
  }

  /**
   * Reads a single frame from the current video file.
   *
   * @param frameNum the frame number to read
   *
   * @return the frame at the given frame index
   *
   * @throws IOException if a frame could not be read from the video file
   */
  public Mat readFrame(int frameNum) throws IOException {
    try {
      lock.lock();
      if (!started.get()) {
        grabber.start();
        started.set(true);
      }
      grabber.setFrameNumber(frameNum);
      Frame frame = grabber.grabFrame(false, true, true, false);
      if (frame == null) {
        // Maybe `do { frame = grabber.grab() } while (frame == null)` instead?
        log.warning("No frame at index " + frameNum + " in video " + fileNumber);
        return null;
      }
      UByteIndexer indexer = frame.createIndexer();
      if (mat == null) {
        mat = new Mat((int) indexer.height(), (int) indexer.width(), CV_8UC3);
        long size = indexer.width() * indexer.height() * indexer.channels();
        if (size > Integer.MAX_VALUE) {
          log.warning(String.format("Frame too large: %.2fGB", size / 1e9));
          return null;
        }
        buffer = new int[(int) size];
        narrowBuffer = new byte[(int) size];
      }
      indexer.get(0, buffer)
          .release();

      for (int i = 0; i < buffer.length; i++) {
        narrowBuffer[i] = (byte) buffer[i];
      }

      mat.put(0, 0, narrowBuffer);

      return mat.clone();
    } catch (FrameGrabber.Exception e) {
      throw new IOException("Could not read frame " + frameNum + " from video file #" + fileNumber, e);
    } finally {
      lock.unlock();
    }
  }

}
