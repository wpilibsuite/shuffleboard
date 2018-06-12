package edu.wpi.first.shuffleboard.plugin.cameraserver.source;

import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.avutil;
import org.bytedeco.javacpp.indexer.UByteIndexer;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.opencv.core.Mat;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.opencv.core.CvType.*;

public final class CameraStreamReader {

  private static final Logger log = Logger.getLogger(CameraStreamReader.class.getName());

  private final String cameraName;
  private final File rootRecordingFile;
  private FFmpegFrameGrabber grabber;
  private final AtomicBoolean started = new AtomicBoolean(false);

  private Mat mat;

  private final AtomicInteger fileNumber = new AtomicInteger(0);

  public CameraStreamReader(String cameraName, File rootRecordingFile) {
    this.cameraName = cameraName;
    this.rootRecordingFile = rootRecordingFile;
    grabber = createGrabber(0);
  }

  public void setFileNumber(int fileNumber) {
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
  }

  private FFmpegFrameGrabber createGrabber(int fileNumber) {
    String file = rootRecordingFile.getAbsolutePath().replace(".sbr", "-" + cameraName + "." + fileNumber + ".mp4");
    FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(file);
    grabber.setVideoCodec(avcodec.AV_CODEC_ID_MPEG4);
    grabber.setFormat("mp4");
    grabber.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
    return grabber;
  }

  public synchronized Mat readFrame(int frameNum) throws IOException {
    try {
      if (!started.get()) {
        grabber.start();
        started.set(true);
      }
      grabber.setFrameNumber(frameNum);
      Frame frame = grabber.grab();
      if (frame == null) {
        log.warning("No frame at index " + frameNum + " in video " + fileNumber);
        return null;
      }
      UByteIndexer indexer = frame.createIndexer();
      long size = indexer.width() * indexer.height() * indexer.channels();
      int[] buf = new int[(int) size];
      indexer.get(0, buf)
          .release();

      byte[] narrow = new byte[buf.length];
      for (int i = 0; i < buf.length; i++) {
        narrow[i] = (byte) buf[i];
      }

      if (mat == null) {
        mat = new Mat((int) indexer.height(), (int) indexer.width(), CV_8UC3);
      }
      mat.put(0, 0, narrow);

      return mat.clone();
    } catch (FrameGrabber.Exception e) {
      throw new IOException(e);
    }
  }

}
