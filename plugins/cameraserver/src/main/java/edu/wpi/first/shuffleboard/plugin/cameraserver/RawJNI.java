package edu.wpi.first.shuffleboard.plugin.cameraserver;

public class RawJNI {
    public static native long allocateRawFrame();
    public static native void freeRawFrame(long ptr);
    public static native long grabSinkFrame(int handle, RawFrame frame);
    public static native long grabSinkFrameTimeout(int handle, RawFrame frame, double timeout);
    public static native int createRawSink(String name);
}
