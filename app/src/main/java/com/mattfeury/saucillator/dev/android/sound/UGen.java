package com.mattfeury.saucillator.dev.android.sound;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A Unit Generator. 
 * I am the basic class for anything that makes noise
 */
public abstract class UGen implements Serializable {
	public static final int CHUNK_SIZE = 256; //formerly 1024
	public static final int SAMPLE_RATE = 11025 * 2;
	
	private boolean isPlaying = false;
	
	ArrayList<UGen> kids = new ArrayList<UGen>(0);
	
	// fill CHUNK_SIZE samples
	// and return true if you actually did any work
	abstract public boolean render(final float[] buffer);

	final public synchronized UGen chuck(UGen that) {
		if(!that.kids.contains(this)) that.kids.add(this);
		return that; // returns RHS
	}
	
	final public synchronized UGen unchuck(UGen that) {		
		if(that.kids.contains(this)) that.kids.remove(this);
		return that; // returns RHS
	}
	
	protected void zeroBuffer(final float[] buffer) {
		for(int i = 0; i < CHUNK_SIZE; i++) {
			buffer[i] = 0;//gen.nextFloat() * 20; //static
		}
	}

  public void togglePlayback() {
    setPlaying(!isPlaying());
  }
  public boolean isPlaying() {
    return isPlaying;
  }
  public void setPlaying(boolean isPlaying) {
    this.isPlaying = isPlaying;
  }

  public void stop() {
    setPlaying(false);
  }
  public void start() {
    setPlaying(true);
  }

	protected boolean renderKids(final float[] buffer) {
		boolean didSomeRealWork = false;
		for(int k = 0; k < kids.size(); k++) {
			didSomeRealWork |= kids.get(k).render(buffer);
		}
		return didSomeRealWork;
	}
}
