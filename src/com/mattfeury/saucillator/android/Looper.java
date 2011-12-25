package com.mattfeury.saucillator.android;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Stack;
/**
 * Creates a loop
 */
public class Looper extends UGen {
  Float[] loopTable;
  LinkedList<Float> baseLoop;
  Stack<Float[]> loops;
  int pointer = 0;
	//boolean enabled = true;
  float amplitude = 0.75f; //don't playback loop at full volume. 
  boolean defined = false;
  boolean recording = false;
  boolean playing = true;

  public Looper() {
    super();
    baseLoop = new LinkedList<Float>();
    loops = new Stack<Float[]>();
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
      Float[] loop = new Float[loopTable.length];
      Arrays.fill(loop, 0f);
      loops.push(loop);
    }
  }
  public synchronized void stopRecording() {
    recording = false;

    synchronized(this) {
      if (! defined) {      
        //setup loopTable
        loopTable = new Float[baseLoop.size()];
        baseLoop.toArray(loopTable);
        loops.push(loopTable.clone());
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
      for (Float[] loop : loops)
        for (int i=0; i < loop.length; i++)
          loopTable[i] += loop[i];
    }
  }
  public synchronized void removeLoopFromTable(final Float[] loop) {
    synchronized(this) {
      if (loop.length != loopTable.length)
        return;

      for (int i=0; i < loopTable.length; i++)
        loopTable[i] -= loop[i];
    }
  }
  public synchronized void undo() {
    if (! defined)
      return;

    stopRecording();
    
    if (loops.size() != 0) {
      Float[] loop = loops.pop();
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
      for(int i = 0; i < CHUNK_SIZE; i++) {

        if (defined) {
          buffer[i] += amplitude*loopTable[pointer];
          pointer = (pointer + 1) % loopTable.length;
        }

        if (recording) {
          if (! defined) {
            baseLoop.add((Float)buffer[i]);
          } else {
            // Add to full loop
            loopTable[origPointer] += buffer[i];

            // Add to newest layer (created when recording starts)
            Float[] loop = loops.peek();
            loop[origPointer] += buffer[i];

            origPointer = (origPointer + 1) % loopTable.length;
          }
        }
      }
		}

		return didWork;
	}
}
