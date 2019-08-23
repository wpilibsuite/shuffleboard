package edu.wpi.first.shuffleboard.plugin.cameraserver;

import java.nio.ByteBuffer;

import edu.wpi.cscore.VideoMode;
import edu.wpi.cscore.VideoSink;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import edu.wpi.cscore.VideoMode.PixelFormat;

public class JavaCvSink extends VideoSink {
    RawFrame frame = new RawFrame();
    Mat tmpMat;
    ByteBuffer origByteBuffer;
    int width;
    int height;
    int pixelFormat;
    int bgrValue = PixelFormat.kBGR.getValue();

    private int getCVFormat(PixelFormat pixelFormat) {
        int type = 0;
        switch (pixelFormat) {
            case kYUYV:
            case kRGB565:
                type = CvType.CV_8UC2;
                break;
            case kBGR:
                type = CvType.CV_8UC3;
                break;
            case kGray:
            case kMJPEG:
            default:
                type = CvType.CV_8UC1;
                break;
        }
        return type;
    }

    @Override
    public void close() {
        frame.close();
        super.close();
    }

    public void setEnabled(boolean enabled) {

    }

    public String getError() {
        return "";
    }

    /**
     * Create a sink for accepting OpenCV images.
     * WaitForFrame() must be called on the created sink to get each new
     * image.
     *
     * @param name Source name (arbitrary unique identifier)
     */
    public JavaCvSink(String name) {
        super(RawJNI.createRawSink(name));
    }

    /**
     * Wait for the next frame and get the image.
     * Times out (returning 0) after timeout seconds.
     * The provided image will have three 3-bit channels stored in BGR order.
     *
     * @return Frame time, or 0 on error (call GetError() to obtain the error
     *         message); the frame time is in 1 us increments.
     */
    public long grabFrameNoTimeout(Mat image) {
        frame.setWidth(0);
        frame.setHeight(0);
        frame.setPixelFormat(bgrValue);
        long rv = RawJNI.grabSinkFrame(m_handle, frame);
        if (rv <= 0) {
            return rv;
        }

        if (frame.getDataByteBuffer() != origByteBuffer || width != frame.getWidth() || height != frame.getHeight() || pixelFormat != frame.getPixelFormat()) {
            origByteBuffer = frame.getDataByteBuffer();
            height = frame.getHeight();
            width = frame.getWidth();
            pixelFormat = frame.getPixelFormat();
            tmpMat = new Mat(frame.getHeight(), frame.getWidth(), getCVFormat(VideoMode.getPixelFormatFromInt(pixelFormat)), origByteBuffer);
        }
        tmpMat.copyTo(image);
        return rv;
    }
}
