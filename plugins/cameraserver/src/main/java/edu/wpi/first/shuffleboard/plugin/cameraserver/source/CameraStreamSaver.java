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
import java.util.logging.Level;
import java.util.logging.Logger;

public final class CameraStreamSaver {

  private static final Logger log = Logger.getLogger(CameraStreamSaver.class.getName());

  private final String cameraName;
  private final File rootRecordingFile;
  private FFmpegFrameRecorder recorder;
  private Frame frame;
  private final AtomicBoolean started = new AtomicBoolean(false);
  private final AtomicInteger frameNum = new AtomicInteger(0);
  private final AtomicInteger fileNum = new AtomicInteger(0);
  private Resolution resolution = null;
  private byte[] buffer = null;

  public CameraStreamSaver(String cameraName, File rootRecordingFile) {
    this.cameraName = cameraName;
    this.rootRecordingFile = rootRecordingFile;
    recorder = createRecorder(cameraName, rootRecordingFile, 0);
  }

  public synchronized void serializeFrame(CameraServerData data) {
    Mat image = data.getImage();
    if (image == null || image.getNativeObjAddr() == 0) {
      // No image to save, bail
      return;
    }
    if (frame == null) {
      frame = newFrameFromMat(image);
      resolution = new Resolution(image.width(), image.height());
      buffer = new byte[(int) (image.total() * image.channels())];
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
      recorder = createRecorder(cameraName, rootRecordingFile, fileNum.incrementAndGet());
      setupAndStartRecorder(data);
      buffer = new byte[(int) (image.total() * image.channels())];
    }
    image.get(0, 0, buffer);
    int[] wide = new int[buffer.length];
    for (int i = 0; i < buffer.length; i++) {
      wide[i] = buffer[i] & 0xFF;
    }
    frame.<UByteBufferIndexer>createIndexer()
        .put(0, wide)
        .release();
    try {
      if (!started.get()) {
        setupAndStartRecorder(data);
      }
      recorder.setFrameNumber(frameNum.getAndIncrement());
      recorder.record(frame);
    } catch (FrameRecorder.Exception e) {
      throw new AssertionError("Could not save frame", e);
    } finally {
      image.release();
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
    try {
      recorder.setImageWidth(resolution.getWidth());
      recorder.setImageHeight(resolution.getHeight());
      //recorder.setFrameRate(data.getFps()); // Doesn't work? "[mpeg4 @ 0x7f2...] The encoder timebase is not set."
      recorder.setVideoBitrate((int) (data.getBandwidth() * 8)); // x8 to covert bytes per second to bits per second
      recorder.start();
      started.set(true);
    } catch (FrameRecorder.Exception e) {
      throw new AssertionError("Could not start recorder", e);
    }
  }

  public int getFrameNum() {
    return frameNum.get();
  }

  public int getFileNum() {
    return fileNum.get();
  }

  public synchronized void finish() throws FrameRecorder.Exception {
    recorder.stop();
    started.set(false);
    frameNum.set(0);
    recorder = null;
  }

  private static FFmpegFrameRecorder createRecorder(String name, File rootRecordingFile, int fileIndex) {
    String file = rootRecordingFile.getAbsolutePath().replace(".sbr", "-" + name + "." + fileIndex + ".mp4");
    try {
      Files.createFile(Paths.get(file));
    } catch (IOException e) {
      throw new AssertionError("Could not create video file " + file, e);
    }
    FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(file, 0);
    recorder.setVideoCodec(avcodec.AV_CODEC_ID_MPEG4);
    recorder.setFormat("mp4");
    recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
    return recorder;
  }

}
