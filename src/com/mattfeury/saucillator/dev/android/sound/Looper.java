package com.mattfeury.saucillator.dev.android.sound;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Stack;
/**
 * Creates a loop
 */
public class Looper extends UGen {
  float[] loopTable;
  LinkedList<Float> baseLoop;
  Stack<float[]> loops;
  int pointer = 0;
	//boolean enabled = true;
  float amplitude = 0.9f; //don't playback loop at full volume. 
  boolean defined = false;
  boolean recording = false;
  boolean playing = true;

  public Looper() {
    super();
    baseLoop = new LinkedList<Float>();
    loops = new Stack<float[]>();
  }

  public synchronized void reset() {
    synchronized(this) {
      recording = false;
      defined = false;

      pointer = 0;
      loops.clear();
      baseLoop.clear();
    }
  }
  public synchronized void startPlaying() {
    playing = true;
  }
  public synchronized void stopPlaying() {
    playing = false;
  }
  public synchronized void startRecording() {
    recording = true;
    if (defined) {
      // Create a new layer for this loop
      float[] loop = new float[loopTable.length];
      Arrays.fill(loop, 0f);
      loops.push(loop);
    }
  }
  public synchronized void stopRecording() {
    recording = false;

    synchronized(this) {
      if (! defined) {      
        //setup loopTable
        loopTable = new float[baseLoop.size()];
        float[] stackedLoop = new float[baseLoop.size()];
        int i = 0;
        for(Float f : baseLoop) {
          loopTable[i] = f;
          stackedLoop[i] = f;
          i++;
        }
        loops.push(stackedLoop);

        // We don't need this reference anymore since it's in the stack.
        // Let it be garbage collected to reduce heap size. 
        // TODO garbage collection causes playback delay. see how this affects.
        baseLoop.clear();

        defined = true;
      }
    }
  }
  public synchronized boolean toggleRecording() {
    if (recording)
      stopRecording();
    else
      startRecording();

    return recording;
  }

  public synchronized void recalculateLoopTable() {
    synchronized(this) {
      Arrays.fill(loopTable, 0f);
      for (float[] loop : loops)
        for (int i=0; i < loop.length; i++)
          loopTable[i] += loop[i];
    }
  }
  public synchronized void removeLoopFromTable(final float[] loop) {
    synchronized(this) {
      if (loop.length != loopTable.length)
        return;

      for (int i=0; i < loopTable.length; i++)
        loopTable[i] -= loop[i];
    }
  }
  public synchronized void undo() {
    if (! defined) {
      reset();
      return;
    } else
      stopRecording();
    
    if (loops.size() != 0) {
      float[] loop = loops.pop();
      removeLoopFromTable(loop);
    }

    if (loops.size() == 0)
      reset();

  }
	
  public boolean render(final float[] buffer) {
    boolean didWork = renderKids(buffer);

    if (! playing) return didWork;

    synchronized(this) {
      int origPointer = pointer;
      float[] loop = null;
      if (recording && defined && loops.size() != 0)
        loop = loops.peek();

      for(int i = 0; i < CHUNK_SIZE; i++) {

        if (recording) {
          if (! defined) {
            baseLoop.add((Float)buffer[i]);
          } else {
            // Add to full loop
            loopTable[origPointer] += buffer[i];

            // Add to newest layer (created when recording starts)
            loop[origPointer] += buffer[i];

            origPointer = (origPointer + 1) % loopTable.length;
          }
        }

        if (defined) {
          if (recording) //buffer has already been added to looptable
            buffer[i] = amplitude*loopTable[pointer];
          else
            buffer[i] += amplitude*loopTable[pointer];

          pointer = (pointer + 1) % loopTable.length;
        }
      }
		}

		return didWork;
	}
}
