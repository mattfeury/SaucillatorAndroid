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
  boolean recording = true;
	
	public Looper() {
		super();
    baseLoop = new LinkedList<Float>();
	}

  public void startRecording() {
    recording = true;
  }
  public void stopRecording() {
    recording = false;

    if (! defined) {      
      //setup loopTable
      loopTable = new Float[baseLoop.size()];
      baseLoop.toArray(loopTable);
      defined = true;
    }
  }
	
	public boolean render(final float[] buffer) {
		boolean didWork = renderKids(buffer);
		
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
        try {
          buffer[i] += loopTable[pointer];
        } catch(Exception e) {} //for concurrency issues
        finally {
          pointer = (pointer + 1) % loopTable.length;
        }
      }
    }

		return didWork;
	}
}
