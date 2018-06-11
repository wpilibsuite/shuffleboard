package edu.wpi.first.shuffleboard.plugin.cameraserver.source;

import edu.wpi.first.shuffleboard.plugin.cameraserver.data.CameraServerData;

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

public final class CameraStreamSaver {

  private final String cameraName;
  private FFmpegFrameRecorder recorder;
  private Frame frame;
  private final AtomicBoolean started = new AtomicBoolean(false);
  private final AtomicInteger frameNum = new AtomicInteger(0);
  private final AtomicInteger fileNum = new AtomicInteger(0);
  private final AtomicBoolean hasEverStarted = new AtomicBoolean(false);

  public CameraStreamSaver(String cameraName, File rootRecordingFile) {
    this.cameraName = cameraName;
    recorder = createRecorder(cameraName, rootRecordingFile, 0);
  }

  public synchronized void serializeFrame(CameraServerData data) {
    if (!started.get() && hasEverStarted.get()) {
      return;
    }
    Mat image = data.getImage();
    if (image == null || image.getNativeObjAddr() == 0) {
      // No image to save, bail
      return;
    }
    if (frame == null) {
      frame = new Frame(image.width(), image.height(), OpenCVFrameConverter.getFrameDepth(image.depth()), image.channels());
    }
    byte[] b = new byte[(int) (image.total() * image.channels())];
    image.get(0, 0, b);
    int[] wide = new int[b.length];
    for (int i = 0; i < b.length; i++) {
      wide[i] = b[i] & 0xFF;
    }
    frame.<UByteBufferIndexer>createIndexer()
        .put(0, wide)
        .release();
    try {
      if (!started.get()) {
        try {
          recorder.setImageWidth(image.width());
          recorder.setImageHeight(image.height());
          //recorder.setFrameRate(data.getFps()); // Doesn't work? "[mpeg4 @ 0x7f2...] The encoder timebase is not set."
          recorder.setVideoBitrate((int) (data.getBandwidth() * 8)); // x8 to covert bytes per second to bits per second
          recorder.start();
          started.set(true);
          hasEverStarted.set(true);
        } catch (FrameRecorder.Exception e) {
          throw new AssertionError("Could not start recorder", e);
        }
      }
      recorder.setFrameNumber(frameNum.getAndIncrement());
      recorder.record(frame);
    } catch (FrameRecorder.Exception e) {
      throw new AssertionError("Could not save frame", e);
    } finally {
      image.release();
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
  }

  private static FFmpegFrameRecorder createRecorder(String name, File rootRecordingFile, int fileIndex) {
    String file = rootRecordingFile.getAbsolutePath().replace(".sbr", "-" + name +  "." + fileIndex + ".mp4");
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
