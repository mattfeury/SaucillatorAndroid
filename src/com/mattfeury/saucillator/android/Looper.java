package com.mattfeury.saucillator.android;

import java.util.LinkedList;
/**
 * Creates a loop
 */
public class Looper extends UGen {
  Float[] loopTable;
  LinkedList<Float> baseLoop;
  int pointer = 0;
	//boolean enabled = true;
  boolean defined = false;
  boolean recording = false;
  boolean playing = true;

  public Looper() {
    super();
    baseLoop = new LinkedList<Float>();
  }

  public synchronized void reset() {
    synchronized(this) {
      recording = false;
      defined = false;

      pointer = 0;
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
  }
  public synchronized void stopRecording() {
    recording = false;

    synchronized(this) {
      if (! defined) {      
        //setup loopTable
        loopTable = new Float[baseLoop.size()];
        baseLoop.toArray(loopTable);
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
	
	public boolean render(final float[] buffer) {
		boolean didWork = renderKids(buffer);
		
		if (! playing) return didWork;
		
		synchronized(this) {
      int origPointer = pointer;
      for(int i = 0; i < CHUNK_SIZE; i++) {
        if (recording) {
          if (! defined) {
              baseLoop.add((Float)buffer[i]);
          } else { //add rendered buffer of children to loop
            loopTable[origPointer] += buffer[i];
            origPointer = (origPointer + 1) % loopTable.length;
          }
        }
        if (defined) {
          buffer[i] += loopTable[pointer];
          pointer = (pointer + 1) % loopTable.length;
        }
      }
		}

		return didWork;
	}
}
