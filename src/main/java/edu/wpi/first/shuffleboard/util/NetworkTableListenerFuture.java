package edu.wpi.first.shuffleboard.util;

import edu.wpi.first.wpilibj.networktables.NetworkTablesJNI;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A type of {@link Future} that is completed on the network table listener thread.
 */
public class NetworkTableListenerFuture implements Future {

  private final Object lock = new Object();

  private final int listenerId;
  private volatile boolean completed = false;
  private volatile boolean cancelled = false;

  public NetworkTableListenerFuture() {
    listenerId = NetworkTablesJNI.addEntryListener("", this::onUpdate, 0xFF);
  }

  private void onUpdate(int uid, String key, Object value, int flags) {
    completed = true;
    synchronized (lock) {
      lock.notifyAll();
    }
  }

  private void removeListener() {
    NetworkTablesJNI.removeEntryListener(listenerId);
  }

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    if (completed || cancelled) {
      return false;
    }
    removeListener();
    cancelled = true;
    return cancelled;
  }

  @Override
  public boolean isCancelled() {
    return cancelled;
  }

  @Override
  public boolean isDone() {
    return completed;
  }

  @Override
  public Object get() throws InterruptedException, ExecutionException {
    if (completed || cancelled) {
      return null;
    }
    synchronized (lock) {
      while (!completed) {
        lock.wait();
      }
    }
    removeListener();
    return null;
  }

  @Override
  public Object get(long timeout, TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    if (completed || cancelled) {
      return null;
    }
    synchronized (lock) {
      lock.wait(unit.toMillis(timeout));
    }
    removeListener();
    return null;
  }

}
