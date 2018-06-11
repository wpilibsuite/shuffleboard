package edu.wpi.first.shuffleboard.plugin.cameraserver.source;

import edu.wpi.first.shuffleboard.api.sources.recording.serialization.TypeAdapter;
import edu.wpi.first.shuffleboard.plugin.cameraserver.data.CameraServerData;
import edu.wpi.first.shuffleboard.plugin.cameraserver.data.type.CameraServerDataType;

import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.avutil;
import org.bytedeco.javacpp.indexer.UByteBufferIndexer;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.opencv.core.Mat;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class CameraStreamRecorder extends TypeAdapter<CameraServerData> {

  private final Map<String, FFmpegFrameRecorder> savers = new HashMap<>();
  private final Map<String, FFmpegFrameGrabber> readers = new HashMap<>();

  public CameraStreamRecorder() {
    super(CameraServerDataType.Instance);
  }

  @Override
  public synchronized void flush() {
    // TODO make this able to update the existing video file
    /*
    savers.values().forEach(r -> {
      try {
        r.stop();
      } catch (FrameRecorder.Exception e) {
        e.printStackTrace();
      }
    });
    savers.clear();
    started = false;
    frame = null;
    // Do not reset frame num
    */
  }

  @Override
  public synchronized void cleanUp() {
    savers.values().forEach(r -> {
      try {
        r.stop();
      } catch (FrameRecorder.Exception e) {
        e.printStackTrace();
      }
    });
    readers.clear();
    frameNum.set(0);
  }

  @Override
  public CameraServerData deserialize(byte[] buffer, int bufferPosition) {
    // TODO
    return null;
  }

  @Override
  public int getSerializedSize(CameraServerData value) {
    // TODO
    return 0;
  }

  @Override
  public synchronized byte[] serialize(CameraServerData data) {
    serializeFrame(data.getName(), data.getImage());
    return new byte[0];
  }

  private Frame frame;
  private final AtomicInteger frameNum = new AtomicInteger();
  private boolean started = false;

  public synchronized void serializeFrame(String cameraName, Mat image) {
    FFmpegFrameRecorder recorder = savers.computeIfAbsent(cameraName, this::createRecorder);
    recorder.setVideoQuality(1);
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
      if (!started) {
        try {
          recorder.setImageWidth(image.width());
          recorder.setImageHeight(image.height());
          recorder.start();
          started = true;
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

  private FFmpegFrameRecorder createRecorder(String name) {
    String file = getCurrentFile().getAbsolutePath().replace(".sbr", "-" + name + ".mp4");
    try {
      new File(file).createNewFile();
    } catch (IOException e) {
      throw new AssertionError(e);
    }
    FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(file, 0);
    recorder.setVideoCodec(avcodec.AV_CODEC_ID_MPEG4);
    recorder.setFormat("mp4");
    recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
//    recorder.setVideoBitrate(1_000_000);
    return recorder;
  }

}
