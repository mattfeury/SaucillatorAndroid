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
  boolean mutex = false;
  boolean playing = true;
	
	public Looper() {
		super();
    baseLoop = new LinkedList<Float>();
	}

	public void reset() {
		mutex = true;
		recording = false;
		defined = false;
		
		pointer = 0;
		baseLoop.clear();
		
		mutex = false;
	}
	public void startPlaying() {
		playing = true;
	}
	public void stopPlaying() {
		playing = false;
	}
  public void startRecording() {
    recording = true;
  }
  public void stopRecording() {
    recording = false;
    mutex = true;

    if (! defined) {      
      //setup loopTable
      loopTable = new Float[baseLoop.size()];
      baseLoop.toArray(loopTable);
      defined = true;
      mutex = false;
    }
  }
  public boolean toggleRecording() {
    if (recording)
      stopRecording();
    else
      startRecording();

    return recording;
  }
	
	public boolean render(final float[] buffer) {
		boolean didWork = renderKids(buffer);
		
		if (! playing) return didWork;
		
    int origPointer = pointer;
    for(int i = 0; i < CHUNK_SIZE; i++) {
      if (recording) {
        if (! defined) {
          if (! mutex)
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

		return didWork;
	}
}
